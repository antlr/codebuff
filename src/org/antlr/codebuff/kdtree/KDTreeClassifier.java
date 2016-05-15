package org.antlr.codebuff.kdtree;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.FeatureMetaData;

public class KDTreeClassifier {
	protected final Corpus corpus;
	protected final FeatureMetaData[] FEATURES;
	protected final int maxDistanceCount;

	public KDTreeClassifier(Corpus corpus, FeatureMetaData[] FEATURES) {
		this.corpus = corpus;
		this.FEATURES = FEATURES;
		int n = 0;
		for (FeatureMetaData FEATURE : FEATURES) {
			n += FEATURE.mismatchCost;
		}
		maxDistanceCount = n;
	}
}
