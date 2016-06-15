package org.antlr.codebuff.walkers;

import org.antlr.codebuff.Trainer;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class VisitSiblingLists implements ParseTreeListener {
	public void enterEveryRule(ParserRuleContext ctx) {
		// Find sibling lists that are children of this parent node
		Set<Class> completed = new HashSet<>(); // only count sibling list for each subtree type once
		for (int i = 0; i<ctx.getChildCount(); i++) {
			ParseTree child = ctx.getChild(i);

			if ( completed.contains(child.getClass()) ) continue; // avoid counting repeatedly
			completed.add(child.getClass());
			if ( child instanceof TerminalNode ) continue; // tokens are separators at most not siblings

			// found subtree child
			List<? extends ParserRuleContext> siblings =
				ctx.getRuleContexts(((ParserRuleContext) child).getClass());
			if ( siblings.size()>1 ) { // we found a list
				// check for separator by looking between first two siblings (assume all are same)
				ParserRuleContext first = siblings.get(0);
				ParserRuleContext second = siblings.get(1);
				List<Tree> children = Trees.getChildren(ctx);

				int firstIndex = children.indexOf(first);
				int secondIndex = children.indexOf(second);

				if ( firstIndex+1 == secondIndex ) continue; // nothing between first and second so no separator

				ParseTree between = ctx.getChild(firstIndex+1);
				if ( between instanceof TerminalNode ) { // is it a token?
					Token separator = ((TerminalNode) between).getSymbol();
					visitNonSingletonWithSeparator(ctx, siblings, separator);
				}
			}
		}
	}

	public abstract void visitNonSingletonWithSeparator(ParserRuleContext ctx,
	                                                    List<? extends ParserRuleContext> siblings,
	                                                    Token separator);

	public static List<Tree> getSeparators(ParserRuleContext ctx, List<? extends ParserRuleContext> siblings) {
		ParserRuleContext first = siblings.get(0);
		ParserRuleContext last = siblings.get(siblings.size()-1);
		int start = BuffUtils.indexOf(ctx, first);
		int end = BuffUtils.indexOf(ctx, last);
		List<Tree> elements = Trees.getChildren(ctx).subList(start, end+1);
		return BuffUtils.filter(elements, c -> c instanceof TerminalNode);
	}

	/** Return map for the various tokens related to this list re list membership */
	public static Map<Token,Pair<Boolean,Integer>> getInfoAboutListTokens(ParserRuleContext ctx,
	                                                                      CodeBuffTokenStream tokens,
	                                                                      Map<Token, TerminalNode> tokenToNodeMap,
	                                                                      List<? extends ParserRuleContext> siblings,
	                                                                      boolean isOversizeList)
	{
		Map<Token,Pair<Boolean,Integer>> tokenToListInfo = new HashMap<>();

		ParserRuleContext first = siblings.get(0);
		ParserRuleContext last = siblings.get(siblings.size()-1);

		Token prefixToken = tokens.getPreviousRealToken(first.getStart().getTokenIndex()); // e.g., '(' in an arg list or ':' in grammar def
		Token suffixToken = tokens.getNextRealToken(last.getStop().getTokenIndex());       // e.g., LT(1) is last token of list; LT(2) is ')' in an arg list of ';' in grammar def

		TerminalNode prefixNode = tokenToNodeMap.get(prefixToken);
		TerminalNode suffixNode = tokenToNodeMap.get(suffixToken);
		boolean hasSurroundingTokens =
			prefixNode!=null && prefixNode.getParent() == suffixNode.getParent();

		if ( hasSurroundingTokens ) {
			tokenToListInfo.put(prefixToken, new Pair<>(isOversizeList, Trainer.LIST_PREFIX));
			tokenToListInfo.put(suffixToken, new Pair<>(isOversizeList, Trainer.LIST_SUFFIX));
		}

		List<Tree> separators = getSeparators(ctx, siblings);
		Tree firstSep = separators.get(0);
		tokenToListInfo.put((Token)firstSep.getPayload(), new Pair<>(isOversizeList, Trainer.LIST_FIRST_SEPARATOR));
		for (Tree s : separators.subList(1,separators.size())) {
			tokenToListInfo.put((Token)s.getPayload(), new Pair<>(isOversizeList, Trainer.LIST_SEPARATOR));
		}

		// handle sibling members
		tokenToListInfo.put(first.getStart(), new Pair<>(isOversizeList, Trainer.LIST_FIRST_ELEMENT));
		for (ParserRuleContext s : siblings.subList(1,siblings.size())) {
			tokenToListInfo.put(s.getStart(), new Pair<>(isOversizeList, Trainer.LIST_MEMBER));
		}

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
