package org.antlr.codebuff;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectSiblingLists implements ParseTreeListener {
	public Map<Pair<Integer,Integer>,Pair<Integer,Integer>> ruleToChildListName = new HashMap<>();

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		// am I a child that is part of a list?
		Class<? extends ParserRuleContext> myClass = ctx.getClass();
		ParserRuleContext parent = ctx.getParent();
		if ( parent!=null ) {
			List<? extends ParserRuleContext> siblings = parent.getRuleContexts(myClass);
			if ( siblings.size()>1 ) {
				Pair<Integer, Integer> key = new Pair<>(parent.getRuleIndex(), parent.getAltNumber());
				Pair<Integer, Integer> value = new Pair<>(ctx.getRuleIndex(), ctx.getAltNumber());
				ruleToChildListName.put(key, value);
			}
		}
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
