package org.antlr.groom;

import java.util.List;

public class Corpus {
	List<int[]> X;
	List<Integer> injectNewlines;
	List<Integer> injectWS;

	public Corpus(List<int[]> X, List<Integer> injectNewlines, List<Integer> injectWS) {
		this.X = X;
		this.injectNewlines = injectNewlines;
		this.injectWS = injectWS;
	}
}
