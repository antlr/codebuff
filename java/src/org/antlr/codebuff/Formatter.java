package org.antlr.codebuff;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.WritableToken;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import static org.antlr.codebuff.CollectFeatures.CAT_ALIGN_WITH_ANCESTORS_PARENT_FIRST_TOKEN;
import static org.antlr.codebuff.CollectFeatures.CAT_ALIGN_WITH_ANCESTOR_FIRST_TOKEN;
import static org.antlr.codebuff.CollectFeatures.CAT_ALIGN_WITH_LIST_FIRST_ELEMENT;
import static org.antlr.codebuff.CollectFeatures.CAT_ALIGN_WITH_PAIR;
import static org.antlr.codebuff.CollectFeatures.CAT_INDENT;
import static org.antlr.codebuff.CollectFeatures.CAT_NO_ALIGNMENT;
import static org.antlr.codebuff.CollectFeatures.FEATURES_ALIGN;
import static org.antlr.codebuff.CollectFeatures.FEATURES_INDENT;
import static org.antlr.codebuff.CollectFeatures.FEATURES_INJECT_NL;
import static org.antlr.codebuff.CollectFeatures.FEATURES_INJECT_WS;
import static org.antlr.codebuff.CollectFeatures.INDEX_FIRST_ON_LINE;
import static org.antlr.codebuff.CollectFeatures.INDEX_PREV_END_COLUMN;
import static org.antlr.codebuff.CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD;
import static org.antlr.codebuff.CollectFeatures.earliestAncestorEndingWithToken;
import static org.antlr.codebuff.CollectFeatures.getListSiblings;
import static org.antlr.codebuff.CollectFeatures.getMatchingLeftSymbol;
import static org.antlr.codebuff.CollectFeatures.getNodeFeatures;
import static org.antlr.codebuff.CollectFeatures.getRealTokens;
import static org.antlr.codebuff.CollectFeatures.getTokensOnPreviousLine;
import static org.antlr.codebuff.CollectFeatures.indexTree;
import static org.antlr.codebuff.CollectFeatures.isAlignedWithFirstSiblingOfList;

public class Formatter {
	protected final Corpus corpus;
	protected StringBuilder output = new StringBuilder();
	protected InputDocument doc;
	protected ParserRuleContext root;
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected List<CommonToken> originalTokens; // copy of tokens with line/col info
	protected List<Token> realTokens;           // just the real tokens from tokens

	protected Map<Token, TerminalNode> tokenToNodeMap = null;

	protected Vector<TokenPositionAnalysis> analysis = new Vector<>();

	protected CodekNNClassifier newlineClassifier;
	protected CodekNNClassifier wsClassifier;
	protected CodekNNClassifier indentClassifier;
	protected CodekNNClassifier alignClassifier;
	protected int k;

	protected int line = 1;
	protected int charPosInLine = 0;

	protected int tabSize;

	protected boolean debug_NL = true;
	protected int misclassified_NL = 0;
	protected int misclassified_WS = 0;

	public Formatter(Corpus corpus, InputDocument doc, int tabSize) {
		this.corpus = corpus;
		this.doc = doc;
		this.root = doc.tree;
		this.tokens = doc.tokens;
		this.originalTokens = Tool.copy(tokens);
		Tool.wipeLineAndPositionInfo(tokens);
		newlineClassifier = new CodekNNClassifier(corpus, FEATURES_INJECT_NL);
		wsClassifier = new CodekNNClassifier(corpus, FEATURES_INJECT_WS);
		indentClassifier = new CodekNNClassifier(corpus, FEATURES_INDENT);
		alignClassifier = new CodekNNClassifier(corpus, FEATURES_ALIGN);
//		k = (int)Math.sqrt(corpus.X.size());
		k = 7;
		this.tabSize = tabSize;
	}

	public String getOutput() {
		return output.toString();
	}

	public List<TokenPositionAnalysis> getAnalysisPerToken() {
		return analysis;
	}


	public String format() {
		if ( tokenToNodeMap == null ) {
			tokenToNodeMap = indexTree(root);
		}

		tokens.seek(0);
		WritableToken firstToken = (WritableToken)tokens.LT(1);
		WritableToken secondToken = (WritableToken)tokens.LT(2);
		// all tokens are wiped of line/col info so set them for first 2
		firstToken.setLine(1);
		firstToken.setCharPositionInLine(0);
		secondToken.setLine(1);
		secondToken.setCharPositionInLine(firstToken.getText().length());

		String prefix = tokens.getText(Interval.of(0, secondToken.getTokenIndex()));
		output.append(prefix);


		realTokens = getRealTokens(tokens);
		for (int i = 2; i<realTokens.size(); i++) { // can't process first 2 tokens
			int tokenIndexInStream = realTokens.get(i).getTokenIndex();
			processToken(i, tokenIndexInStream);
		}
		return output.toString();
	}

	public void processToken(int indexIntoRealTokens, int tokenIndexInStream) {
		CommonToken curToken = (CommonToken)tokens.get(tokenIndexInStream);
		String tokText = curToken.getText();

		int[] features = getNodeFeatures(tokenToNodeMap, doc, tokenIndexInStream, line, tabSize);
		// must set "prev end column" value as token stream doesn't have it;
		// we're tracking it as we emit tokens
		features[INDEX_PREV_END_COLUMN] = charPosInLine;

		int injectNewline = newlineClassifier.classify(k, features, corpus.injectNewlines, MAX_CONTEXT_DIFF_THRESHOLD);

		// getNodeFeatures() also doesn't know what line curToken is on. If \n, we need to find exemplars that start a line
		features[INDEX_FIRST_ON_LINE] = injectNewline; // use \n prediction to match exemplars for alignment

		int align = alignClassifier.classify(k, features, corpus.align, MAX_CONTEXT_DIFF_THRESHOLD);
		int indent = 0;
		//indentClassifier.classify(k, features, corpus.indent, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		int ws = wsClassifier.classify(k, features, corpus.injectWS, MAX_CONTEXT_DIFF_THRESHOLD);

		TokenPositionAnalysis tokenPositionAnalysis =
			getTokenAnalysis(features, indexIntoRealTokens, tokenIndexInStream, injectNewline, align, indent, ws);
		analysis.setSize(tokenIndexInStream+1);
		analysis.set(tokenIndexInStream, tokenPositionAnalysis);

		if ( ws==0 && cannotJoin(realTokens.get(indexIntoRealTokens-1), curToken) ) { // failsafe!
			ws = 1;
		}

		if ( injectNewline>0 ) {
			output.append(Tool.newlines(injectNewline));
			line++;
			charPosInLine = 0;

			List<Token> tokensOnPreviousLine = getTokensOnPreviousLine(tokens, tokenIndexInStream, line);
			Token firstTokenOnPrevLine = null;
			if ( tokensOnPreviousLine.size()>0 ) {
				firstTokenOnPrevLine = tokensOnPreviousLine.get(0);
			}

			TerminalNode node = tokenToNodeMap.get(curToken);
			ParserRuleContext parent = (ParserRuleContext)node.getParent();
			ParserRuleContext earliestRightAncestor = earliestAncestorEndingWithToken(parent, curToken);

			switch ( align ) {
				case CAT_INDENT :
					if ( firstTokenOnPrevLine!=null ) { // if not on first line, we can indent indent
						int indentedCol = firstTokenOnPrevLine.getCharPositionInLine() + 4;
						charPosInLine = indentedCol;
						output.append(Tool.spaces(indentedCol));
					}
					break;
				case CAT_ALIGN_WITH_ANCESTOR_FIRST_TOKEN :
					if ( earliestRightAncestor!=null ) {
						Token earliestRightAncestorStart = earliestRightAncestor.getStart();
						int linedUpCol = earliestRightAncestorStart.getCharPositionInLine();
						charPosInLine = linedUpCol;
						output.append(Tool.spaces(linedUpCol));
					}
					break;
				case CAT_ALIGN_WITH_ANCESTORS_PARENT_FIRST_TOKEN :
					if ( earliestRightAncestor!=null ) {
						ParserRuleContext earliestAncestorParent = earliestRightAncestor.getParent();
						if ( earliestAncestorParent!=null ) {
							Token earliestAncestorParentStart = earliestAncestorParent.getStart();
							int linedUpCol = earliestAncestorParentStart.getCharPositionInLine();
							charPosInLine = linedUpCol;
							output.append(Tool.spaces(linedUpCol));
						}
					}
					break;
				case CAT_ALIGN_WITH_LIST_FIRST_ELEMENT :
					List<ParserRuleContext> listSiblings = getListSiblings(tokenToNodeMap, curToken);
					if ( listSiblings!=null ) {
						ParserRuleContext firstSibling = listSiblings.get(0);
						int linedUpCol = firstSibling.getStart().getCharPositionInLine();
						charPosInLine = linedUpCol;
						output.append(Tool.spaces(linedUpCol));
					}
					break;
				case CAT_ALIGN_WITH_PAIR :
					TerminalNode matchingLeftSymbol = getMatchingLeftSymbol(doc, node);
					int linedUpCol = matchingLeftSymbol.getSymbol().getCharPositionInLine();
					charPosInLine = linedUpCol;
					output.append(Tool.spaces(linedUpCol));
					break;
				case CAT_NO_ALIGNMENT :
					break;
			}
//			if ( currentIndent<0 ) currentIndent = 0; // don't allow bad indents to accumulate
//			charPosInLine = currentIndent;
//			output.append(Tool.spaces(currentIndent));
		}
		else {
			// inject whitespace instead of \n?
			output.append(Tool.spaces(ws));
			charPosInLine += ws;
		}

//		if ( injectNewline>0 ) {
//			output.append(Tool.newlines(injectNewline));
//			line++;
//			TerminalNode node = tokenToNodeMap.get(tokens.get(tokenIndexInStream));
//			ParserRuleContext parent = (ParserRuleContext)node.getParent();
//			int myIndex = 0;
//			ParserRuleContext earliestAncestor = CollectFeatures.earliestAncestorStartingWithToken(parent, curToken);
//			if ( earliestAncestor!=null ) {
//				ParserRuleContext commonAncestor = earliestAncestor.getParent();
//				List<ParserRuleContext> siblings = commonAncestor.getRuleContexts(earliestAncestor.getClass());
//				myIndex = siblings.indexOf(earliestAncestor);
//			}
//			if ( false ) { //if ( myIndex>0 && align>0 ) { // align with first sibling's start token
//				ParserRuleContext commonAncestor = earliestAncestor.getParent();
//				List<ParserRuleContext> siblings = commonAncestor.getRuleContexts(earliestAncestor.getClass());
//				ParserRuleContext firstSibling = siblings.get(0);
//				Token firstSiblingStartToken = firstSibling.getStart();
//				// align but don't update currentIndent
//				charPosInLine = firstSiblingStartToken.getCharPositionInLine();
//				output.append(Tool.spaces(charPosInLine));
//			}
//			else {
//				currentIndent += indent;
//				if ( currentIndent<0 ) currentIndent = 0; // don't allow bad indents to accumulate
//				charPosInLine = currentIndent;
//				output.append(Tool.spaces(currentIndent));
//			}
//		}
//		else {
//			// inject whitespace instead of \n?
//			output.append(Tool.spaces(ws));
//			charPosInLine += ws;
//		}
		// update Token object with position information now that we are about
		// to emit it.
		curToken.setLine(line);
		curToken.setCharPositionInLine(charPosInLine);

		// emit
		int n = tokText.length();
		tokenPositionAnalysis.charIndexStart = output.length();
		tokenPositionAnalysis.charIndexStop = tokenPositionAnalysis.charIndexStart + n - 1;
		output.append(tokText);
		charPosInLine += n;
	}

	public TokenPositionAnalysis getTokenAnalysis(int[] features, int indexIntoRealTokens, int tokenIndexInStream,
	                                              int injectNewline,
	                                              int alignWithPrevious,
	                                              int indent,
	                                              int ws)
	{
		CommonToken curToken = (CommonToken)tokens.get(tokenIndexInStream);
		// compare prediction of newline against original, alert about any diffs
		CommonToken prevToken = originalTokens.get(curToken.getTokenIndex()-1);
		CommonToken originalCurToken = originalTokens.get(curToken.getTokenIndex());

		boolean failsafeTriggered = false;
		if ( ws==0 && cannotJoin(realTokens.get(indexIntoRealTokens-1), curToken) ) { // failsafe!
			ws = 1;
			failsafeTriggered = true;
		}

		boolean prevIsWS = prevToken.getType()==JavaLexer.WS;
		int actualNL = Tool.count(prevToken.getText(), '\n');
		int actualWS = Tool.count(prevToken.getText(), ' ');
		int actualIndent = originalCurToken.getCharPositionInLine()-0;// currentIndent;
		boolean actualAlign = isAlignedWithFirstSiblingOfList(tokenToNodeMap, tokens, curToken);
		String newlinePredictionString = String.format("### line %d: predicted %d \\n actual %s",
		                                               originalCurToken.getLine(), injectNewline, prevIsWS ? actualNL : "none");
		String alignPredictionString = String.format("### line %d: predicted %s actual %s",
		                                             originalCurToken.getLine(),
		                                             alignWithPrevious==1?"align":"unaligned",
		                                             actualAlign?"align":"unaligned");
		String indentPredictionString = String.format("### line %d: predicted indent %d actual %s",
		                                              originalCurToken.getLine(), indent, actualIndent);
		String wsPredictionString = String.format("### line %d: predicted %d ' ' actual %s",
		                                          originalCurToken.getLine(), ws, prevIsWS ? actualWS : "none");
		if ( failsafeTriggered ) {
			wsPredictionString += " (failsafe triggered)";
		}


		String newlineAnalysis = newlinePredictionString+"\n"+
			newlineClassifier.getPredictionAnalysis(k, features, corpus.injectNewlines,
			                                        MAX_CONTEXT_DIFF_THRESHOLD);
		String alignAnalysis =alignPredictionString+"\n"+
			alignClassifier.getPredictionAnalysis(k, features, corpus.align,
			                                      MAX_CONTEXT_DIFF_THRESHOLD);
		String indentAnalysis =indentPredictionString+"\n"+
			indentClassifier.getPredictionAnalysis(k, features, corpus.indent,
			                                       MAX_CONTEXT_DIFF_THRESHOLD);
		String wsAnalysis =wsPredictionString+"\n"+
			wsClassifier.getPredictionAnalysis(k, features, corpus.injectWS,
			                                   MAX_CONTEXT_DIFF_THRESHOLD);
		return new TokenPositionAnalysis(newlineAnalysis, alignAnalysis, indentAnalysis, wsAnalysis);
	}

	/** Do not join two words like "finaldouble" or numbers like "3double",
	 *  "double3", "34", (3 and 4 are different tokens) etc...
	 */
	public static boolean cannotJoin(Token prevToken, Token curToken) {
		String prevTokenText = prevToken.getText();
		char prevLastChar = prevTokenText.charAt(prevTokenText.length()-1);
		String curTokenText = curToken.getText();
		char curFirstChar = curTokenText.charAt(0);
		return Character.isLetterOrDigit(prevLastChar) && Character.isLetterOrDigit(curFirstChar);
	}
}
