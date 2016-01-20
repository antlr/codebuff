package org.antlr.groom;

import java.util.List;

/** A k nearest neighbor classifier to decide on injecting whitespace before a token. */
public class InjectWSClassifier extends CodekNNClassifier {
	public InjectWSClassifier(List<int[]> X, List<Integer> Y, boolean[] categorical) {
		super(X, Y, categorical);
	}
}
