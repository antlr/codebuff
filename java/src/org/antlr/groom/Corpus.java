package org.antlr.groom;

import java.util.List;

public class Corpus {
	List<int[]> X;
	List<Integer> injectNewlines;
	List<Integer> injectWS;
	List<Integer> indent;
	List<Integer> levelsToCommonAncestor; // steps to common ancestor whose first token is alignment anchor

	public Corpus(List<int[]> X,
				  List<Integer> injectNewlines,
				  List<Integer> injectWS,
				  List<Integer> indent,
				  List<Integer> levelsToCommonAncestor)
	{
		this.X = X;
		this.injectNewlines = injectNewlines;
		this.injectWS = injectWS;
		this.indent = indent;
		this.levelsToCommonAncestor = levelsToCommonAncestor;
	}
}
