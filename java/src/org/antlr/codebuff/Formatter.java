package org.antlr.codebuff;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.Map;

public class Formatter {
	protected final Corpus corpus;
	protected StringBuilder output = new StringBuilder();
	protected InputDocument doc;
	protected ParserRuleContext root;
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected List<CommonToken> originalTokens; // copy of tokens with line/col info

	protected Map<Token, TerminalNode> tokenToNodeMap = null;

//	protected CodekNNClassifier classifier;
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

	public String format() {
		if ( tokenToNodeMap == null ) {
			tokenToNodeMap = CollectFeatures.indexTree(root);
		}

		List<Token> realTokens = CollectFeatures.getRealTokens(tokens);
		for (int i = 2; i<realTokens.size(); i++) { // can't process first 2 tokens
			int tokenIndexInStream = realTokens.get(i).getTokenIndex();
			processToken(tokenIndexInStream);
		}
		return output.toString();
	}

	public void processToken(int i) {
		CommonToken curToken = (CommonToken)tokens.get(i);

		tokens.seek(i); // seek so that LT(1) is tokens.get(i);

		String tokText = curToken.getText();

		int[] features = CollectFeatures.getNodeFeatures(tokenToNodeMap, doc, i, line, tabSize);
		// must set "prev end column" value as token stream doesn't have it;
		// we're tracking it as we emit tokens
		features[CollectFeatures.INDEX_PREV_END_COLUMN] = charPosInLine;

//		int[] categories = classifier.classify(k, features, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		int injectNewline = newlineClassifier.classify(k, features, corpus.injectNewlines, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		int indent = indentClassifier.classify(k, features, corpus.indent, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		int ws = wsClassifier.classify(k, features, corpus.injectWS, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		int alignWithPrevious = alignClassifier.classify(k, features, corpus.alignWithPrevious, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);

		// compare prediction of newline against original, alert about any diffs
		CommonToken prevToken = originalTokens.get(curToken.getTokenIndex()-1);
		CommonToken originalCurToken = originalTokens.get(curToken.getTokenIndex());

		if ( debug_NL && prevToken.getType()==JavaLexer.WS ) {
			int actualNL = Tool.count(prevToken.getText(), '\n');
			if ( injectNewline!=actualNL ) {
				misclassified_NL++;
				doc.misclassifiedNewLineCount++;
				if (doc.dumpVotes) {
					System.out.println();
					System.out.printf("### line %d: predicted %d \\n actual %d:\n",
						originalCurToken.getLine(), injectNewline, actualNL);
					Tool.printOriginalFilePiece(doc, originalCurToken);
					newlineClassifier.dumpVotes = true;
					newlineClassifier.classify(k, features, corpus.injectNewlines, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
					newlineClassifier.dumpVotes = false;
				}
			}
			int actualWS = Tool.count(prevToken.getText(), ' ');
			if ( injectNewline==0 && ws!=actualWS ) {
				misclassified_WS++;
				doc.misclassifiedWSCount++;
				if (doc.dumpVotes) {
					System.out.println();
					System.out.printf("### line %d: predicted %d ' ' actual %d:\n",
						originalCurToken.getLine(), ws, actualWS);
					Tool.printOriginalFilePiece(doc, originalCurToken);
					wsClassifier.dumpVotes = true;
					wsClassifier.classify(k, features, corpus.injectWS, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
					wsClassifier.dumpVotes = false;
				}
			}
		}

		if ( injectNewline>0 ) {
			output.append(Tool.newlines(injectNewline));
			line++;
			TerminalNode node = tokenToNodeMap.get(tokens.get(i));
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
		output.append(tokText);
		charPosInLine += tokText.length();
	}
}
