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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectFeatures {
	// Feature values for pair on diff lines feature
	public static final int NOT_PAIR = -1;
	public static final int PAIR_ON_SAME_LINE = 0;
	public static final int PAIR_ON_DIFF_LINE = 1;

	// Categories for alignment/indentation
	public static final int CAT_NO_ALIGNMENT = 0;
	public static final int CAT_INDENT = 1;
	public static final int CAT_ALIGN_WITH_ANCESTOR_FIRST_TOKEN = 2;
	public static final int CAT_ALIGN_WITH_ANCESTORS_PARENT_FIRST_TOKEN = 3;
	public static final int CAT_ALIGN_WITH_LIST_FIRST_ELEMENT = 4;
	public static final int CAT_ALIGN_WITH_PAIR = 5;

	public static final double MAX_CONTEXT_DIFF_THRESHOLD = 0.6;

	public static final int INDEX_PREV2_TYPE        = 0;
	public static final int INDEX_PREV_TYPE         = 1;
	public static final int INDEX_PREV_RULE         = 2; // what rule is prev token in?
	public static final int INDEX_PREV_END_COLUMN   = 3;
	public static final int INDEX_PREV_EARLIEST_ANCESTOR = 4;
	public static final int INDEX_TYPE              = 5;
	public static final int INDEX_FIRST_EL_OF_LIST  = 6;
	public static final int INDEX_MATCHING_TOKEN_DIFF_LINE = 7;
	public static final int INDEX_FIRST_ON_LINE		= 8; // a \n right before this token?
	public static final int INDEX_RULE              = 9; // what rule are we in?
	public static final int INDEX_EARLIEST_RIGHT_ANCESTOR = 10;
	public static final int INDEX_EARLIEST_LEFT_ANCESTOR = 11;
	public static final int INDEX_NEXT_TYPE         = 12;
	public static final int INDEX_INFO_FILE         = 13;
	public static final int INDEX_INFO_LINE         = 14;
	public static final int INDEX_INFO_CHARPOS      = 15;

	public static final int NUM_FEATURES            = 15;

	public static FeatureMetaData[] FEATURES_INJECT_NL = {
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-2)"}, 1),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "rule"}, 2),
		new FeatureMetaData(FeatureType.INT,   new String[] {"LT(-1)", "end col"}, 0),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "right ancestor"}, 3),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 2),
		new FeatureMetaData(FeatureType.BOOL,   new String[]{"Strt", "list"}, 3),
		new FeatureMetaData(FeatureType.BOOL,   new String[]{"Pair", "dif\\n"}, 3),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "rule"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "right ancestor"}, 3),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "left ancestor"}, 3),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(FeatureType.INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public static FeatureMetaData[] FEATURES_ALIGN = {
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-2)"}, 1),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "rule"}, 2),
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 2),
//		new FeatureMetaData(FeatureType.BOOL,   new String[]{"Strt", "list"}, 3),
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.BOOL,   new String[]{"Strt", "line"}, 3),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "rule"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "right ancestor"}, 3),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "left ancestor"}, 3),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(2)"}, 1),
		new FeatureMetaData(FeatureType.INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(FeatureType.INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public static FeatureMetaData[] FEATURES_INDENT = {
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "rule"}, 2),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "right ancestor"}, 3),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 2),
		new FeatureMetaData(FeatureType.BOOL,   new String[]{"Strt", "list"}, 3),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.BOOL,   new String[]{"Strt", "line"}, 3),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "rule"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "right ancestor"}, 3),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "left ancestor"}, 3),
//		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(2)"}, 1),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(FeatureType.INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public static FeatureMetaData[] FEATURES_INJECT_WS = {
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-2)"}, 1),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "rule"}, 2),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "right ancestor"}, 3),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 3),
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.BOOL,   new String[]{"Strt", "line"}, 3),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "rule"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "right ancestor"}, 3),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "left ancestor"}, 3),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(2)"}, 1),
		new FeatureMetaData(FeatureType.INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(FeatureType.INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public static FeatureMetaData[] FEATURES_ALL = {
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-2)"}, 1),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "rule"}, 2),
		new FeatureMetaData(FeatureType.INT,   new String[] {"LT(-1)", "end col"}, 0),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "right ancestor"}, 3),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 2),
		new FeatureMetaData(FeatureType.BOOL,   new String[]{"Strt", "list"}, 3),
		new FeatureMetaData(FeatureType.BOOL,   new String[]{"Pair", "dif\\n"}, 3),
		new FeatureMetaData(FeatureType.BOOL,   new String[]{"Strt", "line"}, 3),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "rule"}, 2),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "right ancestor"}, 3),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "left ancestor"}, 3),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(2)"}, 1),
		new FeatureMetaData(FeatureType.INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(FeatureType.INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public static int getCurrentTokenType(int[] features) { return features[INDEX_TYPE]; }
	public static int getInfoLine(int[] features) { return features[INDEX_INFO_LINE]; }
	public static int getInfoCharPos(int[] features) { return features[INDEX_INFO_CHARPOS]; }

	protected InputDocument doc;
	protected ParserRuleContext root;
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected List<int[]> features = new ArrayList<>();
	protected List<Integer> injectNewlines = new ArrayList<>();
	protected List<Integer> injectWS = new ArrayList<>();
	protected List<Integer> indent = new ArrayList<>();
	protected List<Integer> align = new ArrayList<>();

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
		TerminalNode node = tokenToNodeMap.get(curToken);

		int[] features = getNodeFeatures(tokenToNodeMap, doc, i, curToken.getLine(), tabSize);

		int precedingNL = getPrecedingNL(tokens, i); // how many lines to inject

		this.injectNewlines.add(precedingNL);

		int columnDelta = 0;
		if ( precedingNL>0 ) { // && aligned!=1 ) {
			columnDelta = curToken.getCharPositionInLine() - currentIndent;
			currentIndent = curToken.getCharPositionInLine();
		}

		int aligned = CAT_NO_ALIGNMENT ;
		if ( precedingNL>0 ) {
			// at a newline, are we aligned with a prior sibling (in a list) etc...
			if ( CollectFeatures.isAlignedWithFirstSiblingOfList(tokenToNodeMap, tokens, curToken) ) {
				aligned = CAT_ALIGN_WITH_LIST_FIRST_ELEMENT;
			}
			else {
				TerminalNode matchingLeftSymbol = getMatchingLeftSymbol(doc, node);
				if ( matchingLeftSymbol!=null &&
					matchingLeftSymbol.getSymbol().getCharPositionInLine()==curToken.getCharPositionInLine() ) {
					aligned = CAT_ALIGN_WITH_PAIR;
				}
				else {
					ParserRuleContext parent = (ParserRuleContext)node.getParent();
					ParserRuleContext earliestRightAncestor = earliestAncestorEndingWithToken(parent, curToken);
					Token earliestAncestorRightStart = null;
					if ( earliestRightAncestor!=null ) {
						earliestAncestorRightStart = earliestRightAncestor.getStart();
					}
					if ( earliestAncestorRightStart!=null &&
						 earliestAncestorRightStart!=curToken &&
						 earliestAncestorRightStart.getCharPositionInLine()==curToken.getCharPositionInLine() )
					{
						aligned = CAT_ALIGN_WITH_ANCESTOR_FIRST_TOKEN;
					}
					else {
						Token earliestAncestorsParentStart = null;
						if ( earliestRightAncestor!=null && earliestRightAncestor.getParent()!=null ) {
							earliestAncestorsParentStart = earliestRightAncestor.getParent().getStart();
						}
						if ( earliestAncestorsParentStart!=null &&
							 earliestAncestorsParentStart!=curToken &&
							 earliestAncestorsParentStart.getCharPositionInLine()==curToken.getCharPositionInLine() )
						{
							aligned = CAT_ALIGN_WITH_ANCESTORS_PARENT_FIRST_TOKEN;
						}
						else if ( columnDelta>0 ) {
							aligned = CAT_INDENT; // indent standard amount
						}
					}
				}
			}
		}

		int ws = 0;
		if ( precedingNL==0 ) {
			ws = curToken.getCharPositionInLine() -
				(prevToken.getCharPositionInLine()+prevToken.getText().length());
		}

		this.indent.add(columnDelta);

		this.injectWS.add(ws); // likely negative if precedingNL

		this.align.add(aligned);

		this.features.add(features);
	}

	public static int getPrecedingNL(CommonTokenStream tokens, int i) {
		int precedingNL = 0;
		List<Token> wsTokensBeforeCurrentToken = tokens.getHiddenTokensToLeft(i);
		if ( wsTokensBeforeCurrentToken==null ) return 0;
		for (Token t : wsTokensBeforeCurrentToken) {
			precedingNL += Tool.count(t.getText(), '\n');
		}
		return precedingNL;
	}

	public static boolean isAlignedWithFirstSiblingOfList(Map<Token, TerminalNode> tokenToNodeMap,
														  CommonTokenStream tokens,
														  Token curToken)
	{
		TerminalNode node = tokenToNodeMap.get(curToken);
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		ParserRuleContext earliestAncestor = earliestAncestorStartingWithToken(parent, curToken);
		boolean aligned = false;

		// at a newline, are we aligned with a prior sibling (in a list)?
		int precedingNL = getPrecedingNL(tokens, curToken.getTokenIndex());
		if ( precedingNL>0 && earliestAncestor!=null ) {
			ParserRuleContext commonAncestor = earliestAncestor.getParent();
			List<ParserRuleContext> siblings = commonAncestor.getRuleContexts(earliestAncestor.getClass());
			if ( siblings.size()>1 ) {
				ParserRuleContext firstSibling = siblings.get(0);
				Token firstSiblingStartToken = firstSibling.getStart();
				if ( firstSiblingStartToken!=curToken && // can't align with yourself
					firstSiblingStartToken.getCharPositionInLine()==curToken.getCharPositionInLine() ) {
					aligned = true;
				}
			}
		}
		return aligned;
	}

	/** Return list of sibling if curToken's ancestor is in a list.
	 *  Return null if curToken has not ancestor starting with curToken or
	 *  if the ancestor has no siblings (same node type like StatementContext).
	 */
	public static List<ParserRuleContext> getListSiblings(Map<Token, TerminalNode> tokenToNodeMap,
														  Token curToken)
	{
		TerminalNode node = tokenToNodeMap.get(curToken);
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		ParserRuleContext earliestAncestor = earliestAncestorStartingWithToken(parent, curToken);

		if ( earliestAncestor!=null ) {
			ParserRuleContext commonAncestor = earliestAncestor.getParent();
			List<ParserRuleContext> siblings = commonAncestor.getRuleContexts(earliestAncestor.getClass());
			if ( siblings.size()>1 ) {
				return siblings;
			}
		}
		return null;
	}

	/** Is curToken the first statement of an slist, first arg of arglist, etc... */
	public static boolean isFirstSiblingOfList(Map<Token, TerminalNode> tokenToNodeMap,
											   Token curToken)
	{
		TerminalNode node = tokenToNodeMap.get(curToken);
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		ParserRuleContext earliestAncestor = earliestAncestorStartingWithToken(parent, curToken);

		if ( earliestAncestor!=null ) {
			ParserRuleContext commonAncestor = earliestAncestor.getParent();
			List<ParserRuleContext> siblings = commonAncestor.getRuleContexts(earliestAncestor.getClass());
			if ( siblings.size()>1 ) {
				ParserRuleContext firstSibling = siblings.get(0);
				Token firstSiblingStartToken = firstSibling.getStart();
				return firstSiblingStartToken==curToken;
			}
		}
		return false;
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
		List<Token> tokensOnPreviousLine = getTokensOnPreviousLine(tokens, tokIndex, tokens.get(tokIndex).getLine());
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
	public static ParserRuleContext earliestAncestorStartingWithToken(ParserRuleContext node, Token token) {
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
	public static ParserRuleContext earliestAncestorEndingWithToken(ParserRuleContext node, Token token) {
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
		if ( node==null ) {
			System.err.println("### No node associated with token "+tokens.get(i));
		}
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
		ParserRuleContext prevEarliestRightAncestor = earliestAncestorEndingWithToken(parent, prevToken);
		int prevEarliestAncestorRuleIndex = -1;
		int prevEarliestAncestorWidth = -1;
		if ( prevEarliestRightAncestor!=null ) {
			prevEarliestAncestorRuleIndex = prevEarliestRightAncestor.getRuleIndex();
			prevEarliestAncestorWidth = prevEarliestRightAncestor.stop.getStopIndex()-prevEarliestRightAncestor.start.getStartIndex()+1;
		}

		// Get context information for current token
		parent = (ParserRuleContext)node.getParent();
		int curTokensParentRuleIndex = parent.getRuleIndex();
		ParserRuleContext earliestLeftAncestor = earliestAncestorStartingWithToken(parent, curToken);
		int earliestAncestorWidth = -1;
		int earliestLeftAncestorRuleIndex = -1;
		if ( earliestLeftAncestor!=null ) {
			earliestLeftAncestorRuleIndex = earliestLeftAncestor.getRuleIndex();
			earliestAncestorWidth = earliestLeftAncestor.stop.getStopIndex()-earliestLeftAncestor.start.getStartIndex()+1;
		}

		ParserRuleContext earliestRightAncestor = earliestAncestorEndingWithToken(parent, curToken);
		int earliestRightAncestorRuleIndex = -1;
		if ( earliestRightAncestor!=null ) {
			earliestRightAncestorRuleIndex = earliestRightAncestor.getRuleIndex();
		}
		int prevTokenEndCharPos = window.get(1).getCharPositionInLine() + window.get(1).getText().length();

		int matchingSymbolOnDiffLine = getMatchingSymbolOnDiffLine(doc, node, line);

		int sumEndColAndAncestorWidth = -1;
		if ( earliestAncestorWidth>=0 ) {
			sumEndColAndAncestorWidth = prevTokenEndCharPos+earliestAncestorWidth;
		}

		boolean startOfList = isFirstSiblingOfList(tokenToNodeMap, curToken);

		boolean curTokenStartsNewLine = window.get(2).getLine()>window.get(1).getLine();
		int[] features = {
			window.get(0).getType(),

			window.get(1).getType(),
			prevTokenRuleIndex,
			prevTokenEndCharPos,
			prevEarliestAncestorRuleIndex,

			window.get(2).getType(), // LT(1)
			startOfList ? 1 : 0,
			matchingSymbolOnDiffLine,
			curTokenStartsNewLine ? 1 : 0,
			curTokensParentRuleIndex,
			earliestRightAncestorRuleIndex,
			earliestLeftAncestorRuleIndex,
			window.get(3).getType(),

			// info
			0, // dummy; we don't store file index into feature vector
			curToken.getLine(),
			curToken.getCharPositionInLine()
		};
		assert features.length == NUM_FEATURES;
//		System.out.print(curToken+": "+CodekNNClassifier._toString(features));
		return features;
	}

	public static int getMatchingSymbolOnDiffLine(InputDocument doc,
												  TerminalNode node,
												  int line)
	{
		TerminalNode matchingLeftNode = getMatchingLeftSymbol(doc, node);
		if (matchingLeftNode != null) {
			int matchingLeftTokenLine = matchingLeftNode.getSymbol().getLine();
			return matchingLeftTokenLine != line ? PAIR_ON_DIFF_LINE : PAIR_ON_SAME_LINE;
		}
		return NOT_PAIR;
	}

	public static TerminalNode getMatchingLeftSymbol(InputDocument doc,
													 TerminalNode node)
	{
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		int curTokensParentRuleIndex = parent.getRuleIndex();
		Token curToken = node.getSymbol();
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
					if (matchingLeftNode == null) {
						System.err.println("can't find matching node for "+node.getSymbol());
					}
					return matchingLeftNode;
				}
			}
		}
		return null;
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
	public static List<Token> getTokensOnPreviousLine(CommonTokenStream tokens, int tokIndex, int curLine) {
		// first find previous line by looking for real token on line < tokens.get(i)
		int prevLine = 0;
		for (int i = tokIndex-1; i>=0; i--) {
			Token t = tokens.get(i);
			if ( t.getChannel()==Token.DEFAULT_CHANNEL && t.getLine()<curLine ) {
				prevLine = t.getLine();
				tokIndex = i; // start collecting at this index
				break;
			}
		}

		// Now collect the on-channel real tokens for this line
		List<Token> online = new ArrayList<>();
		for (int i = tokIndex; i>=0; i--) {
			Token t = tokens.get(i);
			if ( t.getChannel()==Token.DEFAULT_CHANNEL ) {
				if ( t.getLine()<prevLine )	break; // found last token on that previous line
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

	public List<Integer> getAlign() {
		return align;
	}

	public List<Integer> getIndent() {
		return indent;
	}

	public static String _toString(FeatureMetaData[] FEATURES, InputDocument doc, int[] features) {
		Vocabulary v = doc.parser.getVocabulary();
		String[] ruleNames = doc.parser.getRuleNames();
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
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
					String fname = new File(doc.fileName).getName();
					fname = StringUtils.abbreviate(fname, displayWidth);
					buf.append(String.format("%"+displayWidth+"s", fname));
					break;
				case BOOL :
					if ( features[i]!=-1 ) {
						buf.append(features[i] == 1 ? "true " : "false");
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

	public static String featureNameHeader(FeatureMetaData[] FEATURES) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center(FEATURES[i].abbrevHeaderRows[0], displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center(FEATURES[i].abbrevHeaderRows[1], displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center("("+((int)FEATURES[i].mismatchCost)+")", displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
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
