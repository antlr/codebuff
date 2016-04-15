package org.antlr.codebuff;

import org.antlr.codebuff.misc.Quad;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Find subtree roots with repeated child subtrees such as typeArguments
 *  has more than 1 typeArgument child.  For an actual feature vector
 *  computation, we analyze a specific node to see if it's a member of
 *  a sibling list for its parent. This works even when that specific
 *  exemplar has just 1 child such as tree (typeArguments typeArgument).
 *  Long lists are sometimes treated differently than short lists, such as
 *  formal arg lists in Java. Sometimes they are split across lines.
 *
 *  Currently, I'm not tracking any separator or terminators.
 */
public class CollectSiblingLists implements ParseTreeListener {
	/** Track set of (parent:alt,child:alt) pairs and their min,median,max */
	public Map<Quad<Integer,Integer,Integer,Integer>, List<Integer>> listLength = new HashMap<>();

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
				List<Integer> lens = listLength.get(pair);
				if ( lens==null ) {
					lens = new ArrayList<>();
					listLength.put(pair, lens);
				}
				lens.add(CollectFeatures.getSiblingsLength(siblings));
			}
		}
	}

	public Map<Quad<Integer, Integer, Integer, Integer>, Triple<Integer,Integer,Integer>> getListSizeMedians() {
		Map<Quad<Integer,Integer,Integer,Integer>, Triple<Integer,Integer,Integer>> listSizes = new HashMap<>();
		for (Quad<Integer, Integer, Integer, Integer> pair : listLength.keySet()) {
			List<Integer> lens = listLength.get(pair);
			Collections.sort(lens);
			int n = lens.size();
			Integer min = lens.get(0);
			Integer median = lens.get(n/2);
			Integer max = lens.get(n-1);
			listSizes.put(pair, new Triple<>(lens.get(0), median, max));
		}
		return listSizes;
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
