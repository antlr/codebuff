package org.antlr.codebuff;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Formatter {

	protected StringBuilder output = new StringBuilder();
	protected InputDocument doc;
	protected ParserRuleContext root;
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected List<CommonToken> originalTokens; // copy of tokens with line/col info

	protected Map<Token, TerminalNode> tokenToNodeMap = null;

	protected CodekNNClassifier newlineClassifier;
	protected CodekNNClassifier wsClassifier;
	protected CodekNNClassifier indentClassifier;
	protected CodekNNClassifier alignClassifier;
	protected int k;

	protected int line = 1;
	protected int charPosInLine = 0;
	protected int currentIndent = 0;

	protected int tabSize;

	protected int misclassified = 0;

	public Formatter(Corpus corpus, InputDocument doc, int tabSize) {
		this.doc = doc;
		this.root = doc.tree;
		this.tokens = doc.tokens;
		this.originalTokens = Tool.copy(tokens);
		Tool.wipeLineAndPositionInfo(tokens);
		newlineClassifier = new CodekNNClassifier(corpus, corpus.injectNewlines);
		wsClassifier = new CodekNNClassifier(corpus, corpus.injectWS);
		indentClassifier = new CodekNNClassifier(corpus, corpus.indent);
//		indentClassifier.dumpVotes = true;
		alignClassifier = new CodekNNClassifier(corpus, corpus.levelsToCommonAncestor);
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

		int injectNewline = newlineClassifier.classify(k, features, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
		int indent = indentClassifier.classify(k, features, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);

		// compare prediction of newline against original, alert about any diffs
		CommonToken prevToken = originalTokens.get(curToken.getTokenIndex()-1);
		CommonToken originalCurToken = originalTokens.get(curToken.getTokenIndex());

		if ( prevToken.getType()==JavaLexer.WS ) {
			int actual = Tool.count(prevToken.getText(), '\n');
			if ( injectNewline!=actual ) {
				misclassified++;
				doc.misclassifiedNewLineCount++;
				System.out.println();
				System.out.printf("### line %d: predicted %d actual %d:\n",
				                  originalCurToken.getLine(), injectNewline, actual);
				Tool.printOriginalFilePiece(doc, originalCurToken);
				newlineClassifier.dumpVotes = true;
				newlineClassifier.classify(k, features, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
				newlineClassifier.dumpVotes = false;
			}
		}

		if ( injectNewline>0 ) {
			output.append(Tool.newlines(injectNewline));
			line++;
			int levelsToCommonAncestor = alignClassifier.classify(k, features, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD);
			if ( levelsToCommonAncestor>0 ) {
				List<? extends Tree> ancestors = Trees.getAncestors(tokenToNodeMap.get(curToken));
				Collections.reverse(ancestors);
				ParserRuleContext commonAncestor =
					(ParserRuleContext)ancestors.get(levelsToCommonAncestor);
				charPosInLine = commonAncestor.getStart().getCharPositionInLine();
				output.append(Tool.spaces(charPosInLine));
			}
			else {
				currentIndent += indent;
				if ( currentIndent<0 ) currentIndent = 0; // don't allow bad indents to accumulate
				charPosInLine = currentIndent;
				output.append(Tool.spaces(currentIndent));
			}
//			else { //if ( indent>0 ) {
//				output.append(Tool.spaces(currentIndent));
//				output.append(Tool.spaces(indent));
//				currentIndent += indent;
//			}
		}
		else {
			// inject whitespace instead of \n?
			int ws = wsClassifier.classify(k, features, CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD); // the class is the number of WS chars
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
