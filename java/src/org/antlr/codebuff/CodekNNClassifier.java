package org.antlr.codebuff;

import org.antlr.groom.JavaParser;
import org.antlr.v4.runtime.Vocabulary;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	/** 4 for current token, 2 for both adjacent, and 1 for distant tokens. 1 for earliest ancestor mismatch */
	public static final int MAX_L0_DISTANCE_COUNT = 10;

	public CodekNNClassifier(List<int[]> X, List<Integer> Y, boolean[] categorical) {
		super(X, Y, categorical);
	}

//	public int classify(int k, int[] unknown, double distanceThreshold) {
//		Map<Integer, Double> votes = __votes(k, unknown, distanceThreshold);
//		double max = Double.MIN_VALUE;
//		int catWithMostVotes = 0;
//		for (Integer category : votes.keySet()) {
//			if ( votes.get(category) > max ) {
//				max = votes.get(category);
//				catWithMostVotes = category;
//			}
//		}
//
//		return catWithMostVotes;
//	}
//
//	public Map<Integer, Double> __votes(int k, int[] unknown, double distanceThreshold) {
//		Neighbor[] kNN = kNN(k, unknown);
////		HashBag<Integer> votes = new HashBag<>();
//
//		Map<Integer, Double> votes = new HashMap<>();
//		for (int i=0; i<k && i<kNN.length; i++) {
//			// Don't count any votes for training samples too distant.
//			if ( kNN[i].distance > distanceThreshold ) break;
//			// now give more weight to closer matches. Distance is P(match)
//			// so 1-P(match) gives boost to P(match)=1.0 and kills P(match)=0.0 cases
//			double d = kNN[i].distance;
//			int cat = kNN[i].category;
//			Double existing = votes.get(cat);
//			if ( existing==null ) {
//				votes.put(cat, 1.0);
//			}
//			else {
//				votes.put(cat, existing+(1-d));
//			}
//		}
//		if ( dumpVotes ) {
//			System.out.println(toString(unknown)+"->"+Arrays.toString(kNN)+"->"+votes);
//		}
//
//		return votes;
//	}

	/** Compute distance as a probability of match, based
	 *  solely on context information.
	 *
	 *  Ratio of num differences / num total context positions.
	 */
	public double distance(int[] A, int[] B) {
//		return ((float)Tool.L0_Distance(categorical, A, B))/num_categorical;
		float d = (float) Tool.weightedL0_Distance(categorical, A, B);
		return d / MAX_L0_DISTANCE_COUNT;
	}

	public String toString(int[] features) {
		return _toString(features);
	}

	public static String _toString(int[] features) {
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
