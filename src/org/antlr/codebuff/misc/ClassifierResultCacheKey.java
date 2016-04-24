package org.antlr.codebuff.misc;

import org.antlr.v4.runtime.misc.MurmurHash;

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
		int hash = MurmurHash.initialize();
		for (int feature : features) {
			hash = MurmurHash.update(hash, feature);
		}
		hash = MurmurHash.update(hash, System.identityHashCode(Y));
		return MurmurHash.finish(hash, features.length + 1);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if ( !(obj instanceof ClassifierResultCacheKey) ) {
			return false;
		}
		ClassifierResultCacheKey other = (ClassifierResultCacheKey) obj;
		return Arrays.equals(features, other.features) &&
			Y == other.Y;
	}

	@Override
	public String toString() {
		return Arrays.toString(features)+"/"+System.identityHashCode(Y);
	}
}
