package org.antlr.codebuff;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

	protected ParserRuleContext root;
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected List<int[]> features = new ArrayList<>();
	protected List<Integer> injectNewlines = new ArrayList<>();
	protected List<Integer> injectWS = new ArrayList<>();
	protected List<Integer> indent = new ArrayList<>();
	/** steps to common ancestor whose first token is alignment anchor */
	protected List<Integer> levelsToCommonAncestor = new ArrayList<>();
	protected Token firstTokenOnLine = null;

	protected int tabSize;

	public CollectFeatures(ParserRuleContext root, CommonTokenStream tokens, int tabSize) {
		this.root = root;
		this.tokens = tokens;
		this.tabSize = tabSize;
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		Token curToken = node.getSymbol();
		if ( curToken.getType()==Token.EOF ) return;

		int i = curToken.getTokenIndex();
		if ( Tool.getNumberRealTokens(tokens, 0, i-1)<2 ) return;

		tokens.seek(i); // see so that LT(1) is tokens.get(i);
		Token prevToken = tokens.LT(-1);

		// find number of blank lines
		int[] features = getNodeFeatures(tokens, node, tabSize);

		int precedingNL = 0; // how many lines to inject
		if ( curToken.getLine() > prevToken.getLine() ) { // a newline must be injected
			List<Token> wsTokensBeforeCurrentToken = tokens.getHiddenTokensToLeft(i);
			for (Token t : wsTokensBeforeCurrentToken) {
				precedingNL += Tool.count(t.getText(), '\n');
			}
		}

//		System.out.printf("%5s: ", precedingNL);
//		System.out.printf("%s\n", Tool.toString(features));
		this.injectNewlines.add(precedingNL);

		int columnDelta = 0;
		int ws = 0;
		int levelsToCommonAncestor = 0;
		if ( precedingNL>0 ) {
			if ( firstTokenOnLine!=null ) {
				columnDelta = curToken.getCharPositionInLine() - firstTokenOnLine.getCharPositionInLine();
			}
			firstTokenOnLine = curToken;
			ParserRuleContext commonAncestor = getFirstTokenOfCommonAncestor(root, tokens, i, tabSize);
			List<? extends Tree> ancestors = Trees.getAncestors(node);
			Collections.reverse(ancestors);
			levelsToCommonAncestor = ancestors.indexOf(commonAncestor);
		}
		else {
			ws = curToken.getCharPositionInLine() -
				(prevToken.getCharPositionInLine()+prevToken.getText().length());
		}

		this.indent.add(columnDelta);

		this.injectWS.add(ws); // likely negative if precedingNL

		this.levelsToCommonAncestor.add(levelsToCommonAncestor);

		this.features.add(features);
	}

	/** Return number of steps to common ancestor whose first token is alignment anchor.
	 *  Return null if no such common ancestor.
	 */
	public static ParserRuleContext getFirstTokenOfCommonAncestor(
		ParserRuleContext root,
		CommonTokenStream tokens,
		int tokIndex,
		int tabSize)
	{
		List<Token> tokensOnPreviousLine = getTokensOnPreviousLine(tokens, tokIndex);
		// look for alignment
		if ( tokensOnPreviousLine.size()>0 ) {
			Token curToken = tokens.get(tokIndex);
			Token alignedToken = findAlignedToken(tokensOnPreviousLine, curToken);
			tokens.seek(tokIndex); // seek so that LT(1) is tokens.get(i);
			Token prevToken = tokens.LT(-1);
			int prevIndent = tokensOnPreviousLine.get(0).getCharPositionInLine();
			int curIndent = curToken.getCharPositionInLine();
			boolean tabbed = curIndent>prevIndent && curIndent%tabSize==0;
			boolean precedingNL = curToken.getLine()>prevToken.getLine();
			if ( precedingNL &&
				alignedToken!=null &&
				alignedToken!=tokensOnPreviousLine.get(0) &&
				!tabbed ) {
				// if cur token is on new line and it lines up and it's not left edge,
				// it's alignment not 0 indent
//				printAlignment(tokens, curToken, tokensOnPreviousLine, alignedToken);
				ParserRuleContext commonAncestor = Trees.getRootOfSubtreeEnclosingRegion(root, alignedToken.getTokenIndex(), curToken.getTokenIndex());
//				System.out.println("common ancestor: "+JavaParser.ruleNames[commonAncestor.getRuleIndex()]);
				if ( commonAncestor.getStart()==alignedToken ) {
					// aligned with first token of common ancestor
					return commonAncestor;
				}
			}
		}
		return null;
	}

	/** Walk upwards from node while p.start == token */
	public static ParserRuleContext earliestAncestorStartingAtToken(ParserRuleContext node, Token token) {
		ParserRuleContext p = node;
		ParserRuleContext prev = null;
		while (p!=null && p.getStart()==token) {
			prev = p;
			p = p.getParent();
		}
		if ( prev==null ) return node;
		return prev;
	}

	public static ParserRuleContext deepestCommonAncestor(ParserRuleContext t1, ParserRuleContext t2) {
		if ( t1==t2 ) return t1;
		List<? extends Tree> t1_ancestors = Trees.getAncestors(t1);
		List<? extends Tree> t2_ancestors = Trees.getAncestors(t2);
		// first ancestor of t2 that matches an ancestor of t1 is the deepest common ancestor
		for (Tree t : t1_ancestors) {
			int i = t2_ancestors.indexOf(t);
			if ( i>=0 ) {
				return (ParserRuleContext)t2_ancestors.get(i);
			}
		}
		return null;
	}

	public static int[] getNodeFeatures(CommonTokenStream tokens, TerminalNode node, int tabSize) {
		Token curToken = node.getSymbol();

		int i = curToken.getTokenIndex();
		tokens.seek(i); // seek so that LT(1) is tokens.get(i);

		// Get a 4-gram of tokens with current token in 3rd position
		List<Token> window =
			Arrays.asList(tokens.LT(-2), tokens.LT(-1), tokens.LT(1), tokens.LT(2));

		// Get context information
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		ParserRuleContext earliestAncestor = earliestAncestorStartingAtToken(parent, curToken);
		int earliestAncestorRuleIndex = earliestAncestor.getRuleIndex();
		int earliestAncestorWidth = earliestAncestor.stop.getStopIndex()-earliestAncestor.start.getStartIndex()+1;
		int prevTokenEndCharPos = window.get(1).getCharPositionInLine() + window.get(1).getText().length();

		int[] features = {
			window.get(0).getType(),
			window.get(1).getType(), prevTokenEndCharPos,
			window.get(2).getType(), earliestAncestorRuleIndex, earliestAncestorWidth,
			window.get(3).getType(),
		};
//		System.out.print(curToken+": "+CodekNNClassifier._toString(features));
		return features;
	}

	public static Token findAlignedToken(List<Token> tokens, Token leftEdgeToken) {
		for (Token t : tokens) {
			if ( t.getCharPositionInLine() == leftEdgeToken.getCharPositionInLine() ) {
				return t;
			}
		}
		return null;
	}

	/** Search backwards from tokIndex into 'tokens' stream and get all on-channel
	 *  tokens on previous line with respect to token at tokIndex.
	 *  return empty list if none found. First token in returned list is
	 *  the first token on the line.
	 */
	public static List<Token> getTokensOnPreviousLine(CommonTokenStream tokens, int tokIndex) {
		// first find previous line by looking for real token on line < tokens.get(i)
		Token curToken = tokens.get(tokIndex);
		int curLine = curToken.getLine();
		int prevLine = 0;
		for (int i=tokIndex-1; i>=0; i--) {
			Token t = tokens.get(i);
			if ( t.getChannel()==Token.DEFAULT_CHANNEL && t.getLine()<curLine ) {
				prevLine = t.getLine();
				tokIndex = i; // start collecting at this index
				break;
			}
		}

		// Now collect the on-channel real tokens for this line
		List<Token> online = new ArrayList<>();
		for (int i=tokIndex; i>=0; i--) {
			Token t = tokens.get(i);
			if ( t.getLine()<prevLine ) break; // found last token on that previous line
			if ( t.getChannel()==Token.DEFAULT_CHANNEL && t.getLine()==prevLine ) {
				online.add(t);
			}
		}
		Collections.reverse(online);
		return online;
	}

	public static void printAlignment(CommonTokenStream tokens, Token curToken, List<Token> tokensOnPreviousLine, Token alignedToken) {
		int alignedCol = alignedToken.getCharPositionInLine();
		int indent = tokensOnPreviousLine.get(0).getCharPositionInLine();
		int first = tokensOnPreviousLine.get(0).getTokenIndex();
		int last = tokensOnPreviousLine.get(tokensOnPreviousLine.size()-1).getTokenIndex();
		System.out.println(Tool.spaces(alignedCol-indent)+"\u2193");
		for (int j=first; j<=last; j++) {
			System.out.print(tokens.get(j).getText());
		}
		System.out.println();
		System.out.println(Tool.spaces(alignedCol-indent)+curToken.getText());
	}

	public List<int[]> getFeatures() {
		return features;
	}

	public List<Integer> getInjectNewlines() {
		return injectNewlines;
	}

	public List<Integer> getInjectWS() {
		return injectWS;
	}

	public List<Integer> getLevelsToCommonAncestor() {
		return levelsToCommonAncestor;
	}

	public List<Integer> getIndent() {
		return indent;
	}
}
