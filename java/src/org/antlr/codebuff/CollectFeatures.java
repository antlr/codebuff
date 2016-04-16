package org.antlr.codebuff;

import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.Quad;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;
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

public class CollectFeatures {
	public static final double MAX_CONTEXT_DIFF_THRESHOLD = 0.12;
	public static final double MAX_CONTEXT_DIFF_THRESHOLD2 = 0.30;

	/** When computing child indexes, we use this value for any child list
	 *  element other than the first one.  If a parent has just one X child,
	 *  we use the actual child index. If parent has two or more X children,
	 *  and we are not the first X, use CHILD_INDEX_LIST_ELEMENT. If first
	 *  of two or more X children, use actual child index.
	 */
	public static final int CHILD_INDEX_LIST_ELEMENT = 1_111_111_111;

	public static final int MEMBER_INDEX_LIST_ELEMENT = 1_111_111_111;

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
	public static final int INDEX_CUR_TYPE                      = 3;
	public static final int INDEX_MATCHING_TOKEN_DIFF_LINE      = 4; // during ws prediction, indicates current line on same as matching symbol
	public static final int INDEX_FIRST_ON_LINE		            = 5; // a \n right before this token?
	public static final int INDEX_INDEX_IN_LIST                 = 6; // either -1 (not member of list), 0 (first in list), or INDEX_LIST_ELEMENT
	public static final int INDEX_LIST_WIDTH                    = 7;
	public static final int INDEX_EARLIEST_LEFT_ANCESTOR        = 8;
	public static final int INDEX_ANCESTORS_CHILD_INDEX         = 9;
	public static final int INDEX_ANCESTORS_PARENT_RULE         = 10;
	public static final int INDEX_ANCESTORS_PARENT_CHILD_INDEX  = 11;
	public static final int INDEX_ANCESTORS_PARENT2_RULE        = 12;
	public static final int INDEX_ANCESTORS_PARENT2_CHILD_INDEX = 13;
	public static final int INDEX_ANCESTORS_PARENT3_RULE        = 14;
	public static final int INDEX_ANCESTORS_PARENT3_CHILD_INDEX = 15;
	public static final int INDEX_ANCESTORS_PARENT4_RULE        = 16;
	public static final int INDEX_ANCESTORS_PARENT4_CHILD_INDEX = 17;

	public static final int INDEX_INFO_FILE                     = 18;
	public static final int INDEX_INFO_LINE                     = 19;
	public static final int INDEX_INFO_CHARPOS                  = 20;

	public static final int NUM_FEATURES                        = 21;
	public static final int ANALYSIS_START_TOKEN_INDEX          = 1; // we use current and previous token in context so can't start at index 0

	public static FeatureMetaData[] FEATURES_INJECT_WS = { // inject ws or nl
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 1),
		new FeatureMetaData(FeatureType.BOOL,  new String[] {"Strt", "line"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "right ancestor"}, 1),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"Pair", "dif\\n"}, 1),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.INT,   new String[] {"List", "index"}, 1),
		new FeatureMetaData(FeatureType.COLWIDTH,   new String[] {"List", "width"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(1)", "left ancestor"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"ancestor", "child index"}, 1),
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(FeatureType.INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public static FeatureMetaData[] FEATURES_ALIGN = {
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 1),
		FeatureMetaData.UNUSED,
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "right ancestor"}, 1),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"Pair", "dif\\n"}, 1),
		new FeatureMetaData(FeatureType.BOOL,  new String[] {"Strt", "line"}, 4),
		FeatureMetaData.UNUSED,
		FeatureMetaData.UNUSED,
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
		new FeatureMetaData(FeatureType.INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(FeatureType.INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	public static FeatureMetaData[] FEATURES_ALL = {
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(-1)"}, 1),
		new FeatureMetaData(FeatureType.BOOL,  new String[] {"Strt", "line"}, 1),
		new FeatureMetaData(FeatureType.RULE,  new String[] {"LT(-1)", "right ancestor"}, 1),
		new FeatureMetaData(FeatureType.TOKEN, new String[] {"", "LT(1)"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"Pair", "dif\\n"}, 1),
		new FeatureMetaData(FeatureType.BOOL,  new String[] {"Strt", "line"}, 1),
		new FeatureMetaData(FeatureType.INT,   new String[] {"List", "index"}, 1),
		new FeatureMetaData(FeatureType.COLWIDTH,   new String[] {"List", "width"}, 1),
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
		new FeatureMetaData(FeatureType.INFO_FILE,    new String[] {"", "file"}, 0),
		new FeatureMetaData(FeatureType.INFO_LINE,    new String[] {"", "line"}, 0),
		new FeatureMetaData(FeatureType.INFO_CHARPOS, new String[] {"char", "pos"}, 0)
	};

	protected Corpus corpus;
	protected InputDocument doc;
	protected ParserRuleContext root;
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected List<Token> realTokens;
	protected List<int[]> features = new ArrayList<>();
	protected List<Integer> injectWhitespace = new ArrayList<>();
	protected List<Integer> align = new ArrayList<>();

	protected int currentIndent = 0;

	protected Map<Token, TerminalNode> tokenToNodeMap = null;

	protected int tabSize;

	public CollectFeatures(InputDocument doc, int tabSize)
	{
		this.corpus = doc.corpus;
		this.doc = doc;
		this.root = doc.tree;
		this.tokens = doc.tokens;
		this.tabSize = tabSize;
	}

	public void computeFeatureVectors() {
		realTokens = getRealTokens(tokens);
		for (int i = ANALYSIS_START_TOKEN_INDEX; i<realTokens.size(); i++) { // can't process first token
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
		this.injectWhitespace.add(injectNL_WS);

		int columnDelta = 0;
		if ( precedingNL>0 ) { // && aligned!=1 ) {
			columnDelta = curToken.getCharPositionInLine() - currentIndent;
			currentIndent = curToken.getCharPositionInLine();
		}

		int aligned = CAT_NO_ALIGNMENT ;
		if ( precedingNL>0 ) {
			aligned = getAlignmentCategory(node, curToken, columnDelta);
		}

		this.align.add(aligned);

		this.features.add(features);
	}

	// at a newline, are we aligned with a prior sibling (in a list) etc...
	public int getAlignmentCategory(TerminalNode node, Token curToken, int columnDelta) {
		int aligned = CAT_NO_ALIGNMENT;

		// at a newline, are we aligned with a prior sibling (in a list) etc...
		ParserRuleContext earliestLeftAncestor = earliestAncestorStartingWithToken(node);
		Pair<ParserRuleContext, Integer> pair =
			earliestAncestorWithChildStartingAtCharPos(earliestLeftAncestor, curToken, curToken.getCharPositionInLine());
		if ( pair!=null ) {
			int deltaFromLeftAncestor = getDeltaToAncestor(earliestLeftAncestor, pair.a);
			aligned = aligncat(deltaFromLeftAncestor, pair.b);
//			System.out.printf("ALIGN %s %s i=%d %x %s\n",
//			                  curToken,
//			                  doc.parser.getRuleNames()[pair.a.getRuleIndex()],
//			                  pair.b, aligned, doc.fileName);
 		}
		else if ( columnDelta!=0 ) {
			int indentedFromPos = curToken.getCharPositionInLine()-Formatter.INDENT_LEVEL;
			pair = earliestAncestorWithChildStartingAtCharPos(earliestLeftAncestor, curToken, indentedFromPos);
			if ( pair!=null ) {
//				System.out.printf("INDENT %s %s i=%d %x %s\n",
//				                  curToken,
//				                  doc.parser.getRuleNames()[pair.a.getRuleIndex()],
//				                  pair.b, aligned, doc.fileName);
				int deltaFromLeftAncestor = getDeltaToAncestor(earliestLeftAncestor, pair.a);
				aligned = indentcat(deltaFromLeftAncestor, pair.b);
			}
			else {
				aligned = CAT_INDENT; // indent standard amount
			}
		}

		return aligned;
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
	public Pair<ParserRuleContext,Integer> earliestAncestorWithChildStartingAtCharPos(ParserRuleContext node, Token t, int charpos) {
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
			return null;
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
		ParserRuleContext earliestLeftAncestorParent = earliestLeftAncestor.getParent();

		ParserRuleContext earliestLeftAncestorParent2 = earliestLeftAncestorParent!=null ? earliestLeftAncestorParent.getParent() : null;
		ParserRuleContext earliestLeftAncestorParent3 = earliestLeftAncestorParent2!=null ? earliestLeftAncestorParent2.getParent() : null;
		ParserRuleContext earliestLeftAncestorParent4 = earliestLeftAncestorParent3!=null ? earliestLeftAncestorParent3.getParent() : null;

		int matchingSymbolOnDiffLine = getMatchingSymbolOnDiffLine(doc, node, line);

		int memberIndexOfNonSingletonList = -1; // >=0 if |list|>1.  -1 implies not member of list or singleton list
		int nonSingletonListWidth = -1;

		ParserRuleContext childOfSiblingList =
			getMemberOfSiblingList(doc.corpus, node, earliestLeftAncestor);
		if ( childOfSiblingList!=null ) {
			ParserRuleContext siblingListParent = childOfSiblingList.getParent();
			List<? extends ParserRuleContext> siblings = siblingListParent.getRuleContexts(childOfSiblingList.getClass());

			if ( siblings.size()>1 ) {
				memberIndexOfNonSingletonList = siblings.indexOf(childOfSiblingList);
				String siblingsText = getSiblingsText(siblings);
				nonSingletonListWidth = siblingsText.length();

//				String[] ruleNames = doc.parser.getRuleNames();
//				String sibParent = ruleNames[siblingListParent.getRuleIndex()];
//				String child = ruleNames[childOfSiblingList.getRuleIndex()];
//				child = child.replace("Context", "");
//				sibParent = sibParent.replace("Context", "");
//				System.out.println(StringUtils.abbreviate(siblingsText,30)+
//					                   "  "+sibParent+":"+siblingListParent.getAltNumber()+"->"+child+
//					                   ":"+childOfSiblingList.getAltNumber()+" "+siblings.indexOf(childOfSiblingList)+" of "+siblings.size()+" sibs"+
//				                  ": len="+len);
			}
		}
		else { // maybe we're a separator?
			childOfSiblingList = getFirstSiblingFromSeparatorNode(doc.corpus, node);
			if ( childOfSiblingList!=null ) {
				ParserRuleContext siblingListParent = childOfSiblingList.getParent();
				List<? extends ParserRuleContext> siblings = siblingListParent.getRuleContexts(childOfSiblingList.getClass());
				if ( siblings.size()>1 ) {
					String siblingsText = getSiblingsText(siblings);
					nonSingletonListWidth = siblingsText.length();
					memberIndexOfNonSingletonList = MEMBER_INDEX_LIST_ELEMENT;
				}
			}
		}

		boolean curTokenStartsNewLine = tokens.LT(1).getLine()>tokens.LT(-1).getLine();
		boolean prevTokenStartsNewLine = false;
		if ( tokens.index()-2 >= 0 ) {
			if ( tokens.LT(-2)!=null ) {
				prevTokenStartsNewLine = tokens.LT(-1).getLine()>tokens.LT(-2).getLine();
			}
		}

		int[] features = {
			tokens.LT(-1).getType(),
			prevTokenStartsNewLine ? 1 : 0,
			rulealt(prevEarliestAncestorRuleIndex,prevEarliestAncestorRuleAltNum),
			tokens.LT(1).getType(),
			matchingSymbolOnDiffLine,
			curTokenStartsNewLine ? 1 : 0,
			memberIndexOfNonSingletonList>0 ? MEMBER_INDEX_LIST_ELEMENT : memberIndexOfNonSingletonList,
			nonSingletonListWidth,
			rulealt(earliestLeftAncestor.getRuleIndex(),earliestLeftAncestor.getAltNumber()),
			getChildIndex(node),
			earliestLeftAncestorParent!=null ? rulealt(earliestLeftAncestorParent.getRuleIndex(), earliestLeftAncestorParent.getAltNumber()) : -1,
			getChildIndex(earliestLeftAncestor),
			earliestLeftAncestorParent2!=null ? rulealt(earliestLeftAncestorParent2.getRuleIndex(), earliestLeftAncestorParent2.getAltNumber()) : -1,
			getChildIndex(earliestLeftAncestorParent),
			earliestLeftAncestorParent3!=null ? rulealt(earliestLeftAncestorParent3.getRuleIndex(), earliestLeftAncestorParent3.getAltNumber()) : -1,
			getChildIndex(earliestLeftAncestorParent2),
			earliestLeftAncestorParent4!=null ? rulealt(earliestLeftAncestorParent4.getRuleIndex(), earliestLeftAncestorParent4.getAltNumber()) : -1,
			getChildIndex(earliestLeftAncestorParent3),

			// info
			0, // dummy; we don't store file index into feature vector
			curToken.getLine(),
			curToken.getCharPositionInLine()
		};
		assert features.length == NUM_FEATURES;
		return features;
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

	public static int getMatchingSymbolOnDiffLine(InputDocument doc,
												  TerminalNode node,
												  int line)
	{
		TerminalNode matchingLeftNode = getMatchingLeftSymbol(doc, node);
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
			Quad<Integer,Integer,Integer,Integer> pair = new Quad<>(
				parent.getRuleIndex(), parent.getAltNumber(),
				child.getRuleIndex(), child.getAltNumber()
			);
			if ( corpus.rootAndChildListPairs.containsKey(pair) ) {
				// count children
				List<? extends ParserRuleContext> siblings = parent.getRuleContexts(child.getClass());
				if ( siblings.size()>1 ) {
					childMemberOfList = child;
					break; // stop at FIRST opportunity up the tree
				}
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

	/** If current node is a separator in a list, return the first subtree root
	 *  member of that sibling list.
	 */
	public static ParserRuleContext getFirstSiblingFromSeparatorNode(Corpus corpus, TerminalNode node) {
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		if ( parent==null ) return null;
		Triple<Integer, Integer, Integer> key =
			new Triple<>(parent.getRuleIndex(), parent.getAltNumber(), node.getSymbol().getType());
		Class<? extends ParserRuleContext> memberClass = corpus.listSeparators.get(key);
		if ( memberClass!=null ) {
			// separator of list so get the siblings
			List<? extends ParserRuleContext> siblings = parent.getRuleContexts(memberClass);
			if ( siblings.size()>1 ) {
				return siblings.get(0);
			}
		}
		return null;
	}

	public static TerminalNode getMatchingLeftSymbol(InputDocument doc,
													 TerminalNode node)
	{
		ParserRuleContext parent = (ParserRuleContext)node.getParent();
		int curTokensParentRuleIndex = parent.getRuleIndex();
		Token curToken = node.getSymbol();
		if (doc.corpus.ruleToPairsBag != null) {
			String ruleName = doc.parser.getRuleNames()[curTokensParentRuleIndex];
			List<Pair<Integer, Integer>> pairs = doc.corpus.ruleToPairsBag.get(ruleName);
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

	public List<int[]> getFeatures() {
		return features;
	}

	public List<Integer> getInjectWhitespace() {
		return injectWhitespace;
	}

	public List<Integer> getAlign() {
		return align;
	}

	public static String _toString(FeatureMetaData[] FEATURES, InputDocument doc, int[] features) {
		Vocabulary v = doc.parser.getVocabulary();
		String[] ruleNames = doc.parser.getRuleNames();
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TYPE ) {
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
					if ( features[i]>=0 ) {
						buf.append(String.format("%"+displayWidth+"s", StringUtils.center(String.valueOf(features[i]),displayWidth)));
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
			if ( i==INDEX_CUR_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center(FEATURES[i].abbrevHeaderRows[0], displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center(FEATURES[i].abbrevHeaderRows[1], displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TYPE ) {
				buf.append("| "); // separate prev from current tokens
			}
			int displayWidth = FEATURES[i].type.displayWidth;
			buf.append(StringUtils.center("("+((int)FEATURES[i].mismatchCost)+")", displayWidth));
		}
		buf.append("\n");
		for (int i=0; i<FEATURES.length; i++) {
			if ( FEATURES[i].type.equals(FeatureType.UNUSED) ) continue;
			if ( i>0 ) buf.append(" ");
			if ( i==INDEX_CUR_TYPE ) {
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

	public static int getChildIndex(ParseTree t) {
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
		else {
			TerminalNode node = (TerminalNode)t;
			List<TerminalNode> siblings =
				((ParserRuleContext)parent).getTokens(node.getSymbol().getType());
			if ( siblings.size()>1 && siblings.indexOf(t)>0 ) {
				return CHILD_INDEX_LIST_ELEMENT;
			}
		}
		// Either first of sibling list or not in a list.
		// Figure out which child index t is of parent
		for (int i = 0; i<parent.getChildCount(); i++) {
			if ( parent.getChild(i)==t ) {
				return i;
			}
		}
		return -1;
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

	public static int[] unaligncat(int v) {
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
}
