package org.antlr.groom;

import org.antlr.v4.runtime.misc.Pair;

import java.util.Arrays;
import java.util.List;

/** A kNN (k-Nearest Neighbor) classifier */
public class Classifier {
	protected List<int[]> X;
	protected List<Integer> Y;
	protected boolean[] categorical;
	public final int maxCategoryValue;

	public Classifier(List<int[]> X, List<Integer> Y, boolean[] categorical) {
		this.X = X;
		this.Y = Y;
		this.categorical = categorical;
		maxCategoryValue = max(Y);
	}

	public int max(List<Integer> Y) {
		int max = 0;
		for (int y : Y) max = Math.max(max, y);
		return max;
	}

	public int count(int[] a) {
		int max = 0;
		for (int x : a) max = Math.max(max, x);
		return max;
	}

	/** Walk all training samples and compute distance(). Return indexes of k
	 *  smallest distance values.
	 */
	public int classify(int k, int[] unknown) {
		Pair<Integer, Double>[] kNN = kNN(k, unknown);
		// each neighbor gets a vote
		int[] votes = new int[maxCategoryValue];
		for (int i=0; i<k; i++) {
			votes[kNN[i].a]++;
		}
		int max = 0;
		int cat = 0;
		for (int i=0; i<maxCategoryValue; i++) {
			if ( votes[i]>max ) {
				max = votes[i];
				cat = i;
			}
		}
		return cat;
	}

	public Pair<Integer, Double>[] kNN(int k, int[] unknown) {
		int n = unknown.length;
		Pair<Integer, Double>[] distances = new Pair[n];
		for (int i=0; i<n; i++) {
			int[] x = X.get(i);
			distances[i] = new Pair<>(Y.get(i), distance(x, unknown));
		}
		Arrays.sort(distances,
					(Pair<Integer, Double> o1, Pair<Integer, Double> o2) -> Double.compare(o1.b,o2.b));
		return Arrays.copyOfRange(distances, n-4, n);
	}

	public double distance(int[] A, int[] B) {
		double d = 0.0;
		int hamming = 0; // count how many mismatched categories there are
		for (int i=0; i<A.length; i++) {
			if ( categorical[i] ) {
				if ( A[i] != B[i] ) {
					hamming++;
				}
			}
			else {
				d += Math.abs(A[i]-B[i]);
			}
		}
		// assume numeric data has been normalized so we don't overwhelm hamming distance
		return d + hamming;
	}
}
