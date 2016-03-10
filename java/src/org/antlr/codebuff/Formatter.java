package org.antlr.codebuff;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.Map;
import java.util.Vector;

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
	protected int currentIndent = 0;

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
		newlineClassifier = new CodekNNClassifier(corpus, CollectFeatures.FEATURES_INJECT_NL);
		wsClassifier = new CodekNNClassifier(corpus, CollectFeatures.FEATURES_INJECT_WS);
		indentClassifier = new CodekNNClassifier(corpus, CollectFeatures.FEATURES_INDENT);
		alignClassifier = new CodekNNClassifier(corpus, CollectFeatures.FEATURES_ALIGN);
		k = (int)Math.sqrt(corpus.X.size());
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
			tokenToNodeMap = CollectFeatures.indexTree(root);
		}

		tokens.seek(0);
		Token secondToken = tokens.LT(2);
		String prefix = tokens.getText(Interval.of(0, secondToken.getTokenIndex()));
		output.append(prefix);

		realTokens = CollectFeatures.getRealTokens(tokens);
		for (int i = 2; i<realTokens.size(); i++) { // can't process first 2 tokens
			int tokenIndexInStream = realTokens.get(i).getTokenIndex();
			processToken(i, tokenIndexInStream);
		}
		return output.toString();
	}

	public void processToken(int indexIntoRealTokens, int tokenIndexInStream) {
		CommonToken curToken = (CommonToken)tokens.get(tokenIndexInStream);
		String tokText = curToken.getText();

		int[] features = CollectFeatures.getNodeFeatures(tokenToNodeMap, doc, tokenIndexInStream, line, tabSize);
		// must set "prev end column" value as token stream doesn't have it;
		// we're tracking it as we emit tokens
		features[CollectFeatures.INDEX_PREV_END_COLUMN] = charPosInLine;

		int injectNewline = newlineClassifier.classify(k, features, corpus.injectNewlines, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		int alignWithPrevious = alignClassifier.classify(k, features, corpus.alignWithPrevious, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		int indent = indentClassifier.classify(k, features, corpus.indent, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		int ws = wsClassifier.classify(k, features, corpus.injectWS, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);

		TokenPositionAnalysis tokenPositionAnalysis =
			getTokenAnalysis(features, indexIntoRealTokens, tokenIndexInStream, injectNewline, alignWithPrevious, indent, ws);
		analysis.setSize(tokenIndexInStream+1);
		analysis.set(tokenIndexInStream, tokenPositionAnalysis);

		if ( injectNewline>0 ) {
			output.append(Tool.newlines(injectNewline));
			line++;
			TerminalNode node = tokenToNodeMap.get(tokens.get(tokenIndexInStream));
			ParserRuleContext parent = (ParserRuleContext)node.getParent();
			int myIndex = 0;
			ParserRuleContext earliestAncestor = CollectFeatures.earliestAncestorStartingAtToken(parent, curToken);
			if ( earliestAncestor!=null ) {
				ParserRuleContext commonAncestor = earliestAncestor.getParent();
				List<ParserRuleContext> siblings = commonAncestor.getRuleContexts(earliestAncestor.getClass());
				myIndex = siblings.indexOf(earliestAncestor);
			}
			if ( myIndex>0 && alignWithPrevious>0 ) { // align with first sibling's start token
				ParserRuleContext commonAncestor = earliestAncestor.getParent();
				List<ParserRuleContext> siblings = commonAncestor.getRuleContexts(earliestAncestor.getClass());
				ParserRuleContext firstSibling = siblings.get(0);
				Token firstSiblingStartToken = firstSibling.getStart();
				// align but don't update currentIndent
				charPosInLine = firstSiblingStartToken.getCharPositionInLine();
				output.append(Tool.spaces(charPosInLine));
			}
			else {
				currentIndent += indent;
				if ( currentIndent<0 ) currentIndent = 0; // don't allow bad indents to accumulate
				charPosInLine = currentIndent;
				output.append(Tool.spaces(currentIndent));
			}
		}
		else {
			// inject whitespace instead of \n?
			output.append(Tool.spaces(ws));
			charPosInLine += ws;
		}
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
		int actualIndent = originalCurToken.getCharPositionInLine()-currentIndent;
		boolean actualAlign = CollectFeatures.isAlignedWithFirstSibling(tokenToNodeMap, tokens, curToken);
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
			                                        CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		String alignAnalysis =alignPredictionString+"\n"+
			alignClassifier.getPredictionAnalysis(k, features, corpus.alignWithPrevious,
			                                      CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		String indentAnalysis =indentPredictionString+"\n"+
			indentClassifier.getPredictionAnalysis(k, features, corpus.indent,
			                                       CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		String wsAnalysis =wsPredictionString+"\n"+
			wsClassifier.getPredictionAnalysis(k, features, corpus.injectWS,
			                                   CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
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
