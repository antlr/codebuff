package org.antlr.codebuff.kdtree;

import org.antlr.codebuff.FeatureMetaData;
import org.antlr.codebuff.Tool;

public class HammingDistance implements Distance {
	protected final FeatureMetaData[] FEATURES;
	protected final int maxDistanceCount;

	public HammingDistance(FeatureMetaData[] FEATURES, int maxDistanceCount) {
		this.FEATURES = FEATURES;
		this.maxDistanceCount = maxDistanceCount;
	}

	@Override
	public double distance(Exemplar from, Exemplar to) {
		return distance(from.features, to.features);
	}

	@Override
	public double distance2(Exemplar from, Exemplar to) {
		return Math.pow(distance(from.features, to.features), 2);
	}

	public double distance(int[] A, int[] B) {
		double d = Tool.weightedL0_Distance(FEATURES, A, B);
		return d/maxDistanceCount;
	}
}
