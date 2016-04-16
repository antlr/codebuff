package org.antlr.codebuff;

import org.antlr.codebuff.misc.Quad;
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
import java.util.List;
import java.util.Map;

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
	/** Track set of (parent:alt,child:alt) pairs and their min,median,max */
	public Map<Quad<Integer,Integer,Integer,Integer>, List<Integer>> listInfo = new HashMap<>();

	/** Map (parent:alt,tokentype) -> childMemberClassType if tokentype is separator in
	 *  a (parent:alt,child:alt) list.
	 */
	public Map<Triple<Integer,Integer,Integer>, Class<? extends ParserRuleContext>> listSeparators = new HashMap<>();

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		// am I a child that is part of a list?
		Class<? extends ParserRuleContext> myClass = ctx.getClass();
		ParserRuleContext parent = ctx.getParent();
		if ( parent!=null ) {
			List<? extends ParserRuleContext> siblings = parent.getRuleContexts(myClass);
			if ( siblings.size()>1 ) {
				Quad<Integer, Integer, Integer, Integer> pair = new Quad<>(
					parent.getRuleIndex(), parent.getAltNumber(),
					ctx.getRuleIndex(), ctx.getAltNumber()
				);
				List<Integer> lens = listInfo.get(pair);
				if ( lens==null ) {
					lens = new ArrayList<>();
					listInfo.put(pair, lens);
				}
				lens.add(CollectFeatures.getSiblingsLength(siblings));

				// check for separator by looking between first two siblings
				ParserRuleContext first = siblings.get(0);
				ParserRuleContext second = siblings.get(1);

				List<Tree> children = Trees.getChildren(parent);
				int firstIndex = children.indexOf(first);
				int secondIndex = children.indexOf(second);

				if ( firstIndex+1 < secondIndex ) { // is there something in between first and second?
					ParseTree between = parent.getChild(firstIndex+1);
					if ( between instanceof TerminalNode ) { // is it a token?
						Token separator = ((TerminalNode)between).getSymbol();
						Triple<Integer, Integer, Integer> key =
							new Triple<>(parent.getRuleIndex(), parent.getAltNumber(), separator.getType());
						// map (parent:alt,tokentype) -> (child:alt) so we can create look up key for listInfo later if we want
						listSeparators.put(key, first.getClass());
					}
				}
			}
		}
	}

	public Map<Quad<Integer, Integer, Integer, Integer>, Triple<Integer,Integer,Integer>> getListSizeMedians() {
		Map<Quad<Integer,Integer,Integer,Integer>, Triple<Integer,Integer,Integer>> listSizes = new HashMap<>();
		for (Quad<Integer, Integer, Integer, Integer> pair : listInfo.keySet()) {
			List<Integer> lens = listInfo.get(pair);
			Collections.sort(lens);
			int n = lens.size();
			Integer min = lens.get(0);
			Integer median = lens.get(n/2);
			Integer max = lens.get(n-1);
			listSizes.put(pair, new Triple<>(min, median, max));
		}
		return listSizes;
	}

	public Map<Triple<Integer,Integer,Integer>, Class<? extends ParserRuleContext>> getListSeparators() {
		return listSeparators;
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
