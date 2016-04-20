package org.antlr.codebuff;

import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.codebuff.misc.HashBag;
import org.antlr.codebuff.misc.ParentSiblingListKey;
import org.antlr.codebuff.misc.SiblingListStats;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** [USED IN TRAINING ONLY]
 *  Find subtree roots with repeated child rule subtrees and separators.
 *  Track oversize and regular lists are sometimes treated differently, such as
 *  formal arg lists in Java. Sometimes they are split across lines.
 */
public class CollectSiblingLists implements ParseTreeListener {
	/** Track set of (parent:alt,child:alt) list pairs and their min,median,variance,max
	 *  but only if the list is all on one line and has a separator.
	 */
	public Map<ParentSiblingListKey, List<Integer>> listInfo = new HashMap<>();
	/** Track set of (parent:alt,child:alt) list pairs and their min,median,variance,max
	 *  but only if the list is split with at least one '\n' before/after
	 *  a separator.
	 */
	public Map<ParentSiblingListKey, List<Integer>> splitListInfo = new HashMap<>();

	public Map<ParentSiblingListKey, List<Integer>> splitListForm = new HashMap<>();

	CodeBuffTokenStream tokens;

	// reuse object so the maps above fill from multiple files during training
	public void setTokens(CodeBuffTokenStream tokens) {
		this.tokens = tokens;
	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		// Find sibling lists that are children of this parent node
		Set<Class> completed = new HashSet<>(); // only count sibling list for each subtree type once
		for (int i = 0; i<ctx.getChildCount(); i++) {
			ParseTree child = ctx.getChild(i);

			if ( completed.contains(child.getClass()) ) continue; // avoid counting repeatedly
			completed.add(child.getClass());

			if ( child instanceof ParserRuleContext ) { // found subtree child
				List<? extends ParserRuleContext> siblings =
					ctx.getRuleContexts(((ParserRuleContext)child).getClass());
				if ( siblings.size()>1 ) { // we found a non-singleton list
					// check for separator by looking between first two siblings (assume all are same)
					ParserRuleContext first = siblings.get(0);
					ParserRuleContext second = siblings.get(1);

					List<Tree> children = Trees.getChildren(ctx);
					int firstIndex = children.indexOf(first);
					int secondIndex = children.indexOf(second);

					if ( firstIndex+1 == secondIndex ) continue; // nothing between first and second so no separator

					ParseTree between = ctx.getChild(firstIndex+1);
					if ( between instanceof TerminalNode ) { // is it a token?
						Token separator = ((TerminalNode)between).getSymbol();
						processNonSingletonWithSeparator(ctx, siblings, separator);
					}
				}
			}
		}
	}

	public void processNonSingletonWithSeparator(ParserRuleContext ctx, List<? extends ParserRuleContext> siblings, Token separator) {
		ParserRuleContext first = siblings.get(0);
		ParserRuleContext last = siblings.get(siblings.size()-1);
		Triple<Integer, Integer, Integer> key =	new Triple<>(ctx.getRuleIndex(), ctx.getAltNumber(), separator.getType());
		// map (parent:alt,tokentype) -> (child:alt) so we can create look up key for listInfo later if we want
		List<Token> hiddenToLeft       = tokens.getHiddenTokensToLeft(first.getStart().getTokenIndex());
		List<Token> hiddenToLeftOfSep  = tokens.getHiddenTokensToLeft(separator.getTokenIndex());
		List<Token> hiddenToRightOfSep = tokens.getHiddenTokensToRight(separator.getTokenIndex());
		List<Token> hiddenToRight      = tokens.getHiddenTokensToRight(last.getStop().getTokenIndex());

		Token hiddenTokenToLeft = hiddenToLeft.get(0);
		Token hiddenTokenToRight = hiddenToRight.get(0);

		tokens.seek(first.getStart().getTokenIndex());
		Token prefixToken = tokens.LT(-1); // e.g., '(' in an arg list or ':' in grammar def
		tokens.seek(last.getStop().getTokenIndex());
		Token suffixToken = tokens.LT(1);  // e.g., ')' in an arg list of ';' in grammar def

		int[] ws = new int[4]; // '\n' (before list, before sep, after sep, after last element)
		if ( hiddenToLeft!=null && Tool.count(hiddenTokenToLeft.getText(), '\n')>0 ) {
			ws[0] = '\n';
		}
		if ( hiddenToLeftOfSep!=null && Tool.count(hiddenToLeftOfSep.get(0).getText(), '\n')>0 ) {
			ws[1] = '\n';
			System.out.println("BEFORE "+JavaParser.ruleNames[ctx.getRuleIndex()]+
				                   "->"+JavaParser.ruleNames[ctx.getRuleIndex()]+" sep "+
				                   JavaParser.tokenNames[separator.getType()]+
				                   " "+separator);
		}
		if ( hiddenToRightOfSep!=null && Tool.count(hiddenToRightOfSep.get(0).getText(), '\n')>0 ) {
			ws[2] = '\n';
			System.out.println("AFTER "+JavaParser.ruleNames[ctx.getRuleIndex()]+
				                   "->"+JavaParser.ruleNames[ctx.getRuleIndex()]+" sep "+
				                   JavaParser.tokenNames[separator.getType()]+
				                   " "+separator);
		}
		if ( hiddenToRight!=null && Tool.count(hiddenTokenToRight.getText(), '\n')>0 ) {
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
		lens.add(CollectFeatures.getSiblingsLength(siblings));

		// track the form split lists take
		if ( isSplitList ) {
			int form = CollectFeatures.listform(ws);
			List<Integer> forms = splitListForm.get(pair);
			if ( forms==null ) {
				forms = new ArrayList<>();
				splitListForm.put(pair, forms);
			}
			forms.add(form); // track where we put newlines for this list
		}
	}

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
			double var = variance(lens);
			listSizes.put(pair, new SiblingListStats(n, min, median, var, max));
		}
		return listSizes;
	}

	public static int sum(List<Integer> data) {
		int sum = 0;
		for (int d : data) {
			sum += d;
		}
		return sum;
	}

	public static double variance(List<Integer> data) {
		int n = data.size();
		double sum = 0;
		double avg = sum(data) / ((double)n);
		for (int d : data) {
			sum += (d-avg)*(d-avg);
		}
		return sum / n;
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
