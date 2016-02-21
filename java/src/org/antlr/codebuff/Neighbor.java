package org.antlr.codebuff;

public class Neighbor {
	private org.antlr.codebuff.kNNClassifier kNNClassifier;
	public final int category;
	public final double distance;
	public final int corpusVectorIndex;

	public Neighbor(kNNClassifier kNNClassifier, int category, double distance, int corpusVectorIndex) {
		this.kNNClassifier = kNNClassifier;
		this.category = category;
		this.distance = distance;
		this.corpusVectorIndex = corpusVectorIndex;
	}

	@Override
	public String toString() {
		int[] X = kNNClassifier.corpus.X.get(corpusVectorIndex);
		InputDocument doc = kNNClassifier.corpus.documents.get(corpusVectorIndex);
		String features = CollectFeatures._toString(doc.parser.getVocabulary(), doc.parser.getRuleNames(), X);
		int line = CollectFeatures.getInfoLine(X);
		return String.format("%s (cat=%d,d=%1.3f): %s", features, category, distance, doc.getLine(line));
	}
}
