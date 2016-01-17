package org.antlr.groom;

import java.util.Arrays;
import java.util.List;

/** A kNN (k-Nearest Neighbor) classifier */
public class kNNClassifier {
	protected List<int[]> X;
	protected List<Integer> Y;
	protected boolean[] categorical;
	public final int maxCategoryValue;

	public static class Neighbor {
		public final int category;
		public final double distance;

		public Neighbor(int category, double distance) {
			this.category = category;
			this.distance = distance;
		}

		@Override
		public String toString() {
			return String.format("(cat=%d, d=%1.2f)", category, distance);
		}
	}

	public kNNClassifier(List<int[]> X, List<Integer> Y, boolean[] categorical) {
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
		int[] votes = votes(k, unknown);
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

	public int[] votes(int k, int[] unknown) {
		Neighbor[] kNN = kNN(k, unknown);
		// each neighbor gets a vote
		int[] votes = new int[maxCategoryValue+1];
		for (int i=0; i<k; i++) {
			votes[kNN[i].category]++;
		}
		return votes;
	}

	public Neighbor[] kNN(int k, int[] unknown) {
		int n = X.size(); // num training samples
		Neighbor[] distances = distances(k, unknown);
		Arrays.sort(distances,
					(Neighbor o1, Neighbor o2) -> Double.compare(o1.distance,o2.distance));
		return Arrays.copyOfRange(distances, 0, k);
	}

	public Neighbor[] distances(int k, int[] unknown) {
		int n = X.size(); // num training samples
		Neighbor[] distances = new Neighbor[n];
		for (int i=0; i<n; i++) {
			int[] x = X.get(i);
			distances[i] = new Neighbor(Y.get(i), distance(x, unknown));
		}
		return distances;
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
