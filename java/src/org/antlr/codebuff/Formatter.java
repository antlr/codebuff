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

	protected CodekNNClassifier classifier;
	protected CodekNNClassifier newlineClassifier;
//	protected CodekNNClassifier wsClassifier;
//	protected CodekNNClassifier indentClassifier;
//	protected CodekNNClassifier alignClassifier;
	protected int k;

	protected int line = 1;
	protected int charPosInLine = 0;
	protected int currentIndent = 0;

	protected int tabSize;

	protected boolean debug_NL = true;
	protected int misclassified_NL = 0;

	public Formatter(Corpus corpus, InputDocument doc, int tabSize) {
		this.corpus = corpus;
		this.doc = doc;
		this.root = doc.tree;
		this.tokens = doc.tokens;
		this.originalTokens = Tool.copy(tokens);
		Tool.wipeLineAndPositionInfo(tokens);
		newlineClassifier = new CodekNNClassifier(corpus); // keep separate so we can dump votes for this only
		classifier = new CodekNNClassifier(corpus);
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

		int[] categories = classifier.classify(k, features, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		int injectNewline = categories[Corpus.INDEX_FEATURE_NEWLINES];
		int indent = categories[Corpus.INDEX_FEATURE_INDENT];
		int ws = categories[Corpus.INDEX_FEATURE_WS];
		int alignWithPrevious = categories[Corpus.INDEX_FEATURE_ALIGN_WITH_PREVIOUS];

		// compare prediction of newline against original, alert about any diffs
		CommonToken prevToken = originalTokens.get(curToken.getTokenIndex()-1);
		CommonToken originalCurToken = originalTokens.get(curToken.getTokenIndex());


		if ( debug_NL && prevToken.getType()==JavaLexer.WS ) {
			int actual = Tool.count(prevToken.getText(), '\n');
			if ( injectNewline!=actual ) {
				misclassified_NL++;
				doc.misclassifiedNewLineCount++;
				if (doc.dumpVotes) {
					System.out.println();
					System.out.printf("### line %d: predicted %d actual %d:\n",
						originalCurToken.getLine(), injectNewline, actual);
					Tool.printOriginalFilePiece(doc, originalCurToken);
					newlineClassifier.dumpVotes = true;
					newlineClassifier.classify(k, features, corpus.injectNewlines, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
					newlineClassifier.dumpVotes = false;
				}
			}
		}

		if ( injectNewline>0 ) {
			output.append(Tool.newlines(injectNewline));
			line++;
			if ( alignWithPrevious>0 ) {
				TerminalNode node = tokenToNodeMap.get(tokens.get(i));
				ParserRuleContext parent = (ParserRuleContext)node.getParent();
				ParserRuleContext earliestAncestor = CollectFeatures.earliestAncestorStartingAtToken(parent, curToken);
				ParserRuleContext commonAncestor = earliestAncestor.getParent();
				List<ParserRuleContext> siblings = commonAncestor.getRuleContexts(earliestAncestor.getClass());
				int myIndex = siblings.indexOf(earliestAncestor);
				int prevIndex = myIndex - 1;
				if ( prevIndex>=0 ) {
					ParserRuleContext prevSibling = siblings.get(prevIndex);
					Token prevSiblingStartToken = prevSibling.getStart();
					charPosInLine = prevSiblingStartToken.getCharPositionInLine();
					output.append(Tool.spaces(charPosInLine));
				}
				else {
					// TODO: what to do when we are first in list or only sibling? fail over to indent?
				}
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
