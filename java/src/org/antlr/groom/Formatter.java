package org.antlr.groom;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Formatter extends JavaBaseListener {
	public static final int k = 201; // corpus has 41,973 records. that's about sqrt(41973).
	protected StringBuilder output = new StringBuilder();
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected kNNClassifier newlineClassifier;
	protected kNNClassifier wsClassifier;

	protected int line = 1;
	protected int charPosInLine = 0;

	public Formatter(Corpus corpus, CommonTokenStream tokens) {
		this.tokens = tokens;
		Tool.wipeLineAndPositionInfo(tokens);
		newlineClassifier = new InjectNewlinesClassifier(corpus.X,
														 corpus.injectNewlines,
														 CollectFeatures.CATEGORICAL);
		wsClassifier = new InjectWSClassifier(corpus.X,
											  corpus.injectWS,
											  CollectFeatures.CATEGORICAL);
	}

	public String getOutput() {
		return output.toString();
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		Token curToken = node.getSymbol();
		if ( curToken.getType()==Token.EOF ) return;

		int i = curToken.getTokenIndex();
		String tokText = tokens.get(i).getText();
		if ( i<2 ) {
			output.append(tokText);
			return; // we need 2 previous tokens and current token
		}
		int[] features = CollectFeatures.getNodeFeatures(tokens, node);
		// must set "prev end column" value as token stream doesn't have it;
		// we're tracking it as we emit tokens
		features[CollectFeatures.INDEX_PREV_END_COLUMN] = charPosInLine;

		int injectNewline = newlineClassifier.classify(k, features);
		if ( injectNewline==1 ) {
			output.append("\n");
			line++;
			charPosInLine = 0;
		}
		else {
			// inject whitespace instead of \n?
			int ws = wsClassifier.classify(k, features); // the class is the number of WS chars
			for (int sp=1; sp<=ws; sp++) output.append(" ");
		}
		output.append(tokText);
		charPosInLine += tokText.length();
	}

}
