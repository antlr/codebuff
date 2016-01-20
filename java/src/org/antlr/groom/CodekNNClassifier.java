package org.antlr.groom;

import org.antlr.v4.runtime.Vocabulary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CodekNNClassifier extends kNNClassifier {
	int num_categorical = 0;

	public CodekNNClassifier(List<int[]> X, List<Integer> Y, boolean[] categorical) {
		super(X, Y, categorical);
		int[] x = X.get(0);
		for (int i=0; i<x.length; i++) {
			if ( categorical[i] ) {
				num_categorical++;
			}
		}
	}

	/** Get probability from votes based solely on context information.
	 *  Ratio of num differences / num total context positions.
	 */
	public double distance(int[] A, int[] B) {
		return ((float)Tool.L0_Distance(categorical, A, B))/num_categorical;
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
