package org.antlr.codebuff.misc;

/** Track stats about a single parent:alt,child:alt list or split-list */
public class SiblingListStats {
	public final int numSamples, min, median, max;
	public final double variance;

	public SiblingListStats(int numSamples, int min, int median, double variance, int max) {
		this.numSamples = numSamples;
		this.max = max;
		this.median = median;
		this.min = min;
		this.variance = variance;
	}

	@Override
	public String toString() {
		return "("+numSamples+","+min+","+median+","+variance+","+max+")";
	}
}
