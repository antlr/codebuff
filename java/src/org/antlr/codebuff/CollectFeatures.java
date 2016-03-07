package org.antlr.codebuff;

import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectFeatures {
	public static final double MAX_CONTEXT_DIFF_THRESHOLD = 0.15;

	public static final int INDEX_PREV2_TYPE        = 0;
	public static final int INDEX_PREV_TYPE         = 1;
	public static final int INDEX_PREV_RULE         = 2; // what rule is prev token in?
	public static final int INDEX_PREV_END_COLUMN   = 3;
	public static final int INDEX_PREV_EARLIEST_ANCESTOR = 4;
	public static final int INDEX_PREV_ANCESTOR_WIDTH = 5;
	public static final int INDEX_TYPE              = 6;
	public static final int INDEX_MATCHING_TOKEN_DIFF_LINE = 7;
	public static final int INDEX_RULE              = 8; // what rule are we in?
	public static final int INDEX_EARLIEST_ANCESTOR = 9;
	public static final int INDEX_ANCESTOR_WIDTH    = 10;
	public static final int INDEX_SUM_ENDCOL_ANCESTOR_WIDTH = 11;
	public static final int INDEX_NEXT_TYPE         = 12;
	public static final int INDEX_INFO_FILE         = 13;
	public static final int INDEX_INFO_LINE         = 14;
	public static final int INDEX_INFO_CHARPOS      = 15;

	public static FeatureMetaData[] FEATURES = {
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-2)"}, 1),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "rule"}, 2),
		new FeatureMetaData(FeatureType.INT,   new String[] {"LT(-1)", "end col"}, 0),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "right ancestor"}, 3),
		new FeatureMetaData(FeatureType.INT,   new String[] {"ancest.", "width"}, 0),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 2),
		new FeatureMetaData(FeatureType.BOOL,   new String[] {"Pair", "diff\\n"}, 3),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "rule"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "left ancestor"}, 3),
		new FeatureMetaData(FeatureType.INT,   new String[] {"ancest.", "width"}, 0),
		new FeatureMetaData(FeatureType.COL,   new String[] {"endcol+", "width"}, 3),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(2)"}, 1),
		new FeatureMetaData(FeatureType.INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(FeatureType.INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public static int getCurrentTokenType(int[] features) { return features[INDEX_TYPE]; }
	public static int getInfoLine(int[] features) { return features[INDEX_INFO_LINE]; }
	public static int getInfoCharPos(int[] features) { return features[INDEX_INFO_CHARPOS]; }

	public static final int MAX_L0_DISTANCE_COUNT;
	static {
		int n = 0;
		for (int i=0; i<FEATURES.length; i++) {
			n += FEATURES[i].mismatchCost;
		}
		MAX_L0_DISTANCE_COUNT = n;
	}

	protected InputDocument doc;
	protected ParserRuleContext root;
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected List<int[]> features = new ArrayList<>();
	protected List<Integer> injectNewlines = new ArrayList<>();
	protected List<Integer> injectWS = new ArrayList<>();
	protected List<Integer> indent = new ArrayList<>();
	/** steps to common ancestor whose first token is alignment anchor */
//	protected List<Integer> levelsToCommonAncestor = new ArrayList<>();
	protected List<Integer> alignWithPrevious = new ArrayList<>();

	protected Token firstTokenOnLine = null;

	protected int currentIndent = 0;

	protected Map<Token, TerminalNode> tokenToNodeMap = null;

	protected int tabSize;

	protected static Map<String, List<Pair<Integer, Integer>>> ruleToPairsBag = null;

	public CollectFeatures(InputDocument doc, int tabSize, Map<String, List<Pair<Integer, Integer>>> ruleToPairs) {
		this.doc = doc;
		this.root = doc.tree;
		this.tokens = doc.tokens;
		this.tabSize = tabSize;
		ruleToPairsBag = ruleToPairs;
	}

	public void computeFeatureVectors() {
		List<Token> realTokens = getRealTokens(tokens);
		firstTokenOnLine = realTokens.get(0); // init to first token of file
		for (int i = 2; i<realTokens.size(); i++) { // can't process first 2 tokens
			int tokenIndexInStream = realTokens.get(i).getTokenIndex();
			computeFeatureVectorForToken(tokenIndexInStream);
		}
	}

	public void computeFeatureVectorForToken(int i) {
		if ( tokenToNodeMap == null ) {
			tokenToNodeMap = indexTree(root);
		}

		Token curToken = tokens.get(i);
		if ( curToken.getType()==Token.EOF ) return;

		tokens.seek(i); // seek so that LT(1) is tokens.get(i);
		Token prevToken = tokens.LT(-1);

		int[] features = getNodeFeatures(tokenToNodeMap, doc, i, curToken.getLine(), tabSize);

		int precedingNL = 0; // how many lines to inject
		if ( curToken.getLine() > prevToken.getLine() ) { // a newline must be injected
			List<Token> wsTokensBeforeCurrentToken = tokens.getHiddenTokensToLeft(i);
			for (Token t : wsTokensBeforeCurrentToken) {
				precedingNL += Tool.count(t.getText(), '\n');
			}
//			System.out.println("^^^"+(prevToken.getCharPositionInLine()+prevToken.getText().length()));
		}

		this.injectNewlines.add(precedingNL);

		TerminalNode node = tokenToNodeMap.get(tokens.get(i));
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		ParserRuleContext earliestAncestor = earliestAncestorStartingAtToken(parent, curToken);
		int aligned = 0;

		// at a newline, are we aligned with a prior sibling (in a list)?
		int columnDelta = 0;
		if ( precedingNL>0 && earliestAncestor!=null ) {
			ParserRuleContext commonAncestor = earliestAncestor.getParent();
			List<ParserRuleContext> siblings = commonAncestor.getRuleContexts(earliestAncestor.getClass());
			if ( siblings.size()>1 ) {
				ParserRuleContext firstSibling = siblings.get(0);
				Token firstSiblingStartToken = firstSibling.getStart();
				if ( firstSiblingStartToken!=curToken && // can't align with yourself
					firstSiblingStartToken.getCharPositionInLine() == curToken.getCharPositionInLine() )
				{
					aligned = 1;
//					System.out.println("aligned "+
//						                   doc.parser.getRuleNames()[commonAncestor.getRuleIndex()]+
//						                   " has "+siblings.size()+" "+doc.parser.getRuleNames()[earliestAncestor.getRuleIndex()]+" siblings");
				}
			}
		}

		if ( precedingNL>0 && aligned!=1 ) {
			columnDelta = curToken.getCharPositionInLine() - currentIndent;
			currentIndent = curToken.getCharPositionInLine();
//			System.out.println("set current indent at "+curToken+" to "+currentIndent);
		}

		int ws = 0;
		if ( precedingNL==0 ) {
			ws = curToken.getCharPositionInLine() -
				(prevToken.getCharPositionInLine()+prevToken.getText().length());
		}

		this.indent.add(columnDelta);

		this.injectWS.add(ws); // likely negative if precedingNL

		this.alignWithPrevious.add(aligned);

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

	/** Walk upwards from node while p.start == token; return null if there is
	 *  no ancestor starting at token.
	 */
	public static ParserRuleContext earliestAncestorStartingAtToken(ParserRuleContext node, Token token) {
		ParserRuleContext p = node;
		ParserRuleContext prev = null;
		while (p!=null && p.getStart()==token) {
			prev = p;
			p = p.getParent();
		}
		return prev;
	}

	/** Walk upwards from node while p.stop == token; return null if there is
	 *  no ancestor stopping at token.
	 */
	public static ParserRuleContext earliestAncestorStoppingAtToken(ParserRuleContext node, Token token) {
		ParserRuleContext p = node;
		ParserRuleContext prev = null;
		while (p!=null && p.getStop()==token) {
			prev = p;
			p = p.getParent();
		}
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

	public static int[] getNodeFeatures(Map<Token, TerminalNode> tokenToNodeMap,
	                                    InputDocument doc,
	                                    int i,
	                                    int line,
	                                    int tabSize)
	{
		CommonTokenStream tokens = doc.tokens;
		TerminalNode node = tokenToNodeMap.get(tokens.get(i));
		Token curToken = node.getSymbol();

		tokens.seek(i); // seek so that LT(1) is tokens.get(i);

		// Get a 4-gram of tokens with current token in 3rd position
		List<Token> window =
			Arrays.asList(tokens.LT(-2), tokens.LT(-1), tokens.LT(1), tokens.LT(2));

		// Get context information for previous token
		Token prevToken = tokens.LT(-1);
		TerminalNode prevTerminalNode = tokenToNodeMap.get(prevToken);
		ParserRuleContext parent = (ParserRuleContext)prevTerminalNode.getParent();
		int prevTokenRuleIndex = parent.getRuleIndex();
		ParserRuleContext earliestAncestor = earliestAncestorStoppingAtToken(parent, prevToken);
		int prevEarliestAncestorRuleIndex = -1;
		int prevEarliestAncestorWidth = -1;
		if ( earliestAncestor!=null ) {
			prevEarliestAncestorRuleIndex = earliestAncestor.getRuleIndex();
			prevEarliestAncestorWidth = earliestAncestor.stop.getStopIndex()-earliestAncestor.start.getStartIndex()+1;
		}

		// Get context information for current token
		parent = (ParserRuleContext)node.getParent();
		int curTokensParentRuleIndex = parent.getRuleIndex();
		earliestAncestor = earliestAncestorStartingAtToken(parent, curToken);
		int earliestAncestorRuleIndex = -1;
		int earliestAncestorWidth = -1;
		if ( earliestAncestor!=null ) {
			earliestAncestorRuleIndex = earliestAncestor.getRuleIndex();
			earliestAncestorWidth = earliestAncestor.stop.getStopIndex()-earliestAncestor.start.getStartIndex()+1;
		}
		int prevTokenEndCharPos = window.get(1).getCharPositionInLine() + window.get(1).getText().length();

		// matchingSymbolOnDiffLine
		// -1 means no pair exist
		// 0  means they are on the same line
		// 1  means they are on different lines
		int matchingSymbolOnDiffLine = getMatchingSymbolOnDiffLine(doc, node, line);

		int sumEndColAndAncestorWidth = -1;
		if ( earliestAncestorWidth>=0 ) {
			sumEndColAndAncestorWidth = prevTokenEndCharPos+earliestAncestorWidth;
		}
		int[] features = {
			window.get(0).getType(),

			window.get(1).getType(),
			prevTokenRuleIndex,
			prevTokenEndCharPos,
			prevEarliestAncestorRuleIndex,
			prevEarliestAncestorWidth,

			window.get(2).getType(), // LT(1)
			matchingSymbolOnDiffLine,
			curTokensParentRuleIndex,
			earliestAncestorRuleIndex,
			earliestAncestorWidth,
			sumEndColAndAncestorWidth,
			window.get(3).getType(),

			// info
			0, // file
			curToken.getLine(),
			curToken.getCharPositionInLine()
		};
//		System.out.print(curToken+": "+CodekNNClassifier._toString(features));
		return features;
	}

	private static int getMatchingSymbolOnDiffLine(InputDocument doc,
	                                               TerminalNode node,
	                                               int line)
	{
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		int curTokensParentRuleIndex = parent.getRuleIndex();
		Token curToken = node.getSymbol();
		int matchingSymbolOnDiffLine = -1;
		if (ruleToPairsBag != null) {
			String ruleName = JavaParser.ruleNames[curTokensParentRuleIndex];
			List<Pair<Integer, Integer>> pairs = ruleToPairsBag.get(ruleName);
			if ( pairs!=null ) {
				// Find appropriate pair given current token
				// If more than one pair (a,b) with b=current token pick first one
				// or if a common pair like ({,}), then give that one preference.
				List<Integer> viableMatchingLeftTokenTypes = viableLeftTokenTypes(parent, curToken, pairs);
				Vocabulary vocab = doc.parser.getVocabulary();
				if ( !viableMatchingLeftTokenTypes.isEmpty() ) {
					int matchingLeftTokenType =
						CollectTokenDependencies.getMatchingLeftTokenType(curToken, viableMatchingLeftTokenTypes, vocab);
					List<TerminalNode> matchingLeftNodes = parent.getTokens(matchingLeftTokenType);
					// get matching left node by getting last node to left of current token
					List<TerminalNode> nodesToLeftOfCurrentToken =
						BuffUtils.filter(matchingLeftNodes, n -> n.getSymbol().getTokenIndex()<curToken.getTokenIndex());
					TerminalNode matchingLeftNode = nodesToLeftOfCurrentToken.get(nodesToLeftOfCurrentToken.size()-1);

					if (matchingLeftNode != null) {
						int matchingLeftTokenLine = matchingLeftNode.getSymbol().getLine();
						matchingSymbolOnDiffLine = matchingLeftTokenLine != line ? 1 : 0;
					}
					else {
						System.err.println("can't find matching node for "+curToken);
					}
				}
			}
		}
		return matchingSymbolOnDiffLine;
	}

	public static List<Integer> viableLeftTokenTypes(ParserRuleContext node,
	                                                 Token curToken,
	                                                 List<Pair<Integer,Integer>> pairs)
	{
		List<Integer> newPairs = new ArrayList<>();
		for (Pair<Integer, Integer> p : pairs) {
			if ( p.b==curToken.getType() && !node.getTokens(p.a).isEmpty() ) {
				newPairs.add(p.a);
			}
		}
		return newPairs;
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

	public List<Integer> getAlignWithPrevious() {
		return alignWithPrevious;
	}

	public List<Integer> getIndent() {
		return indent;
	}

	public static String _toString(Vocabulary v, String[] ruleNames, int[] features) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<FEATURES.length; i++) {
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			switch ( FEATURES[i].type ) {
				case TOKEN :
					String tokenName = v.getDisplayName(features[i]);
					String abbrev = StringUtils.abbreviateMiddle(tokenName, "*", displayWidth);
					String centered = StringUtils.center(abbrev, displayWidth);
					buf.append(String.format("%"+displayWidth+"s", centered));
					break;
				case RULE :
					if ( features[i]>=0 ) {
						String ruleName = ruleNames[features[i]];
						abbrev = StringUtils.abbreviateMiddle(ruleName, "*", displayWidth);
						buf.append(String.format("%"+displayWidth+"s", abbrev));
					}
					else {
						buf.append(Tool.sequence(displayWidth, " "));
					}
					break;
				case INT :
				case COL :
				case INFO_LINE:
				case INFO_CHARPOS:
					if ( features[i]>=0 ) {
						buf.append(String.format("%"+displayWidth+"s", String.valueOf(features[i])));
					}
					else {
						buf.append(Tool.sequence(displayWidth, " "));
					}
					break;
				case INFO_FILE:
					buf.append(Tool.sequence(displayWidth, " "));
					break;
				case BOOL :
					if ( features[i]!=-1 ) {
						buf.append(features[i] == 1 ? "true" : "false");
					}
					else {
						buf.append(Tool.sequence(displayWidth, " "));
					}
					break;
				default :
					System.err.println("NO STRING FOR FEATURE TYPE: "+ FEATURES[i].type);
			}
		}
		return buf.toString();
	}

	public static String featureNameHeader() {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<FEATURES.length; i++) {
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center(FEATURES[i].abbrevHeaderRows[0], displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center(FEATURES[i].abbrevHeaderRows[1], displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(Tool.sequence(displayWidth,"="));
		}
		buf.append("\n");
		return buf.toString();
	}

	/** Make an index for fast lookup from Token to tree leaf */
	public static Map<Token, TerminalNode> indexTree(ParserRuleContext root) {
		Map<Token, TerminalNode> tokenToNodeMap = new HashMap<>();
		ParseTreeWalker.DEFAULT.walk(
			new ParseTreeListener() {
				@Override
				public void visitTerminal(TerminalNode node) {
					Token curToken = node.getSymbol();
					tokenToNodeMap.put(curToken, node);
				}

				@Override
				public void visitErrorNode(ErrorNode node) {
				}

				@Override
				public void enterEveryRule(ParserRuleContext ctx) {
				}

				@Override
				public void exitEveryRule(ParserRuleContext ctx) {
				}
			},
			root
		                            );
		return tokenToNodeMap;
	}

	public static List<Token> getRealTokens(CommonTokenStream tokens) {
		List<Token> real = new ArrayList<Token>();
		for (int i=0; i<tokens.size(); i++) {
			Token t = tokens.get(i);
			if ( t.getType()!=Token.EOF &&
				t.getChannel()==Lexer.DEFAULT_TOKEN_CHANNEL )
			{
				real.add(t);
			}
		}
		return real;
	}
}
