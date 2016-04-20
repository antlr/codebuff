package org.antlr.codebuff;

import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;

import java.util.HashSet;
import java.util.List;
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

	public List<Tree> getSeparators(ParserRuleContext ctx, List<? extends ParserRuleContext> siblings) {
		ParserRuleContext first = siblings.get(0);
		ParserRuleContext last = siblings.get(siblings.size()-1);
		int start = BuffUtils.indexOf(ctx, first);
		int end = BuffUtils.indexOf(ctx, last);
		List<Tree> elements = Trees.getChildren(ctx).subList(start, end+1);
		return BuffUtils.filter(elements, c -> c instanceof TerminalNode);
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
