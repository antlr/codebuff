package org.antlr.codebuff.misc;

import java.util.Arrays;
import java.util.List;

public class ClassifierResultCacheKey {
	int[] features;
	List<Integer> Y;

	public ClassifierResultCacheKey(int[] features, List<Integer> y) {
		this.features = features;
		Y = y;
	}

	@Override
	public int hashCode() {
		int h = 0;
		for (int feature : features) {
			h += feature << 7;
		}
		return h + System.identityHashCode(Y)<<7;
	}

	@Override
	public boolean equals(Object obj) {
		if ( !(obj instanceof ClassifierResultCacheKey) ) return false;
		ClassifierResultCacheKey other = (ClassifierResultCacheKey) obj;
		return Arrays.equals(features, other.features) &&
			Y == other.Y;
	}

	@Override
	public String toString() {
		return Arrays.toString(features)+"/"+System.identityHashCode(Y);
	}
}
