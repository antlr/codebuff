package org.antlr.groom;

import org.antlr.v4.runtime.Vocabulary;

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

 There were k (29) exact context matches, but 62% of the	time a new
 line was used. It was this case	that I looked at for column or with
 information as the distinguishing characteristic, but it didn't seem
 to help.
 */

/** A k nearest neighbor classifier to decide on injecting newlines */
public class InjectNewlinesClassifier extends kNNClassifier {
	public InjectNewlinesClassifier(List<int[]> X, List<Integer> Y, boolean[] categorical) {
		super(X, Y, categorical);
	}

	/** Get P(inject-newline) from votes based solely on context information. */
	public double distance(int[] A, int[] B) {
		return L0_Distance(A, B);
	}

	public double L0_Distance(int[] A, int[] B) {
		int hamming = 0; // count how many mismatched categories there are; L0 distance I think
		int num_categorical = 0;
		for (int i=0; i<A.length; i++) {
			if ( categorical[i] ) {
				num_categorical++;
				if ( A[i] != B[i] ) {
					hamming++;
				}
			}
		}
		return ((float)hamming)/num_categorical;
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
