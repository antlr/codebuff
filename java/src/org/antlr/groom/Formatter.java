package org.antlr.groom;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class Formatter extends JavaBaseListener {
	public static final int k = 29;
	protected StringBuilder output = new StringBuilder();
	protected CommonTokenStream tokens; // track stream so we can examine previous tokens
	protected kNNClassifier classifier;

	protected int line = 1;
	protected int charPosInLine = 0;

	public static class MykNNClassifier extends kNNClassifier {
		public MykNNClassifier(List<int[]> X, List<Integer> Y, boolean[] categorical) {
			super(X, Y, categorical);
		}

		public double distance(int[] A, int[] B) {
			// compute the L1 (manhattan) distance of numeric and combined categorical
			double d = 0.0;
			int hamming = 0; // count how many mismatched categories there are; L0 distance I think
			int num_categorical = 0;
			for (int i=0; i<A.length; i++) {
				if ( categorical[i] ) {
					num_categorical++;
					if ( A[i] != B[i] ) {
						hamming++;
					}
				}
				else {
					int delta = Math.abs(A[i]-B[i]);
					d += delta/120.0; // normalize 0-1.0 for a large column value as 1.0.
				}
			}
			// assume numeric data has been normalized so we don't overwhelm hamming distance
			return d + ((float)hamming)/num_categorical;
	//		return ((float)hamming)/num_categorical;
		}

		public String toString(int[] features) {
			Vocabulary v = JavaParser.VOCABULARY;
			return String.format(
				"%s %s %d %s, %s %d %s",
				v.getDisplayName(features[0]),
				v.getDisplayName(features[1]), features[2],
				v.getDisplayName(features[3]), JavaParser.ruleNames[features[4]], features[5],
				v.getDisplayName(features[6])
			                    );
		}
	}

	public Formatter(Corpus corpus, CommonTokenStream tokens) {
		this.tokens = tokens;
		Tool.wipeLineAndPositionInfo(tokens);
		classifier = new MykNNClassifier(corpus.X, corpus.Y, CollectFeatures.CATEGORICAL);
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
		// must set "prev end column" value as token stream doesn't have it; we're tracking it as we emit tokens
		features[CollectFeatures.INDEX_PREV_END_COLUMN] = charPosInLine;

		int injectNewline = classifier.classify(k, features);
		if ( injectNewline==1 ) {
			output.append("\n");
			line++;
			charPosInLine = 0;
		}
		output.append(tokText);
		charPosInLine += tokText.length();
	}

}
