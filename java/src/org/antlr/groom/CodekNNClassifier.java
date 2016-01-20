package org.antlr.groom;

import org.antlr.v4.runtime.Vocabulary;

import java.util.List;

public abstract class CodekNNClassifier extends kNNClassifier {
	public CodekNNClassifier(List<int[]> X, List<Integer> Y, boolean[] categorical) {
		super(X, Y, categorical);
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
