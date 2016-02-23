package org.antlr.codebuff;

public class Neighbor {
	public Corpus corpus;
	public final double distance;
	public final int corpusVectorIndex; // refers to both X (independent) and Y (dependent/predictor) variables

	public Neighbor(Corpus corpus, double distance, int corpusVectorIndex) {
		this.corpus = corpus;
		this.distance = distance;
		this.corpusVectorIndex = corpusVectorIndex;
	}

	@Override
	public String toString() {
		int[] X = corpus.X.get(corpusVectorIndex);
		InputDocument doc = corpus.documents.get(corpusVectorIndex);
		String features = CollectFeatures._toString(doc.parser.getVocabulary(), doc.parser.getRuleNames(), X);
		int line = CollectFeatures.getInfoLine(X);
		return String.format("%s (cat=%d,d=%1.3f): %s", features, -1, distance, doc.getLine(line));
	}
}
