package org.antlr.groom;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class CollectFeatures extends JavaBaseListener {
	public static final String[] FEATURE_NAMES = {
		"inject newline", // predicted var
		"prev^2 type", "prev^2 column",
		"prev type", "prev column",
		"type", "earliest ancestor rule", "earliest ancestor width",
		"next type",
	};

	public static final boolean[] CATEGORICAL = {
		true, // predicted var
		true, false,
		true, false,
		true, true, false,
		true
	};

	protected Parser parser; // track stream so we can examine previous tokens
	protected List<int[]> data = new ArrayList<>();

	public CollectFeatures(Parser parser) {
		this.parser = parser;
	}

	public List<int[]> getData() {
		return data;
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		Token curToken = node.getSymbol();
		if ( curToken.getType()==Token.EOF ) return;

		int i = curToken.getTokenIndex();
		if ( i<2 ) {
			return; // we need 2 previous tokens and current token
		}

		// Get a 4-gram of tokens with current token in 3rd position
		CommonTokenStream tokens = (CommonTokenStream)parser.getTokenStream();
		List<Token> window = tokens.getTokens(i - 2, i + 1);
		Token prevToken = window.get(1);

		boolean precedingNL = curToken.getLine() > prevToken.getLine();

		// Get context information
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		ParserRuleContext earliestAncestor = earliestAncestorStartingAtToken(parent, curToken);
		int earliestAncestorRuleIndex = earliestAncestor.getRuleIndex();
		int earliestAncestorWidth = earliestAncestor.stop.getStopIndex()-earliestAncestor.start.getStartIndex()+1;
		String earliestAncestorRuleName = JavaParser.ruleNames[earliestAncestorRuleIndex];

		int[] features;
		features = new int[] {
			precedingNL ? 1 : 0,
			window.get(0).getType(), window.get(0).getCharPositionInLine(),
			window.get(1).getType(), window.get(1).getCharPositionInLine(),
			window.get(2).getType(), earliestAncestorRuleIndex, earliestAncestorWidth,
			window.get(3).getType(),
		};
		data.add(features);
	}

	/** Walk upwards from node while p.start == token */
	public ParserRuleContext earliestAncestorStartingAtToken(ParserRuleContext node, Token token) {
		ParserRuleContext p = node;
		ParserRuleContext prev = null;
		while (p!=null && p.getPayload()==token) {
			prev = p;
			p = p.getParent();
		}
		if ( prev==null ) return node;
		return prev;
	}
}
