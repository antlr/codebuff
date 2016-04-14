package org.antlr.codebuff;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Find subtree roots with repeated child subtrees such as typeArguments
 *  has more than 1 typeArgument child.  For an actual feature vector
 *  computation, we analyze a specific node to see if it's a member of
 *  a sibling list for its parent. This works even when that specific
 *  exemplar has just 1 child such as tree (typeArguments typeArgument).
 *  Long lists are sometimes treated differently than short lists, such as
 *  formal arg lists in Java. Sometimes they are split across lines.
 *
 *  Currently, I'm not tracking any separator or terminators.
 *
 *  Warning: can only track ONE sibling list per root.
 */
public class CollectSiblingLists implements ParseTreeListener {
	/** Track set of (parent:alt,child:alt) pairs */
	public Set<int[]> rootAndChildListPairs = new HashSet<>();

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		// am I a child that is part of a list?
		Class<? extends ParserRuleContext> myClass = ctx.getClass();
		ParserRuleContext parent = ctx.getParent();
		if ( parent!=null ) {
			List<? extends ParserRuleContext> siblings = parent.getRuleContexts(myClass);
			if ( siblings.size()>1 ) {
				rootAndChildListPairs.add(
					new int[] {
						parent.getRuleIndex(), parent.getAltNumber(),
						ctx.getRuleIndex(), ctx.getAltNumber()
					}
				                         );
			}
		}
	}

	public Set<int[]> getRootAndChildListPairs() {
		return rootAndChildListPairs;
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
