package org.antlr.groom;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Formatter extends JavaBaseListener {
	public static final double MAX_CONTEXT_DIFF_THRESHOLD = 0.4; // anything more than 40% different is probably too far

	protected StringBuilder output = new StringBuilder();
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected kNNClassifier newlineClassifier;
	protected kNNClassifier wsClassifier;
	protected int k;

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
		k = (int)Math.sqrt(corpus.X.size());
	}

	public String getOutput() {
		return output.toString();
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		Token curToken = node.getSymbol();
		if ( curToken.getType()==Token.EOF ) return;

		int i = curToken.getTokenIndex();
		tokens.seek(i); // see so that LT(1) is tokens.get(i);
		if ( tokens.LT(-2)==null ) { // do we have 2 previous tokens?
			output.append(curToken.getText());
			return;
		}

		String tokText = curToken.getText();

		int[] features = CollectFeatures.getNodeFeatures(tokens, node);
		// must set "prev end column" value as token stream doesn't have it;
		// we're tracking it as we emit tokens
		features[CollectFeatures.INDEX_PREV_END_COLUMN] = charPosInLine;

		int injectNewline = newlineClassifier.classify(k, features, MAX_CONTEXT_DIFF_THRESHOLD);
		if ( injectNewline==1 ) {
			output.append("\n");
			line++;
			charPosInLine = 0;
		}
		else {
			// inject whitespace instead of \n?
			int ws = wsClassifier.classify(k, features, MAX_CONTEXT_DIFF_THRESHOLD); // the class is the number of WS chars
			for (int sp=1; sp<=ws; sp++) output.append(" ");
		}
		output.append(tokText);
		charPosInLine += tokText.length();
	}

}
