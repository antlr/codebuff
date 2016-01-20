package org.antlr.groom;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
NOTE: I think multiple newlines are counted as one during training.
 */
public class CollectFeatures extends JavaBaseListener {
	public static final int INDEX_PREV2_TYPE        = 0;
	public static final int INDEX_PREV_TYPE         = 1;
	public static final int INDEX_PREV_END_COLUMN   = 2;
	public static final int INDEX_TYPE              = 3;
	public static final int INDEX_EARLIEST_ANCESTOR = 4;
	public static final int INDEX_ANCESTOR_WIDTH    = 5;
	public static final int INDEX_NEXT_TYPE         = 6;

	public static final String[] FEATURE_NAMES = {
		"prev^2 type",
		"prev type", "prev end column",
		"type", "earliest ancestor rule", "earliest ancestor width",
		"next type",
	};

	public static final boolean[] CATEGORICAL = {
		true,
		true, false,
		true, true, false,
		true
	};

	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected List<int[]> features = new ArrayList<>();
	protected List<Integer> injectNewlines = new ArrayList<>();
	protected List<Integer> injectWS = new ArrayList<>();

	public CollectFeatures(CommonTokenStream tokens) {
		this.tokens = tokens;
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		Token curToken = node.getSymbol();
		if ( curToken.getType()==Token.EOF ) return;

		int i = curToken.getTokenIndex();
		tokens.seek(i); // see so that LT(1) is tokens.get(i);
		if ( tokens.LT(-2)==null ) { // do we have 2 previous tokens?
			return;
		}

		int[] features = getNodeFeatures(tokens, node);
		Token prevToken = tokens.LT(-1);
		boolean precedingNL = curToken.getLine() > prevToken.getLine();

//		System.out.printf("%5s: ", precedingNL);
//		System.out.printf("%s\n", Tool.toString(features));
		this.injectNewlines.add(precedingNL ? 1 : 0);
		int ws = curToken.getCharPositionInLine() -
			(prevToken.getCharPositionInLine()+prevToken.getText().length());

		this.injectWS.add(ws); // likely negative if precedingNL
		this.features.add(features);
	}

	/** Walk upwards from node while p.start == token */
	public static ParserRuleContext earliestAncestorStartingAtToken(ParserRuleContext node, Token token) {
		ParserRuleContext p = node;
		ParserRuleContext prev = null;
		while (p!=null && p.getPayload()==token) {
			prev = p;
			p = p.getParent();
		}
		if ( prev==null ) return node;
		return prev;
	}

	public static int[] getNodeFeatures(CommonTokenStream tokens, TerminalNode node) {
		Token curToken = node.getSymbol();
//		if ( curToken.getType()==Token.EOF ) return null;

		int i = curToken.getTokenIndex();
		tokens.seek(i); // see so that LT(1) is tokens.get(i);
//		if ( tokens.LT(-2)==null ) { // do we have 2 previous tokens?
//			return null;
//		}

		// Get a 4-gram of tokens with current token in 3rd position
		List<Token> window =
			Arrays.asList(tokens.LT(-2), tokens.LT(-1), tokens.LT(1), tokens.LT(2));

		// Get context information
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		ParserRuleContext earliestAncestor = earliestAncestorStartingAtToken(parent, curToken);
		int earliestAncestorRuleIndex = earliestAncestor.getRuleIndex();
		int earliestAncestorWidth = earliestAncestor.stop.getStopIndex()-earliestAncestor.start.getStartIndex()+1;
		int prevTokenEndCharPos = window.get(1).getCharPositionInLine() + window.get(1).getText().length();

		return new int[] {
			window.get(0).getType(),
			window.get(1).getType(), prevTokenEndCharPos,
			window.get(2).getType(), earliestAncestorRuleIndex, earliestAncestorWidth,
			window.get(3).getType(),
		};
	}

//	public static Token getPreviousToken(CommonTokenStream tokens, int tokIndex) {
//		for (int i=tokIndex-1; i>=0; i--) {
//			Token t = tokens.get(i);
//			if ( t.getChannel()==Token.DEFAULT_CHANNEL ) {
//				return t;
//			}
//		}
//		return null; // no such token
//	}

	public List<int[]> getFeatures() {
		return features;
	}

	public List<Integer> getInjectNewlines() {
		return injectNewlines;
	}

	public List<Integer> getInjectWS() {
		return injectWS;
	}
}
