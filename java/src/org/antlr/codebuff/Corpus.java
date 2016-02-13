package org.antlr.codebuff;

import java.util.List;

public class Corpus {
	List<InputDocument> documents; // an entry for each X
	List<int[]> X;
	List<Integer> injectNewlines;
	List<Integer> injectWS;
	List<Integer> indent;
	List<Integer> levelsToCommonAncestor; // steps to common ancestor whose first token is alignment anchor

	public Corpus(List<InputDocument> documents,
				  List<int[]> X,
				  List<Integer> injectNewlines,
				  List<Integer> injectWS,
				  List<Integer> indent,
				  List<Integer> levelsToCommonAncestor)
	{
		this.documents = documents;
		this.X = X;
		this.injectNewlines = injectNewlines;
		this.injectWS = injectWS;
		this.indent = indent;
		this.levelsToCommonAncestor = levelsToCommonAncestor;
	}
}
