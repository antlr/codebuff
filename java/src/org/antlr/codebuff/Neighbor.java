package org.antlr.codebuff;

import java.util.List;

public class Neighbor {
	public Corpus corpus;
	public final double distance;
	public final int corpusVectorIndex; // refers to both X (independent) and Y (dependent/predictor) variables

	public Neighbor(Corpus corpus, double distance, int corpusVectorIndex) {
		this.corpus = corpus;
		this.distance = distance;
		this.corpusVectorIndex = corpusVectorIndex;
	}

	public String toString(FeatureMetaData[] FEATURES, List<Integer> Y) {
		int[] X = corpus.X.get(corpusVectorIndex);
		InputDocument doc = corpus.documents.get(corpusVectorIndex);
		String features = CollectFeatures._toString(FEATURES, doc.parser.getVocabulary(), doc.parser.getRuleNames(), X);
		int line = CollectFeatures.getInfoLine(X);
		String lineText = doc.getLine(line);
		int col = X[CollectFeatures.INDEX_INFO_CHARPOS];
		// insert a dot right before char position
		lineText = lineText.substring(0,col) + '\u00B7' + lineText.substring(col,lineText.length());
		return String.format("%s (cat=%d,d=%1.3f): %s", features, Y.get(corpusVectorIndex), distance, lineText);
	}
}
