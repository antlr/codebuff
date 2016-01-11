package org.antlr.groom;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class CollectFeatures extends JavaBaseListener {
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected List<int[]> data = new ArrayList<>();

	public CollectFeatures(CommonTokenStream tokens) {
		this.tokens = tokens;
	}

	public List<int[]> getData() {
		return data;
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		Token curToken = node.getSymbol();
		if ( curToken.getType()==Token.EOF ) return;

		int i = curToken.getTokenIndex();
		Token prevToken = null;
		boolean precedingNL = false;
		if (i>=1) {
			prevToken = tokens.get(i-1);
			precedingNL = curToken.getLine() > prevToken.getLine();
		}

		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		int ruleIndex = parent.getRuleIndex();
		String ruleName = JavaParser.ruleNames[ruleIndex];
		ParserRuleContext earliestAncestor = earliestAncestorStartingAtToken(parent, curToken);
		int earliestAncestorRuleIndex = Integer.MAX_VALUE;
		int earliestAncestorWidth = 0;
		if ( earliestAncestor!=null ) {
			earliestAncestorRuleIndex = earliestAncestor.getRuleIndex();
			earliestAncestorWidth = earliestAncestor.stop.getStopIndex()-earliestAncestor.start.getStartIndex()+1;
		}

		int[] features;
		if ( prevToken!=null ) {
			int endofprevtoken = prevToken.getCharPositionInLine()+prevToken.getText().length()-1;
			features = new int[]{
				precedingNL ? 1 : 0,
				curToken.getType(), curToken.getCharPositionInLine(), curToken.getText().length(),
				ruleIndex, earliestAncestorRuleIndex, earliestAncestorWidth,
				prevToken.getType(), prevToken.getCharPositionInLine(), endofprevtoken
			};
		}
		else {
			features = new int[] {
				precedingNL ? 1 : 0,
				curToken.getType(), curToken.getCharPositionInLine(), curToken.getText().length(),
				ruleIndex, earliestAncestorRuleIndex, earliestAncestorWidth,
				0, Integer.MAX_VALUE, 0
			};
		}
		data.add(features);
	}

	/** Walk upwards from node while p.start == token */
	public ParserRuleContext earliestAncestorStartingAtToken(ParseTree node, Token token) {
		ParseTree p = node;
		ParseTree prev = null;
		while (p!=null && p.getPayload()==token) {
			prev = p;
			p = p.getParent();
		}
		return (ParserRuleContext)prev;
	}
}
