package org.antlr.codebuff;

import java.util.List;

/*
 It would appear the context information provides the strongest evidence for
 injecting a new line or not.  The column number and even the width of the next
 statement or expression does not help and in fact probably confuses the
 issue. Looking at the histograms of both column and earliest ancestor
 with, I find that they overlap almost always. The sum of the two
 elements also overlapped heavily. It's possible that using the two
 values as coordinates would yield separability but it didn't look
 like it from a scan of the data.

 Using just context information provides amazingly polarized
 decisions. All but a few were 100% for or against. The closest
 decision was 11 against and 18 for injecting a newline at context
 where 'return'his current token:

     ')' '{' 23 'return', statement 49 Identifier

 There were k (29) exact context matches, but 62% of the time a new
 line was used. It was this case	that I looked at for column or with
 information as the distinguishing characteristic, but it didn't seem
 to help.  I bumped that to k=201 for all ST4 source (like 41000 records)
 and I get 60 against, 141 for a newline (70%).

 We can try more context but weight the significance of misses close to
 current token more heavily than tokens farther away.
 */

public class CodekNNClassifier extends kNNClassifier {

	public CodekNNClassifier(List<int[]> X, List<Integer> Y) {
		super(X, Y);
	}

	/** Compute distance as a probability of match, based
	 *  solely on context information.
	 *
	 *  Ratio of num differences / num total context positions.
	 */
	public double distance(int[] A, int[] B) {
//		return ((float)Tool.L0_Distance(categorical, A, B))/num_categorical;
		float d = (float) Tool.weightedL0_Distance(CollectFeatures.FEATURES, A, B);
		return d / CollectFeatures.MAX_L0_DISTANCE_COUNT;
	}

	public String toString(int[] features) {
		return CollectFeatures._toString(features);
	}

}
