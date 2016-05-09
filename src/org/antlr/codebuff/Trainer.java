package org.antlr.codebuff;

import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.codebuff.misc.ParentSiblingListKey;
import org.antlr.codebuff.walkers.CollectTokenDependencies;
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
import java.util.Vector;

import static org.antlr.codebuff.FeatureType.BOOL;
import static org.antlr.codebuff.FeatureType.INFO_CHARPOS;
import static org.antlr.codebuff.FeatureType.INFO_FILE;
import static org.antlr.codebuff.FeatureType.INFO_LINE;

/** Collect feature vectors trained on a single file.
 *
 *  The primary results are: (X, Y1, Y2)
 *  For each feature vector, features[i], injectWhitespace[i] and align[i] tell us
 *  the decisions associated with that context in a corpus file.
 *  After calling {@link #computeFeatureVectors()}, those lists
 *  are available.
 *
 *  There is no shared state computed by this object, only static defs
 *  of feature types and category constants.
 */
public class Trainer {
	public static final double MAX_WS_CONTEXT_DIFF_THRESHOLD = 1.0/7; // 7 features; allow one fault
	public static final double MAX_ALIGN_CONTEXT_DIFF_THRESHOLD = 0.12; // allow 12% mismatch
	public static final double MAX_CONTEXT_DIFF_THRESHOLD2 = 0.50;

	/** When computing child indexes, we use this value for any child list
	 *  element other than the first one.  If a parent has just one X child,
	 *  we use the actual child index. If parent has two or more X children,
	 *  and we are not the first X, use CHILD_INDEX_LIST_ELEMENT. If first
	 *  of two or more X children, use actual child index.
	 */
	public static final int CHILD_INDEX_LIST_ELEMENT = 1_111_111_111;

	public static final int LIST_PREFIX         = 0;
	public static final int LIST_FIRST_ELEMENT  = 1;
	public static final int LIST_FIRST_SEPARATOR= 2;
	public static final int LIST_SEPARATOR      = 3;
	public static final int LIST_SUFFIX         = 4;
	public static final int LIST_MEMBER         = 1_111_111_111;

	// Feature values for pair on diff lines feature
	public static final int NOT_PAIR = -1;
	public static final int PAIR_ON_SAME_LINE = 0;
	public static final int PAIR_ON_DIFF_LINE = 1;

	// Categories for newline, whitespace. CAT_INJECT_NL+n<<8 or CAT_INJECT_WS+n<<8
	public static final int CAT_NO_WS = 0;
	public static final int CAT_INJECT_NL = 100;
	public static final int CAT_INJECT_WS = 200;

	// Categories for alignment/indentation
	public static final int CAT_NO_ALIGNMENT = 0;

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
	public static final int INDEX_MATCHING_TOKEN_DIFF_LINE      = 4; // during ws prediction, indicates current line on same as matching symbol
	public static final int INDEX_FIRST_ON_LINE		            = 5; // a \n right before this token?
	public static final int INDEX_MEMBER_OVERSIZE_LIST          = 6; // -1 if we don't know; false means list but not big list
	public static final int INDEX_LIST_ELEMENT_TYPE             = 7; // see LIST_PREFIX, etc...
	public static final int INDEX_CUR_TOKEN_CHILD_INDEX         = 8; // left ancestor
	public static final int INDEX_EARLIEST_LEFT_ANCESTOR        = 9;
	public static final int INDEX_ANCESTORS_CHILD_INDEX         = 19; // left ancestor
	public static final int INDEX_ANCESTORS_PARENT_RULE         = 11;
	public static final int INDEX_ANCESTORS_PARENT_CHILD_INDEX  = 12;
	public static final int INDEX_ANCESTORS_PARENT2_RULE        = 13;
	public static final int INDEX_ANCESTORS_PARENT2_CHILD_INDEX = 14;
	public static final int INDEX_ANCESTORS_PARENT3_RULE        = 15;
	public static final int INDEX_ANCESTORS_PARENT3_CHILD_INDEX = 16;
	public static final int INDEX_ANCESTORS_PARENT4_RULE        = 17;
	public static final int INDEX_ANCESTORS_PARENT4_CHILD_INDEX = 18;

	public static final int INDEX_INFO_FILE                     = 19;
	public static final int INDEX_INFO_LINE                     = 20;
	public static final int INDEX_INFO_CHARPOS                  = 21;

	public static final int NUM_FEATURES                        = 22;
	public static final int ANALYSIS_START_TOKEN_INDEX          = 1; // we use current and previous token in context so can't start at index 0

	public static FeatureMetaData[] FEATURES_INJECT_WS = { // inject ws or nl
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 1),
		new FeatureMetaData(BOOL, new String[] {"Strt", "line"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "right ancestor"}, 1),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 1),
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		new FeatureMetaData(BOOL, new String[] {"Big", "list"}, 2),
		new FeatureMetaData(FeatureType.INT,   new String[] {"List", "elem."}, 1),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "left ancestor"}, 1),
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		new FeatureMetaData(INFO_FILE, new String[] {"", "file"}, 0),
		new FeatureMetaData(INFO_LINE, new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public static FeatureMetaData[] FEATURES_HPOS = {
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 1),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "right ancestor"}, 1), // TODO: candidate for removal
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"Pair", "dif\\n"}, 1),
		new FeatureMetaData(BOOL, new String[] {"Strt", "line"}, 4),
		new FeatureMetaData(BOOL, new String[] {"Big", "list"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"List", "elem."}, 2),
		new FeatureMetaData(FeatureType.INT,   new String[] {"token", "child index"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "left ancestor"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"ancestor", "child index"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"", "parent"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"parent", "child index"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"", "parent^2"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"parent^2", "child index"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"", "parent^3"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"parent^3", "child index"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"", "parent^4"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"parent^4", "child index"}, 1),
		new FeatureMetaData(INFO_FILE, new String[] {"", "file"}, 0),
		new FeatureMetaData(INFO_LINE, new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public static FeatureMetaData[] FEATURES_ALL = {
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 1),
		new FeatureMetaData(BOOL, new String[] {"Strt", "line"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "right ancestor"}, 1),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"Pair", "dif\\n"}, 1),
		new FeatureMetaData(BOOL, new String[] {"Strt", "line"}, 1),
		new FeatureMetaData(BOOL, new String[] {"Big", "list"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"List", "elem."}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"token", "child index"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "left ancestor"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"ancestor", "child index"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"", "parent"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"parent", "child index"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"", "parent^2"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"parent^2", "child index"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"", "parent^3"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"parent^3", "child index"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"", "parent^4"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"parent^4", "child index"}, 1),
		new FeatureMetaData(INFO_FILE, new String[] {"", "file"}, 0),
		new FeatureMetaData(INFO_LINE, new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	protected Corpus corpus;
	protected InputDocument doc;
	protected ParserRuleContext root;
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected int indentSize;

	// training results:
	protected Vector<int[]> featureVectors;
	protected Vector<Integer> injectWhitespace;
	protected Vector<Integer> hpos;

	/** Make it fast to get a node for a specific token */
	protected Map<Token, TerminalNode> tokenToNodeMap = null;

	public Trainer(Corpus corpus, InputDocument doc, int indentSize) {
		this.corpus = corpus;
		this.doc = doc;
		this.root = doc.tree;
		this.tokens = doc.tokens;
		this.indentSize = indentSize;
	}

	public void computeFeatureVectors() {
		List<Token> realTokens = getRealTokens(tokens);

		tokenToNodeMap = indexTree(root);

		// make space for n feature vectors and decisions, one for each token
		// from stream, including hidden tokens (though hidden tokens have no
		// entries in featureVectors, injectWhitespace, align.
		// Index i in features, decisions are token i
		// for token index from stream, not index into purely real tokens list.
		int n = tokens.size();
		featureVectors = new Vector<>(n); // use vector so we can set ith value
		featureVectors.setSize(n);
		injectWhitespace = new Vector<>(n);
		injectWhitespace.setSize(n);
		hpos = new Vector<>(n);
		hpos.setSize(n);

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

		int aligned = CAT_NO_ALIGNMENT ;
		if ( (injectNL_WS&0xFF)==CAT_INJECT_NL ) {
			TerminalNode node = tokenToNodeMap.get(curToken);
			aligned = getAlignmentCategory(doc, node, indentSize);
		}

		// track feature -> injectws, align decisions for token i
		featureVectors.set(i, features);
		injectWhitespace.set(i, injectNL_WS);
		hpos.set(i, aligned);
	}

	public static int getInjectWSCategory(CommonTokenStream tokens, int i) {
		int precedingNL = getPrecedingNL(tokens, i); // how many lines to inject

		Token curToken = tokens.get(i);
		tokens.seek(i); // seek so that LT(1) is tokens.get(i);
		Token prevToken = tokens.LT(-1);

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
		doc.tokens.seek(curToken.getTokenIndex()); // seek so that LT(-1) is previous real token
		Token prevToken = doc.tokens.LT(-1);

		// at a newline, are we aligned with a prior sibling (in a list) etc...
		ParserRuleContext earliestLeftAncestor = earliestAncestorStartingWithToken(node);
		Pair<ParserRuleContext, Integer> pair =
			earliestAncestorWithChildStartingAtCharPos(earliestLeftAncestor, curToken, curToken.getCharPositionInLine());
		String[] ruleNames = doc.parser.getRuleNames();
		if ( pair!=null ) {
			int deltaFromLeftAncestor = getDeltaToAncestor(earliestLeftAncestor, pair.a);
			alignInfo = new Pair<>(deltaFromLeftAncestor, pair.b);
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

		if ( columnDelta!=0 ) {
			int indentedFromPos = curToken.getCharPositionInLine()-indentSize;
			pair = earliestAncestorWithChildStartingAtCharPos(earliestLeftAncestor, curToken, indentedFromPos);
			if ( pair!=null ) {
				int deltaFromLeftAncestor = getDeltaToAncestor(earliestLeftAncestor, pair.a);
				indentInfo = new Pair<>(deltaFromLeftAncestor, pair.b);
//				int ruleIndex = pair.a.getRuleIndex();
//				System.out.printf("INDENT %s %s i=%d %s %s\n",
//				                  curToken,
//				                  ruleNames[ruleIndex],
//				                  pair.b, indentInfo, doc.fileName);
			}
		}

		// If both align and indent from ancestor child exist, choose closest (lowest delta up tree)
		if ( alignInfo!=null && indentInfo!=null ) {
			if ( alignInfo.a < indentInfo.a ) {
				return aligncat(alignInfo.a, alignInfo.b);
			}
			return indentcat(indentInfo.a, indentInfo.b);
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

		return CAT_NO_ALIGNMENT;
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

	// if we have non-ws tokens like comments, we only count ws after last comment
	public static List<Token> getPreviousWS(CommonTokenStream tokens, int i) {
		List<Token> hiddenTokensToLeft = tokens.getHiddenTokensToLeft(i);
		if ( hiddenTokensToLeft==null ) return null;
		if ( hasCommentToken(hiddenTokensToLeft) ) {
			for (int j = hiddenTokensToLeft.size()-1; j>=0; j--) {
				Token hidden = hiddenTokensToLeft.get(j);
				String hiddenText = hidden.getText();
				if ( !hiddenText.matches("\\s+") ) {
					return hiddenTokensToLeft.subList(j+1, hiddenTokensToLeft.size());
				}
			}
			return null;
		}
		else {
			return hiddenTokensToLeft;
		}
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

	/** Walk upwards from node until we find p.start at char position and p.start
	 *  is first token on a line; return null if there is no such ancestor p.
	 */
	public ParserRuleContext earliestAncestorStartingAtCharPos(ParserRuleContext node, int charpos) {
		ParserRuleContext p = node;
		while ( p!=null ) {
			if ( isFirstOnLine(p.getStart()) && p.getStart().getCharPositionInLine()==charpos ) {
				return p;
			}
			p = p.getParent();
		}
		return null;
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

	public boolean isFirstOnLine(Token t) {
		tokens.seek(t.getTokenIndex()); // LT(1)
		Token prevToken = tokens.LT(-1);
		if ( prevToken==null ) {
			return true; // if we are first token, must be first on line
		}
		return t.getLine()>prevToken.getLine();
	}

	public int[] getFeatures(int i)	{
		CodeBuffTokenStream tokens = doc.tokens;
		TerminalNode node = tokenToNodeMap.get(tokens.get(i));
		if ( node==null ) {
			System.err.println("### No node associated with token "+tokens.get(i));
			return null;
		}

		Token curToken = node.getSymbol();
		tokens.seek(i); // seek so that LT(1) is tokens.get(i);
		Token prevToken = tokens.LT(-1);

		boolean prevTokenStartsLine = false;
		if ( tokens.index()-2 >= 0 ) {
			if ( tokens.LT(-2)!=null ) {
				prevTokenStartsLine = tokens.LT(-1).getLine()>tokens.LT(-2).getLine();
			}
		}

		int matchingSymbolOnDiffLine = getMatchingSymbolOnDiffLine(corpus, doc, node, curToken.getLine());

		boolean curTokenStartsNewLine = curToken.getLine()>prevToken.getLine();

		int[] features = getContextFeatures(tokenToNodeMap, doc, i);

		setListInfoFeatures(corpus.tokenToListInfo, features, curToken);

		features[INDEX_PREV_FIRST_ON_LINE]       = prevTokenStartsLine ? 1 : 0;
		features[INDEX_MATCHING_TOKEN_DIFF_LINE] = matchingSymbolOnDiffLine;
		features[INDEX_FIRST_ON_LINE]            = curTokenStartsNewLine ? 1 : 0;

		return features;
	}

	/** Get the token type and tree ancestor features. These are computed
	 *  the same for both training and formatting.
	 */
	public static int[] getContextFeatures(Map<Token, TerminalNode> tokenToNodeMap,
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

		tokens.seek(i); // seek so that LT(1) is tokens.get(i);
		// Get context information for previous token
		Token prevToken = tokens.LT(-1);
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

	public static String getSiblingsText(List<? extends ParserRuleContext> siblings) {
		StringBuilder buf = new StringBuilder();
		for (ParserRuleContext sib : siblings) {
			buf.append(sib.getText());
		}
		return buf.toString();
	}

	public static String getText(List<? extends Token> tokens) {
		if ( tokens==null ) return "";
		StringBuilder buf = new StringBuilder();
		for (Token sib : tokens) {
			buf.append(sib.getText());
		}
		return buf.toString();
	}


	public static int getMatchingSymbolOnDiffLine(Corpus corpus,
	                                              InputDocument doc,
												  TerminalNode node,
												  int line)
	{
		TerminalNode matchingLeftNode = getMatchingLeftSymbol(corpus, doc, node);
		if (matchingLeftNode != null) {
//			System.out.println(node.getPayload()+" matches with "+matchingLeftNode.getSymbol());
			int matchingLeftTokenLine = matchingLeftNode.getSymbol().getLine();
			return matchingLeftTokenLine != line ? PAIR_ON_DIFF_LINE : PAIR_ON_SAME_LINE;
		}
		return NOT_PAIR;
	}

	/** Walk upwards checking for an ancestor that is a sibling list element.
	 *  Only consider lists identified by the corpus if there are more than
	 *  one actual elements in the list for node.  For example, a grammar
	 *  alt with just element X is in a list from corpus as alternative:element
	 *  is a pair in rootAndChildListPairs. But, it is a singleton list. Ignore
	 *  it and look upwards to the altList:alternative pair and see if there
	 *  is more than one alt in the altList.
	 *
	 *  The earliestLeftAncestor is the highest child we'll look at for
	 *  efficiency reasons.
	 */
	public static ParserRuleContext getMemberOfSiblingList(Corpus corpus,
	                                                       TerminalNode node,
	                                                       ParserRuleContext earliestLeftAncestor)
	{
		ParserRuleContext child = (ParserRuleContext)node.getParent();
		if ( child==null ) return null;
		ParserRuleContext parent = child.getParent();
		ParserRuleContext childMemberOfList = null;
		while ( parent!=null ) {
			ParentSiblingListKey pair = new ParentSiblingListKey(parent, child, node.getSymbol().getType());
			if ( corpus.rootAndChildListStats.containsKey(pair) ) {
				// count children
				List<? extends ParserRuleContext> siblings = parent.getRuleContexts(child.getClass());
//				if ( siblings.size()>1 ) {
					childMemberOfList = child;
					break; // stop at FIRST opportunity up the tree
//				}
			}
			if ( child==earliestLeftAncestor ) break; // we've hit last opportunity to check for sibling list
			child = parent;
			parent = parent.getParent();
		}

		if ( childMemberOfList!=null ) {
//			child = childMemberOfList;
//			parent = childMemberOfList.getParent();
//			List<? extends ParserRuleContext> siblings = parent.getRuleContexts(childMemberOfList.getClass());
//			int len = getSiblingsLength(siblings);
//			Quad<Integer, Integer, Integer, Integer> pair = new Quad<>(
//				parent.getRuleIndex(), parent.getAltNumber(),
//				child.getRuleIndex(), child.getAltNumber()
//			);
//			Triple<Integer, Integer, Integer> info = rootAndChildListPairs.get(pair);
//			System.out.println(StringUtils.abbreviate(parent.getText(),30)+"; "+len+" actual vs "+info);
		}

		return childMemberOfList;
	}

	/** Walk upwards checking for an ancestor that is a sibling list element.
	 *  Only consider lists identified by the corpus if there are more than
	 *  one actual elements in the list for node.  For example, a grammar
	 *  alt with just element X is in a list from corpus as alternative:element
	 *  is a pair in rootAndChildListPairs. But, it is a singleton list. Ignore
	 *  it and look upwards to the altList:alternative pair and see if there
	 *  is more than one alt in the altList.
	 *
	 *  The earliestLeftAncestor is the highest child we'll look at for
	 *  efficiency reasons.
	 */
	public static ParserRuleContext getEarliestMemberOfSiblingList(
		Corpus corpus,
		TerminalNode node,
		ParserRuleContext earliestLeftAncestor
	)
	{
		ParserRuleContext child = (ParserRuleContext)node.getParent();
		if ( child==null ) return null;
		ParserRuleContext parent = child.getParent();
		ParserRuleContext childMemberOfList = null; // track last good match we found
		while ( parent!=null ) {
			ParentSiblingListKey pair = new ParentSiblingListKey(parent, child, node.getSymbol().getType());
			if ( corpus.rootAndChildListStats.containsKey(pair) ) {
				childMemberOfList = child;
			}
			if ( child==earliestLeftAncestor ) break; // we've hit last opportunity to check for sibling list
			child = parent;
			parent = parent.getParent();
		}
		return childMemberOfList;
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
			List<Pair<Integer, Integer>> pairs = corpus.ruleToPairsBag.get(ruleName);
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

	public List<int[]> getFeatureVectors() {
		return BuffUtils.filter(featureVectors, v -> v!=null);
	}

	public List<Integer> getInjectWhitespace() {
		return BuffUtils.filter(injectWhitespace, v -> v!=null);
	}

	public List<Integer> getHPos() {
		return BuffUtils.filter(hpos, v -> v!=null);
	}

	public List<int[]> getTokenToFeatureVectors() {
		return featureVectors;
	}

	public List<Integer> getTokenInjectWhitespace() {
		return injectWhitespace;
	}

	public List<Integer> getTokenAlign() {
		return hpos;
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
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
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
				case COLWIDTH:
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
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TOKEN_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center(FEATURES[i].abbrevHeaderRows[0], displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TOKEN_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center(FEATURES[i].abbrevHeaderRows[1], displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TOKEN_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center("("+((int)FEATURES[i].mismatchCost)+")", displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TOKEN_TYPE ) {
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
				return CHILD_INDEX_LIST_ELEMENT;
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
