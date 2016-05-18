package org.antlr.codebuff.misc;

import java.util.List;

public class TrainingResult {
	public List<int[]> featureVectors;
	public List<Integer> injectWhitespace;
	public List<Integer> hpos;

	public TrainingResult(List<int[]> featureVectors,
	                      List<Integer> injectWhitespace,
	                      List<Integer> hpos)
	{
		this.featureVectors = featureVectors;
		this.hpos = hpos;
		this.injectWhitespace = injectWhitespace;
	}
}
