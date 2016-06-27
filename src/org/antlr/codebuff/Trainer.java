package org.antlr.codebuff;

import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.codebuff.misc.RuleAltKey;
import org.antlr.codebuff.walkers.CollectTokenPairs;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.antlr.codebuff.FeatureType.BOOL;
import static org.antlr.codebuff.FeatureType.INFO_CHARPOS;
import static org.antlr.codebuff.FeatureType.INFO_FILE;
import static org.antlr.codebuff.FeatureType.INFO_LINE;
import static org.antlr.codebuff.FeatureType.INT;
import static org.antlr.codebuff.FeatureType.RULE;
import static org.antlr.codebuff.FeatureType.TOKEN;
import static org.antlr.codebuff.FeatureType.UNUSED;
import static org.antlr.codebuff.misc.BuffUtils.filter;

/** Collect feature vectors trained on a single file.
 *
 *  The primary results are: (X, Y1, Y2)
 *  For each feature vector, features[i], injectWhitespace[i] and align[i] tell us
 *  the decisions associated with that context in a corpus file.
 *  After calling {@link #computeFeatureVectors()}, those lists
 *
 *  There is no shared state computed by this object, only static defs
 *  of feature types and category constants.
 */
public class Trainer {
	public static final double MAX_WS_CONTEXT_DIFF_THRESHOLD = 0.12; //1.0/7;
	public static final double MAX_ALIGN_CONTEXT_DIFF_THRESHOLD = 0.15;
	public static final double MAX_CONTEXT_DIFF_THRESHOLD2 = 0.50;

	/** When computing child indexes, we use this value for any child list
	 *  element other than the first one.  If a parent has just one X child,
	 *  we use the actual child index. If parent has two or more X children,
	 *  and we are not the first X, use CHILD_INDEX_REPEATED_ELEMENT. If first
	 *  of two or more X children, use actual child index.
	 */
	public static final int CHILD_INDEX_REPEATED_ELEMENT = 1_111_111_111;

	public static final int LIST_PREFIX         = 0;
	public static final int LIST_FIRST_ELEMENT  = 1;
	public static final int LIST_FIRST_SEPARATOR= 2;
	public static final int LIST_SEPARATOR      = 3;
	public static final int LIST_SUFFIX         = 4;
	public static final int LIST_MEMBER         = 1_111_111_111;

	// Feature values for pair starts lines feature either T/F or:
	public static final int NOT_PAIR = -1;

	// Categories for newline, whitespace. CAT_INJECT_NL+n<<8 or CAT_INJECT_WS+n<<8
	public static final int CAT_NO_WS = 0;
	public static final int CAT_INJECT_NL = 100;
	public static final int CAT_INJECT_WS = 200;

	// Categories for alignment/indentation
	public static final int CAT_ALIGN = 0;

	/* We want to identify alignment with a child's start token of some parent
	   but that parent could be a number of levels up the tree. The next category
	   values indicate alignment from the current token's left ancestor's
	   parent then it's parent and so on. For category value:

	    CAT_ALIGN_WITH_ANCESTOR_CHILD | delta<<8 | childindex<<16

	   current token is aligned with start token of child childindex,
	   delta levels up from ancestor.
	 */
	public static final int CAT_ALIGN_WITH_ANCESTOR_CHILD = 10;

	/* We want to identify indentation from a parent's start token but that
	   parent could be a number of levels up the tree. The next category
	   values indicate indentation from the current token's left ancestor's
	   parent then it's parent and so on. For category value:

	    CAT_INDENT_FROM_ANCESTOR_FIRST_TOKEN | delta<<8

	   current token is indented from start token of node i levels up
	   from ancestor.
	 */
	public static final int CAT_INDENT_FROM_ANCESTOR_CHILD = 20; // left ancestor's first token is really current token

	public static final int CAT_INDENT = 30;

	// indexes into feature vector

	public static final int INDEX_PREV_TYPE                     = 0;
	public static final int INDEX_PREV_FIRST_ON_LINE            = 1; // a \n right before this token?
	public static final int INDEX_PREV_EARLIEST_RIGHT_ANCESTOR  = 2;
	public static final int INDEX_CUR_TOKEN_TYPE                = 3;
	public static final int INDEX_MATCHING_TOKEN_STARTS_LINE    = 4;
	public static final int INDEX_MATCHING_TOKEN_ENDS_LINE      = 5;
	public static final int INDEX_FIRST_ON_LINE		            = 6; // a \n right before this token?
	public static final int INDEX_MEMBER_OVERSIZE_LIST          = 7; // -1 if we don't know; false means list but not big list
	public static final int INDEX_LIST_ELEMENT_TYPE             = 8; // see LIST_PREFIX, etc...
	public static final int INDEX_CUR_TOKEN_CHILD_INDEX         = 9; // left ancestor
	public static final int INDEX_EARLIEST_LEFT_ANCESTOR        = 10;
	public static final int INDEX_ANCESTORS_CHILD_INDEX         = 11; // left ancestor
	public static final int INDEX_ANCESTORS_PARENT_RULE         = 12;
	public static final int INDEX_ANCESTORS_PARENT_CHILD_INDEX  = 13;
	public static final int INDEX_ANCESTORS_PARENT2_RULE        = 14;
	public static final int INDEX_ANCESTORS_PARENT2_CHILD_INDEX = 15;
	public static final int INDEX_ANCESTORS_PARENT3_RULE        = 16;
	public static final int INDEX_ANCESTORS_PARENT3_CHILD_INDEX = 17;
	public static final int INDEX_ANCESTORS_PARENT4_RULE        = 18;
	public static final int INDEX_ANCESTORS_PARENT4_CHILD_INDEX = 19;
	public static final int INDEX_ANCESTORS_PARENT5_RULE        = 20;
	public static final int INDEX_ANCESTORS_PARENT5_CHILD_INDEX = 21;

	public static final int INDEX_INFO_FILE                     = 22;
	public static final int INDEX_INFO_LINE                     = 23;
	public static final int INDEX_INFO_CHARPOS                  = 24;

	public static final int NUM_FEATURES                        = 25;
	public static final int ANALYSIS_START_TOKEN_INDEX          = 1; // we use current and previous token in context so can't start at index 0

	public final static FeatureMetaData[] FEATURES_INJECT_WS = { // inject ws or nl
		new FeatureMetaData(TOKEN, new String[] {"", "LT(-1)"}, 1),
		new FeatureMetaData(BOOL,  new String[] {"Strt", "line"}, 1),
		new FeatureMetaData(RULE,  new String[] {"LT(-1)", "right ancestor"}, 1),
		new FeatureMetaData(TOKEN, new String[] {"", "LT(1)"}, 1),
		new FeatureMetaData(BOOL,  new String[] {"Pair", "strt\\n"}, 1),
		new FeatureMetaData(BOOL,  new String[] {"Pair", "end\\n"}, 1),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(BOOL,  new String[] {"Big", "list"}, 2),
		new FeatureMetaData(INT,   new String[] {"List", "elem."}, 1),
		new FeatureMetaData(INT,   new String[] {"token", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"LT(1)", "left ancestor"}, 1),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(RULE,  new String[] {"", "parent"}, 1),
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		new FeatureMetaData(INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public final static FeatureMetaData[] FEATURES_HPOS = {
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		new FeatureMetaData(RULE,  new String[] {"LT(-1)", "right ancestor"}, 1),
		new FeatureMetaData(TOKEN, new String[] {"", "LT(1)"}, 1),
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		new FeatureMetaData(BOOL,  new String[] {"Strt", "line"}, 4),
		new FeatureMetaData(BOOL,  new String[] {"Big", "list"}, 1),
		new FeatureMetaData(INT,   new String[] {"List", "elem."}, 2),
		new FeatureMetaData(INT,   new String[] {"token", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"LT(1)", "left ancestor"}, 1),
		new FeatureMetaData(INT,   new String[] {"ancestor", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"", "parent"}, 1),
		new FeatureMetaData(INT,   new String[] {"parent", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"", "parent^2"}, 1),
		new FeatureMetaData(INT,   new String[] {"parent^2", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"", "parent^3"}, 1),
		new FeatureMetaData(INT,   new String[] {"parent^3", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"", "parent^4"}, 1),
		new FeatureMetaData(INT,   new String[] {"parent^4", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"", "parent^5"}, 1),
		new FeatureMetaData(INT,   new String[] {"parent^5", "child index"}, 1),
		new FeatureMetaData(INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public final static FeatureMetaData[] FEATURES_ALL = {
		new FeatureMetaData(TOKEN, new String[] {"", "LT(-1)"}, 1),
		new FeatureMetaData(BOOL,  new String[] {"Strt", "line"}, 1),
		new FeatureMetaData(RULE,  new String[] {"LT(-1)", "right ancestor"}, 1),
		new FeatureMetaData(TOKEN, new String[] {"", "LT(1)"}, 1),
		new FeatureMetaData(BOOL,  new String[] {"Pair", "strt\\n"}, 1),
		new FeatureMetaData(BOOL,  new String[] {"Pair", "end\\n"}, 1),
		new FeatureMetaData(BOOL,  new String[] {"Strt", "line"}, 1),
		new FeatureMetaData(BOOL,  new String[] {"Big", "list"}, 1),
		new FeatureMetaData(INT,   new String[] {"List", "elem."}, 1),
		new FeatureMetaData(INT,   new String[] {"token", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"LT(1)", "left ancestor"}, 1),
		new FeatureMetaData(INT,   new String[] {"ancestor", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"", "parent"}, 1),
		new FeatureMetaData(INT,   new String[] {"parent", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"", "parent^2"}, 1),
		new FeatureMetaData(INT,   new String[] {"parent^2", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"", "parent^3"}, 1),
		new FeatureMetaData(INT,   new String[] {"parent^3", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"", "parent^4"}, 1),
		new FeatureMetaData(INT,   new String[] {"parent^4", "child index"}, 1),
		new FeatureMetaData(RULE,  new String[] {"", "parent^5"}, 1),
		new FeatureMetaData(INT,   new String[] {"parent^5", "child index"}, 1),
		new FeatureMetaData(INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	protected Corpus corpus;
	protected InputDocument doc;
	protected ParserRuleContext root;
	protected CodeBuffTokenStream tokens; // track stream so we can examine previous tokens
	protected int indentSize;

	/** Make it fast to get a node for a specific token */
	protected Map<Token, TerminalNode> tokenToNodeMap = null;

	public Trainer(Corpus corpus, InputDocument doc, int indentSize) {
		this.corpus = corpus;
		this.doc = doc;
		this.root = doc.tree;
		this.tokenToNodeMap = doc.tokenToNodeMap;
		this.tokens = doc.tokens;
		this.indentSize = indentSize;
	}

	public void computeFeatureVectors() {
		List<Token> realTokens = getRealTokens(tokens);

		for (int i = ANALYSIS_START_TOKEN_INDEX; i<realTokens.size(); i++) { // can't process first token
			int tokenIndexInStream = realTokens.get(i).getTokenIndex();
			computeFeatureVectorForToken(tokenIndexInStream);
		}
	}

	public void computeFeatureVectorForToken(int i) {
		Token curToken = tokens.get(i);
		if ( curToken.getType()==Token.EOF ) return;

		int[] features = getFeatures(i);

		int injectNL_WS = getInjectWSCategory(tokens, i);

		int aligned = -1; // "don't care"
		if ( (injectNL_WS&0xFF)==CAT_INJECT_NL ) {
			TerminalNode node = tokenToNodeMap.get(curToken);
			aligned = getAlignmentCategory(doc, node, indentSize);
		}

		// track feature -> injectws, align decisions for token i
		corpus.addExemplar(doc, features, injectNL_WS, aligned);
	}

	public static int getInjectWSCategory(CodeBuffTokenStream tokens, int i) {
		int precedingNL = getPrecedingNL(tokens, i); // how many lines to inject

		Token curToken = tokens.get(i);
		Token prevToken = tokens.getPreviousRealToken(i);

		int ws = 0;
		if ( precedingNL==0 ) {
			ws = curToken.getCharPositionInLine() -
				(prevToken.getCharPositionInLine()+prevToken.getText().length());
		}

		int injectNL_WS = CAT_NO_WS;
		if ( precedingNL>0 ) {
			injectNL_WS = nlcat(precedingNL);
		}
		else if ( ws>0 ) {
			injectNL_WS = wscat(ws);
		}

		return injectNL_WS;
	}

	// at a newline, are we aligned with a prior sibling (in a list) etc...
	public static int getAlignmentCategory(InputDocument doc, TerminalNode node, int indentSize) {
		Pair<Integer,Integer> alignInfo = null;
		Pair<Integer,Integer> indentInfo = null;

		Token curToken = node.getSymbol();

		// at a newline, are we aligned with a prior sibling (in a list) etc...
		ParserRuleContext earliestLeftAncestor = earliestAncestorStartingWithToken(node);
		Pair<ParserRuleContext, Integer> alignPair =
			earliestAncestorWithChildStartingAtCharPos(earliestLeftAncestor, curToken, curToken.getCharPositionInLine());
//		String[] ruleNames = doc.parser.getRuleNames();
//		Token prevToken = doc.tokens.getPreviousRealToken(curToken.getTokenIndex());
		if ( alignPair!=null ) {
			int deltaFromLeftAncestor = getDeltaToAncestor(earliestLeftAncestor, alignPair.a);
			alignInfo = new Pair<>(deltaFromLeftAncestor, alignPair.b);
//			int ruleIndex = pair.a.getRuleIndex();
//			System.out.printf("ALIGN %s %s i=%d %s %s\n",
//			                  curToken,
//			                  ruleNames[ruleIndex],
//			                  pair.b, alignInfo, doc.fileName);
		}

		// perhaps we are indented as well?
		int tokenIndexInStream = node.getSymbol().getTokenIndex();
		List<Token> tokensOnPreviousLine = getTokensOnPreviousLine(doc.tokens, tokenIndexInStream, curToken.getLine());
		Token firstTokenOnPrevLine = null;
		int columnDelta = 0;
		if ( tokensOnPreviousLine.size()>0 ) {
			firstTokenOnPrevLine = tokensOnPreviousLine.get(0);
			columnDelta = curToken.getCharPositionInLine() - firstTokenOnPrevLine.getCharPositionInLine();
		}

		Pair<ParserRuleContext, Integer> indentPair = null;
		if ( columnDelta!=0 ) {
			int indentedFromPos = curToken.getCharPositionInLine()-indentSize;
			indentPair = earliestAncestorWithChildStartingAtCharPos(earliestLeftAncestor, curToken, indentedFromPos);
			if ( indentPair==null ) {
				// try with 2 indents (commented out for now; can't encode how many indents in directive)
//				indentedFromPos = curToken.getCharPositionInLine()-2*indentSize;
//				pair = earliestAncestorWithChildStartingAtCharPos(earliestLeftAncestor, curToken, indentedFromPos);
			}
			if ( indentPair!=null ) {
				int deltaFromLeftAncestor = getDeltaToAncestor(earliestLeftAncestor, indentPair.a);
				indentInfo = new Pair<>(deltaFromLeftAncestor, indentPair.b);
//				int ruleIndex = pair.a.getRuleIndex();
//				System.out.printf("INDENT %s %s i=%d %s %s\n",
//				                  curToken,
//				                  ruleNames[ruleIndex],
//				                  pair.b, indentInfo, doc.fileName);
			}
		}

		/*
		I tried reducing all specific and alignment operations to the generic
		"indent/align from first token on previous line" directives but
		the contexts were not sufficiently precise. method bodies got doubly
		indented when there is a throws clause etc... Might be worth pursuing
		in the future if I can increase context information regarding
	    exactly what kind of method declaration signature it is. See
	    targetTokenIsFirstTokenOnPrevLine().
		 */
		// If both align and indent from ancestor child exist, choose closest (lowest delta up tree)
		if ( alignInfo!=null && indentInfo!=null ) {
			if ( alignInfo.a < indentInfo.a ) {
				return aligncat(alignInfo.a, alignInfo.b);
			}
			// Choose indentation over alignment if both at same ancestor level
			return indentcat(indentInfo.a, indentInfo.b);
//			return aligncat(alignInfo.a, alignInfo.b); // Should not use alignment over indentation; manual review of output shows indentation kinda messed up
		}

		// otherwise just return the align or indent we computed
		if ( alignInfo!=null ) {
			return aligncat(alignInfo.a, alignInfo.b);
		}
		else if ( indentInfo!=null ) {
			return indentcat(indentInfo.a, indentInfo.b);
		}

		if ( columnDelta!=0 ) {
			return CAT_INDENT; // indent standard amount
		}

		return CAT_ALIGN; // otherwise just line up with first token of previous line
	}

	public static int getPrecedingNL(CommonTokenStream tokens, int i) {
		int precedingNL = 0;
		List<Token> previousWS = getPreviousWS(tokens, i);
		if ( previousWS!=null ) {
			for (Token ws : previousWS) {
				precedingNL += Tool.count(ws.getText(), '\n');
			}
		}
		return precedingNL;
	}

	// if we have non-ws tokens like comments, we only count ws not in comments
	public static List<Token> getPreviousWS(CommonTokenStream tokens, int i) {
		List<Token> hiddenTokensToLeft = tokens.getHiddenTokensToLeft(i);
		if ( hiddenTokensToLeft==null ) return null;
		return filter(hiddenTokensToLeft, t -> t.getText().matches("\\s+"));
	}

	public static boolean hasCommentToken(List<Token> hiddenTokensToLeft) {
		boolean hasComment = false;
		for (Token hidden : hiddenTokensToLeft) {
			String hiddenText = hidden.getText();
			if ( !hiddenText.matches("\\s+") ) {
				hasComment = true;
				break;
			}
		}
		return hasComment;
	}

	/** Return first ancestor of p that is not an only child including p.
	 *  So if p.getParent().getChildCount()>1, this returns p.  If we
	 *  have found a chain rule at p's parent (p is its only child), then
	 *  move p to its parent and try again.
	 */
	public static ParserRuleContext getParentClosure(ParserRuleContext p) {
		if ( p==null ) return null;
		// if p not an only child, return p
		if ( p.getParent()==null || p.getParent().getChildCount()>1 ) return p;
		// we found a chain rule node
		return getParentClosure(p.getParent());
	}

	/** Walk upwards from node while p.start == token; return null if there is
	 *  no ancestor starting at token.
	 */
	/** Walk upwards from node while p.start == token; return immediate parent
	 *  if there is no ancestor starting at token. This is the earliest
	 *  left ancestor. E.g, for '{' of a block, return parent up the chain from
	 *  block starting with '{'. For '}' of block, return just block as nothing
	 *  starts with '}'. (block stops with it).
	 */
	public static ParserRuleContext earliestAncestorStartingWithToken(TerminalNode node) {
		Token token = node.getSymbol();
		ParserRuleContext p = (ParserRuleContext)node.getParent();
		ParserRuleContext prev = null;
		while (p!=null && p.getStart()==token) {
			prev = p;
			p = p.getParent();
		}
		if ( prev==null ) {
			return (ParserRuleContext)node.getParent();
		}
		return prev;
	}

	/** Walk upwards from node while p.stop == token; return immediate parent
	 *  if there is no ancestor stopping at token. This is the earliest
	 *  right ancestor. E.g, for '}' of a block, return parent up the chain from
	 *  block stopping with '}'. For '{' of block, return just block as nothing
	 *  stops with '{'. (block starts with it).
	 */
	public static ParserRuleContext earliestAncestorEndingWithToken(TerminalNode node) {
		Token token = node.getSymbol();
		ParserRuleContext p = (ParserRuleContext)node.getParent();
		ParserRuleContext prev = null;
		while (p!=null && p.getStop()==token) {
			prev = p;
			p = p.getParent();
		}
		if ( prev==null ) {
			return (ParserRuleContext)node.getParent();
		}
		return prev;
	}

	/** Walk upwards from node until we find a child of p at t's char position.
	 *  Don't see alignment with self, t, or element *after* us.
	 *  return null if there is no such ancestor p.
	 */
	public static Pair<ParserRuleContext,Integer> earliestAncestorWithChildStartingAtCharPos(ParserRuleContext node, Token t, int charpos) {
		ParserRuleContext p = node;
		while ( p!=null ) {
			// check all children of p to see if one of them starts at charpos
			for (int i = 0; i<p.getChildCount(); i++) {
				ParseTree child = p.getChild(i);
				Token start;
				if ( child instanceof ParserRuleContext ) {
					start = ((ParserRuleContext) child).getStart();
				}
				else { // must be token
					start = ((TerminalNode)child).getSymbol();
				}
				// check that we don't see alignment with self or element *after* us
				if ( start.getTokenIndex()<t.getTokenIndex() && start.getCharPositionInLine()==charpos ) {
					return new Pair<>(p,i);
				}
			}
			p = p.getParent();
		}
		return null;
	}

	/** Return the number of hops to get to ancestor from node or -1 if we
	 *  don't find ancestor on path to root.
	 */
	public static int getDeltaToAncestor(ParserRuleContext node, ParserRuleContext ancestor) {
		int n = 0;
		ParserRuleContext p = node;
		while ( p!=null && p!=ancestor ) {
			n++;
			p = p.getParent();
		}
		if ( p==null ) return -1;
		return n;
	}

	public static ParserRuleContext getAncestor(ParserRuleContext node, int delta) {
		int n = 0;
		ParserRuleContext p = node;
		while ( p!=null && n!=delta ) {
			n++;
			p = p.getParent();
		}
		return p;
	}

	public int[] getFeatures(int i)	{
		CodeBuffTokenStream tokens = doc.tokens;
		TerminalNode node = tokenToNodeMap.get(tokens.get(i));
		if ( node==null ) {
			System.err.println("### No node associated with token "+tokens.get(i));
			return null;
		}

		Token curToken = node.getSymbol();
		Token prevToken = tokens.getPreviousRealToken(i);
		Token prevPrevToken = prevToken!=null ? doc.tokens.getPreviousRealToken(prevToken.getTokenIndex()) : null;

		boolean prevTokenStartsLine = false;
		if ( prevToken!=null && prevPrevToken!=null ) {
			prevTokenStartsLine = prevToken.getLine()>prevPrevToken.getLine();
		}

		boolean curTokenStartsNewLine = false;
		if ( prevToken==null ) curTokenStartsNewLine = true; // we must be at start of file
		else if ( curToken.getLine() > prevToken.getLine() ) curTokenStartsNewLine = true;

		int[] features = getContextFeatures(corpus, tokenToNodeMap, doc, i);

		setListInfoFeatures(corpus.tokenToListInfo, features, curToken);

		features[INDEX_PREV_FIRST_ON_LINE]         = prevTokenStartsLine ? 1 : 0;
		features[INDEX_FIRST_ON_LINE]              = curTokenStartsNewLine ? 1 : 0;

		return features;
	}

	/** Get the token type and tree ancestor features. These are computed
	 *  the same for both training and formatting.
	 */
	public static int[] getContextFeatures(Corpus corpus,
	                                       Map<Token, TerminalNode> tokenToNodeMap,
	                                       InputDocument doc,
	                                       int i)
	{
		int[] features = new int[NUM_FEATURES];
		CodeBuffTokenStream tokens = doc.tokens;
		TerminalNode node = tokenToNodeMap.get(tokens.get(i));
		if ( node==null ) {
			System.err.println("### No node associated with token "+tokens.get(i));
			return features;
		}
		Token curToken = node.getSymbol();

		// Get context information for previous token
		Token prevToken = tokens.getPreviousRealToken(i);
		TerminalNode prevNode = tokenToNodeMap.get(prevToken);

		ParserRuleContext prevEarliestRightAncestor = earliestAncestorEndingWithToken(prevNode);
		int prevEarliestAncestorRuleIndex = prevEarliestRightAncestor.getRuleIndex();
		int prevEarliestAncestorRuleAltNum = prevEarliestRightAncestor.getAltNumber();

		// Get context information for current token
		ParserRuleContext earliestLeftAncestor = earliestAncestorStartingWithToken(node);

		ParserRuleContext earliestLeftAncestorParent  =
			earliestLeftAncestor!=null ? earliestLeftAncestor.getParent() : null;

		ParserRuleContext earliestLeftAncestorParent2 =
			earliestLeftAncestorParent!=null ? earliestLeftAncestorParent.getParent() : null;

		ParserRuleContext earliestLeftAncestorParent3 =
			earliestLeftAncestorParent2!=null ? earliestLeftAncestorParent2.getParent() : null;

		ParserRuleContext earliestLeftAncestorParent4 =
			earliestLeftAncestorParent3!=null ? earliestLeftAncestorParent3.getParent() : null;

		ParserRuleContext earliestLeftAncestorParent5 =
			earliestLeftAncestorParent4!=null ? earliestLeftAncestorParent4.getParent() : null;

		features[INDEX_PREV_TYPE]                     = prevToken.getType();
		features[INDEX_PREV_EARLIEST_RIGHT_ANCESTOR]  = rulealt(prevEarliestAncestorRuleIndex,prevEarliestAncestorRuleAltNum);
		features[INDEX_CUR_TOKEN_TYPE]                = curToken.getType();
		features[INDEX_CUR_TOKEN_CHILD_INDEX]         = getChildIndexOrListMembership(node);
		features[INDEX_EARLIEST_LEFT_ANCESTOR]        = rulealt(earliestLeftAncestor);
		features[INDEX_ANCESTORS_CHILD_INDEX]         = getChildIndexOrListMembership(earliestLeftAncestor);
		features[INDEX_ANCESTORS_PARENT_RULE]         = earliestLeftAncestorParent!=null ? rulealt(earliestLeftAncestorParent) : -1;
		features[INDEX_ANCESTORS_PARENT_CHILD_INDEX]  = getChildIndexOrListMembership(earliestLeftAncestorParent);
		features[INDEX_ANCESTORS_PARENT2_RULE]        = earliestLeftAncestorParent2!=null ? rulealt(earliestLeftAncestorParent2) : -1;
		features[INDEX_ANCESTORS_PARENT2_CHILD_INDEX] = getChildIndexOrListMembership(earliestLeftAncestorParent2);
		features[INDEX_ANCESTORS_PARENT3_RULE]        = earliestLeftAncestorParent3!=null ? rulealt(earliestLeftAncestorParent3) : -1;
		features[INDEX_ANCESTORS_PARENT3_CHILD_INDEX] = getChildIndexOrListMembership(earliestLeftAncestorParent3);
		features[INDEX_ANCESTORS_PARENT4_RULE]        = earliestLeftAncestorParent4!=null ? rulealt(earliestLeftAncestorParent4) : -1;
		features[INDEX_ANCESTORS_PARENT4_CHILD_INDEX] = getChildIndexOrListMembership(earliestLeftAncestorParent4);
		features[INDEX_ANCESTORS_PARENT5_RULE]        = earliestLeftAncestorParent5!=null ? rulealt(earliestLeftAncestorParent5) : -1;
		features[INDEX_ANCESTORS_PARENT5_CHILD_INDEX] = getChildIndexOrListMembership(earliestLeftAncestorParent5);

		features[INDEX_MATCHING_TOKEN_STARTS_LINE] = getMatchingSymbolStartsLine(corpus, doc, node);
		features[INDEX_MATCHING_TOKEN_ENDS_LINE]   = getMatchingSymbolEndsLine(corpus, doc, node);

		features[INDEX_INFO_FILE]    = 0; // dummy; _toString() dumps filename w/o this value; placeholder for col in printout
		features[INDEX_INFO_LINE]    = curToken.getLine();
		features[INDEX_INFO_CHARPOS] = curToken.getCharPositionInLine();

		return features;
	}

	public static void setListInfoFeatures(Map<Token,Pair<Boolean,Integer>> tokenToListInfo, int[] features, Token curToken) {
		int isOversizeList = -1;
		int listElementType = -1;
		Pair<Boolean, Integer> listInfo = tokenToListInfo.get(curToken);
		if ( listInfo!=null ) {
			isOversizeList = listInfo.a ? 1 : 0;
			listElementType = listInfo.b;
		}
		features[INDEX_MEMBER_OVERSIZE_LIST]     = isOversizeList; // -1 if we don't know; false means list but not big list
		features[INDEX_LIST_ELEMENT_TYPE]        = listElementType;
	}

	public static int getSiblingsLength(List<? extends ParserRuleContext> siblings) {
		int len = 0;
		for (ParserRuleContext sib : siblings) {
			len += sib.getText().length();
		}
		return len;
	}

	public static String getText(List<? extends Token> tokens) {
		if ( tokens==null ) return "";
		StringBuilder buf = new StringBuilder();
		for (Token sib : tokens) {
			buf.append(sib.getText());
		}
		return buf.toString();
	}


	public static int getMatchingSymbolStartsLine(Corpus corpus,
	                                              InputDocument doc,
	                                              TerminalNode node)
	{
		TerminalNode matchingLeftNode = getMatchingLeftSymbol(corpus, doc, node);
		if ( matchingLeftNode != null ) {
			Token matchingLeftToken = matchingLeftNode.getSymbol();
			int i = matchingLeftToken.getTokenIndex();
			if ( i==0 ) return 1; // first token is considered first on line
			Token tokenBeforeMatchingToken = doc.tokens.getPreviousRealToken(i);
//			System.out.printf("doc=%s node=%s, pair=%s, before=%s\n",
//			                  new File(doc.fileName).getName(), node.getSymbol(), matchingLeftToken, tokenBeforeMatchingToken);
			if ( tokenBeforeMatchingToken!=null ) {
				return matchingLeftToken.getLine()>tokenBeforeMatchingToken.getLine() ? 1 : 0;
			}
			else { // matchingLeftToken must be first in file
				return 1;
			}
		}
		return NOT_PAIR;
	}

	public static int getMatchingSymbolEndsLine(Corpus corpus,
	                                            InputDocument doc,
	                                            TerminalNode node)
	{
		TerminalNode matchingLeftNode = getMatchingLeftSymbol(corpus, doc, node);
		if ( matchingLeftNode != null ) {
			Token matchingLeftToken = matchingLeftNode.getSymbol();
			int i = matchingLeftToken.getTokenIndex();
			Token tokenAfterMatchingToken = doc.tokens.getNextRealToken(i);
//			System.out.printf("doc=%s node=%s, pair=%s, after=%s\n",
//			                  new File(doc.fileName).getName(), node.getSymbol(), matchingLeftToken, tokenAfterMatchingToken);
			if ( tokenAfterMatchingToken!=null ) {
				if ( tokenAfterMatchingToken.getType()==Token.EOF ) {
					return 1;
				}
				return tokenAfterMatchingToken.getLine()>matchingLeftToken.getLine() ? 1 : 0;
			}
		}
		return NOT_PAIR;
	}

	public static TerminalNode getMatchingLeftSymbol(Corpus corpus,
	                                                 InputDocument doc,
	                                                 TerminalNode node)
	{
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		int curTokensParentRuleIndex = parent.getRuleIndex();
		Token curToken = node.getSymbol();
		if (corpus.ruleToPairsBag != null) {
			String ruleName = doc.parser.getRuleNames()[curTokensParentRuleIndex];
			RuleAltKey ruleAltKey = new RuleAltKey(ruleName, parent.getAltNumber());
			List<Pair<Integer, Integer>> pairs = corpus.ruleToPairsBag.get(ruleAltKey);
			if ( pairs!=null ) {
				// Find appropriate pair given current token
				// If more than one pair (a,b) with b=current token pick first one
				// or if a common pair like ({,}), then give that one preference.
				// or if b is punctuation, prefer a that is punct
				List<Integer> viableMatchingLeftTokenTypes = viableLeftTokenTypes(parent, curToken, pairs);
				Vocabulary vocab = doc.parser.getVocabulary();
				if ( !viableMatchingLeftTokenTypes.isEmpty() ) {
					int matchingLeftTokenType =
						CollectTokenPairs.getMatchingLeftTokenType(curToken, viableMatchingLeftTokenTypes, vocab);
					List<TerminalNode> matchingLeftNodes = parent.getTokens(matchingLeftTokenType);
					// get matching left node by getting last node to left of current token
					List<TerminalNode> nodesToLeftOfCurrentToken =
						filter(matchingLeftNodes, n -> n.getSymbol().getTokenIndex()<curToken.getTokenIndex());
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

	public static String _toString(FeatureMetaData[] FEATURES, InputDocument doc, int[] features) {
		return _toString(FEATURES, doc, features, true);
	}

	public static String _toString(FeatureMetaData[] FEATURES, InputDocument doc, int[] features,
	                               boolean showInfo) {
		Vocabulary v = doc.parser.getVocabulary();
		String[] ruleNames = doc.parser.getRuleNames();
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TOKEN_TYPE ) {
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
						String ruleName = ruleNames[unrulealt(features[i])[0]];
						int ruleAltNum = unrulealt(features[i])[1];
						ruleName += ":"+ruleAltNum;
						abbrev = StringUtils.abbreviateMiddle(ruleName, "*", displayWidth);
						buf.append(String.format("%"+displayWidth+"s", abbrev));
					}
					else {
						buf.append(Tool.sequence(displayWidth, " "));
					}
					break;
				case INT :
				case INFO_LINE:
				case INFO_CHARPOS:
					if ( showInfo ) {
						if ( features[i]>=0 ) {
							buf.append(String.format("%"+displayWidth+"s", StringUtils.center(String.valueOf(features[i]), displayWidth)));
						}
						else {
							buf.append(Tool.sequence(displayWidth, " "));
						}
					}
					break;
				case INFO_FILE:
					if ( showInfo ) {
						String fname = new File(doc.fileName).getName();
						fname = StringUtils.abbreviate(fname, displayWidth);
						buf.append(String.format("%"+displayWidth+"s", fname));
					}
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

	public static String _toFileInfoString(FeatureMetaData[] FEATURES, InputDocument doc, int[] features) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type!=INFO_FILE &&
				FEATURES[i].type!=INFO_LINE &&
				FEATURES[i].type!=INFO_CHARPOS )
			{
				continue;
			}
			if ( i>0 ) buf.append(" ");
			int displayWidth = FEATURES[i].type.displayWidth;
			switch ( FEATURES[i].type ) {
				case INFO_LINE:
				case INFO_CHARPOS:
					if ( features[i]>=0 ) {
						buf.append(String.format("%"+displayWidth+"s", StringUtils.center(String.valueOf(features[i]), displayWidth)));
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
				default :
					System.err.println("NO STRING FOR FEATURE TYPE: "+ FEATURES[i].type);
			}
		}
		return buf.toString();
	}

	public static String featureNameHeader(FeatureMetaData[] FEATURES) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TOKEN_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center(FEATURES[i].abbrevHeaderRows[0], displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TOKEN_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center(FEATURES[i].abbrevHeaderRows[1], displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TOKEN_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center("("+((int)FEATURES[i].mismatchCost)+")", displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TOKEN_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(Tool.sequence(displayWidth, "="));
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
		List<Token> real = new ArrayList<>();
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

	/** Return the index 0..n-1 of t as child of t.parent.
	 *  If t is index 0, always return 0.
	 *  If t is a repeated subtree root and index within
	 *  sibling list > 0, return CHILD_INDEX_LIST_ELEMENT.
	 *  In all other cases, return the actual index of t. That means for a
	 *  sibling list starting at child index 5, the first sibling will return
	 *  5 but 2nd and beyond in list will return CHILD_INDEX_LIST_ELEMENT.
	 */
	public static int getChildIndexOrListMembership(ParseTree t) {
		if ( t==null ) return -1;
		ParseTree parent = t.getParent();
		if ( parent==null ) {
			return -1;
		}
		// we know we have a parent now
		// check to see if we are 2nd or beyond element in a sibling list
		if ( t instanceof ParserRuleContext ) {
			List<ParserRuleContext> siblings =
				((ParserRuleContext)parent).getRuleContexts(((ParserRuleContext)t).getClass());
			if ( siblings.size()>1 && siblings.indexOf(t)>0 ) {
				return CHILD_INDEX_REPEATED_ELEMENT;
			}
		}
		// check to see if we are 2nd or beyond repeated token
		if ( t instanceof TerminalNode ) {
			List<TerminalNode> repeatedTokens =
				((ParserRuleContext) parent).getTokens(((TerminalNode) t).getSymbol().getType());
			if ( repeatedTokens.size()>1 && repeatedTokens.indexOf(t)>0 ) {
				return CHILD_INDEX_REPEATED_ELEMENT;
			}
		}

		return getChildIndex(t);
	}

	public static int getChildIndex(ParseTree t) {
		if ( t==null ) return -1;
		ParseTree parent = t.getParent();
		if ( parent==null ) {
			return -1;
		}
		// Figure out which child index t is of parent
		for (int i = 0; i<parent.getChildCount(); i++) {
			if ( parent.getChild(i)==t ) {
				return i;
			}
		}
		return -1;
	}

	public static int rulealt(ParserRuleContext r) {
		return rulealt(r.getRuleIndex(), r.getAltNumber());
	}

	/** Pack a rule index and an alternative number into the same 32-bit integer. */
	public static int rulealt(int rule, int alt) {
		if ( rule==-1 ) return -1;
		return rule<<16 | alt;
	}

	/** Return {rule index, rule alt number} */
	public static int[] unrulealt(int ra) {
		if ( ra==-1 ) return new int[] {-1, ATN.INVALID_ALT_NUMBER};
		return new int[] {(ra>>16)&0xFFFF,ra&0xFFFF};
	}

	public static int indentcat(int deltaFromLeftAncestor, int child) {
		return CAT_INDENT_FROM_ANCESTOR_CHILD| (deltaFromLeftAncestor<<8) | (child << 16);
	}

	public static int[] unindentcat(int v) {
		int deltaFromLeftAncestor = (v>>8)&0xFF;
		int child = (v>>16)&0xFFFF;
		return new int[] { deltaFromLeftAncestor, child };
	}

	public static int aligncat(int deltaFromLeftAncestor, int child) {
		return CAT_ALIGN_WITH_ANCESTOR_CHILD | (deltaFromLeftAncestor<<8) | (child << 16);
	}

	public static int[] triple(int v) {
		int deltaFromLeftAncestor = (v>>8)&0xFF;
		int child = (v>>16)&0xFFFF;
		return new int[] { deltaFromLeftAncestor, child };
	}

	public static int wscat(int n) {
		return CAT_INJECT_WS | (n<<8);
	}

	public static int nlcat(int n) {
		return CAT_INJECT_NL | (n<<8);
	}

	public static int unwscat(int v) {
		return v >> 8 & 0xFFFF;
	}

	public static int unnlcat(int v) {
		return v >> 8 & 0xFFFF;
	}

	// '\n' (before list, before sep, after sep, after last element)
	public static int listform(int[] ws) {
		boolean[] nl = {
			ws[0]=='\n',
			ws[1]=='\n',
			ws[2]=='\n',
			ws[3]=='\n'
		};
		return
			(nl[0]?0x01000000:0) |
			(nl[1]?0x00010000:0) |
			(nl[2]?0x00000100:0) |
			(nl[3]?0x00000001:0);
	}

	public static int[] unlistform(int v) {
		return new int[] { v>>24&0xFF, v>>16&0xFF, v>>8&0xFF, v & 0xFF };
	}
}
