package org.antlr.codebuff.validation;

import org.antlr.v4.runtime.misc.MurmurHash;

import java.util.Arrays;


public class FeatureVectorAsObject {
	int[] features;

	public FeatureVectorAsObject(int[] features) {
		this.features = features;
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		for (int i = 0; i<features.length-3; i++) { // don't include INFO
			int feature = features[i];
			hash = MurmurHash.update(hash, feature);
		}
		return MurmurHash.finish(hash, features.length - 3);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if ( !(obj instanceof FeatureVectorAsObject) ) {
			return false;
		}
		FeatureVectorAsObject other = (FeatureVectorAsObject) obj;
		for (int i = 0; i<features.length-3; i++) { // don't include INFO
			if ( features[i]!=other.features[i] ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return Arrays.toString(features);
	}
}
