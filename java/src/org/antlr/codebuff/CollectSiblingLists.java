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

/** Find subtree roots with repeated child subtrees, such as typeArguments
 *  has more than 1 typeArgument child.  For a formatting feature vector
 *  computation, we analyze a specific node to see if it's a member of
 *  a sibling list for its parent. This works even when that specific
 *  exemplar has just 1 child such as tree (typeArguments typeArgument).
 *  Long lists are sometimes treated differently than short lists, such as
 *  formal arg lists in Java. Sometimes they are split across lines.
 *
 *  I'm tracking separators (and treat terminators as separators).
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

	/** Map (parent:alt,tokentype) -> childMemberClassType if tokentype is separator in
	 *  a (parent:alt,child:alt) list.
	 */
	public Map<Triple<Integer,Integer,Integer>, Class<? extends ParserRuleContext>> listSeparators = new HashMap<>();

	CodeBuffTokenStream tokens;

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

					int form = -1;
					ParseTree between = ctx.getChild(firstIndex+1);
					if ( between instanceof TerminalNode ) { // is it a token?
						Token separator = ((TerminalNode)between).getSymbol();
						Triple<Integer, Integer, Integer> key =
							new Triple<>(ctx.getRuleIndex(), ctx.getAltNumber(), separator.getType());
						// map (parent:alt,tokentype) -> (child:alt) so we can create look up key for listInfo later if we want
						listSeparators.put(key, first.getClass());
						List<Token> hiddenToLeft = tokens.getHiddenTokensToLeft(separator.getTokenIndex());
						List<Token> hiddenToRight = tokens.getHiddenTokensToRight(separator.getTokenIndex());
						if ( hiddenToLeft!=null ) {
							Token left = hiddenToLeft.get(0);
							String hiddenText = left.getText();
							if ( Tool.count(hiddenText, '\n')>0 ) {
								form = CollectFeatures.listform(true, false);
								System.out.println("BEFORE "+JavaParser.ruleNames[ctx.getRuleIndex()]+
									                   "->"+JavaParser.ruleNames[ctx.getRuleIndex()]+" sep "+
									                   JavaParser.tokenNames[separator.getType()]+
									                   " "+separator);
							}
						}
						else if ( hiddenToRight!=null ) {
							Token right = hiddenToRight.get(0);
							String hiddenText = right.getText();
							if ( Tool.count(hiddenText, '\n')>0 ) {
								form = CollectFeatures.listform(false, true);
								System.out.println("AFTER "+JavaParser.ruleNames[ctx.getRuleIndex()]+
									                   "->"+JavaParser.ruleNames[ctx.getRuleIndex()]+" sep "+
									                   JavaParser.tokenNames[separator.getType()]+
									                   " "+separator);
							}
						}

						// now track length of parent:alt,child:alt list or split-list
						ParentSiblingListKey pair = new ParentSiblingListKey(ctx, first, separator.getType());
						List<Integer> lens;
						if ( form==-1 ) {
							lens = listInfo.get(pair);
							if ( lens==null ) {
								lens = new ArrayList<>();
								listInfo.put(pair, lens);
							}
						}
						else {
							lens = splitListInfo.get(pair);
							if ( lens==null ) {
								lens = new ArrayList<>();
								splitListInfo.put(pair, lens);
							}
							List<Integer> forms = splitListForm.get(pair);
							if ( forms==null ) {
								forms = new ArrayList<>();
								splitListForm.put(pair, forms);
							}
							forms.add(form); // track where we put newlines for this list
						}
						lens.add(CollectFeatures.getSiblingsLength(siblings));
					}
				}
			}
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

	public Map<Triple<Integer,Integer,Integer>, Class<? extends ParserRuleContext>> getListSeparators() {
		return listSeparators;
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
