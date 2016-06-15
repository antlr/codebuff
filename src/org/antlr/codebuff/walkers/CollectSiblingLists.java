package org.antlr.codebuff.walkers;

import org.antlr.codebuff.Tool;
import org.antlr.codebuff.Trainer;
import org.antlr.codebuff.kNNClassifier;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.codebuff.misc.HashBag;
import org.antlr.codebuff.misc.ParentSiblingListKey;
import org.antlr.codebuff.misc.SiblingListStats;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** [USED IN TRAINING ONLY]
 *  Find subtree roots with repeated child rule subtrees and separators.
 *  Track oversize and regular lists are sometimes treated differently, such as
 *  formal arg lists in Java. Sometimes they are split across lines.
 *
 *  A single instance is shared across all training docs to collect complete info.
 */
public class CollectSiblingLists extends VisitSiblingLists {
	// listInfo and splitListInfo are used to collect statistics for use by the formatting engine when computing "is oversize list"

	/** Track set of (parent:alt,child:alt) list pairs and their min,median,variance,max
	 *  but only if the list is all on one line and has a separator.
	 */
	public Map<ParentSiblingListKey, List<Integer>> listInfo = new HashMap<>();

	/** Track set of (parent:alt,child:alt) list pairs and their min,median,variance,max
	 *  but only if the list is split with at least one '\n' before/after
	 *  a separator.
	 */
	public Map<ParentSiblingListKey, List<Integer>> splitListInfo = new HashMap<>();

	/** Debugging */
	public Map<ParentSiblingListKey, List<Integer>> splitListForm = new HashMap<>();

	/** Map token to ("is oversize", element type). Used to compute feature vector. */
	public Map<Token,Pair<Boolean,Integer>> tokenToListInfo = new HashMap<>();

	public Map<Token, TerminalNode> tokenToNodeMap = null;

	public CodeBuffTokenStream tokens;

	// reuse object so the maps above fill from multiple files during training
	public void setTokens(CodeBuffTokenStream tokens, ParserRuleContext root, Map<Token, TerminalNode> tokenToNodeMap) {
		this.tokens = tokens;
		this.tokenToNodeMap = tokenToNodeMap;
	}

	public void visitNonSingletonWithSeparator(ParserRuleContext ctx, List<? extends ParserRuleContext> siblings, Token separator) {
		ParserRuleContext first = siblings.get(0);
		ParserRuleContext last = siblings.get(siblings.size()-1);
		List<Token> hiddenToLeft       = tokens.getHiddenTokensToLeft(first.getStart().getTokenIndex());
		List<Token> hiddenToLeftOfSep  = tokens.getHiddenTokensToLeft(separator.getTokenIndex());
		List<Token> hiddenToRightOfSep = tokens.getHiddenTokensToRight(separator.getTokenIndex());
		List<Token> hiddenToRight      = tokens.getHiddenTokensToRight(last.getStop().getTokenIndex());

		Token hiddenTokenToLeft = hiddenToLeft!=null ? hiddenToLeft.get(0) : null;
		Token hiddenTokenToRight = hiddenToRight!=null ? hiddenToRight.get(0) : null;

		int[] ws = new int[4]; // '\n' (before list, before sep, after sep, after last element)
		if ( hiddenTokenToLeft!=null && Tool.count(hiddenTokenToLeft.getText(), '\n')>0 ) {
			ws[0] = '\n';
		}
		if ( hiddenToLeftOfSep!=null && Tool.count(hiddenToLeftOfSep.get(0).getText(), '\n')>0 ) {
			ws[1] = '\n';
//			System.out.println("BEFORE "+JavaParser.ruleNames[ctx.getRuleIndex()]+
//				                   "->"+JavaParser.ruleNames[ctx.getRuleIndex()]+" sep "+
//				                   JavaParser.tokenNames[separator.getType()]+
//				                   " "+separator);
		}
		if ( hiddenToRightOfSep!=null && Tool.count(hiddenToRightOfSep.get(0).getText(), '\n')>0 ) {
			ws[2] = '\n';
//			System.out.println("AFTER "+JavaParser.ruleNames[ctx.getRuleIndex()]+
//				                   "->"+JavaParser.ruleNames[ctx.getRuleIndex()]+" sep "+
//				                   JavaParser.tokenNames[separator.getType()]+
//				                   " "+separator);
		}
		if ( hiddenTokenToRight!=null && Tool.count(hiddenTokenToRight.getText(), '\n')>0 ) {
			ws[3] = '\n';
		}
		boolean isSplitList = ws[1]=='\n' || ws[2]=='\n';

		// now track length of parent:alt,child:alt list or split-list
		ParentSiblingListKey pair = new ParentSiblingListKey(ctx, first, separator.getType());
		Map<ParentSiblingListKey, List<Integer>> info = isSplitList ? splitListInfo : listInfo;
		List<Integer> lens = info.get(pair);
		if ( lens==null ) {
			lens = new ArrayList<>();
			info.put(pair, lens);
		}
		lens.add(Trainer.getSiblingsLength(siblings));

		// track the form split lists take for debugging
		if ( isSplitList ) {
			int form = Trainer.listform(ws);
			List<Integer> forms = splitListForm.get(pair);
			if ( forms==null ) {
				forms = new ArrayList<>();
				splitListForm.put(pair, forms);
			}
			forms.add(form); // track where we put newlines for this list
		}

		Map<Token, Pair<Boolean, Integer>> tokenInfo =
			getInfoAboutListTokens(ctx, tokens, tokenToNodeMap, siblings, isSplitList);

		// copy sibling list info for associated tokens into overall list
		// but don't overwrite existing so that most general (largest construct)
		// list information is use/retained (i.e., not overwritten).
		for (Token t : tokenInfo.keySet()) {
			if ( !tokenToListInfo.containsKey(t) ) {
				tokenToListInfo.put(t, tokenInfo.get(t));
			}
		}
	}

	// for debugging
	public Map<ParentSiblingListKey, Integer> getSplitListForms() {
		Map<ParentSiblingListKey, Integer> results = new HashMap<>();
		for (ParentSiblingListKey pair : splitListForm.keySet()) {
			HashBag<Integer> votes = new HashBag<>();
			List<Integer> forms = splitListForm.get(pair);
			forms.forEach(votes::add);
			int mostCommonForm = kNNClassifier.getCategoryWithMostVotes(votes);
			results.put(pair, mostCommonForm);
		}
		return results;
	}

	public Map<ParentSiblingListKey, SiblingListStats> getListStats() {
		return getListStats(listInfo);
	}

	public Map<ParentSiblingListKey, SiblingListStats> getSplitListStats() {
		return getListStats(splitListInfo);
	}

	public Map<ParentSiblingListKey, SiblingListStats> getListStats(Map<ParentSiblingListKey, List<Integer>> map) {
		Map<ParentSiblingListKey, SiblingListStats> listSizes = new HashMap<>();
		for (ParentSiblingListKey pair : map.keySet()) {
			List<Integer> lens = map.get(pair);
			Collections.sort(lens);
			int n = lens.size();
			Integer min = lens.get(0);
			Integer median = lens.get(n/2);
			Integer max = lens.get(n-1);
			double var = BuffUtils.variance(lens);
			listSizes.put(pair, new SiblingListStats(n, min, median, var, max));
		}
		return listSizes;
	}

	public Map<Token, Pair<Boolean, Integer>> getTokenToListInfo() {
		return tokenToListInfo;
	}

	@Override
	public void visitTerminal(TerminalNode node) {
	}

	@Override
	public void visitErrorNode(ErrorNode node) {
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
	}
}
