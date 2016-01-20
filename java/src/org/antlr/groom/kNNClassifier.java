package org.antlr.groom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A kNN (k-Nearest Neighbor) classifier */
public abstract class kNNClassifier {
	protected List<int[]> X;
	protected List<Integer> Y;
	protected boolean[] categorical;
	public final int numCategories;

	public static class Neighbor {
		public final int category;
		public final double distance;
		public final int corpusVectorIndex;

		public Neighbor(int category, double distance, int corpusVectorIndex) {
			this.category = category;
			this.distance = distance;
			this.corpusVectorIndex = corpusVectorIndex;
		}

		@Override
		public String toString() {
//			return String.format("(@%d,cat=%d,d=%1.2f)", corpusVectorIndex, category, distance);
			return String.format("(cat=%d,d=%1.2f)", category, distance);
		}
	}

	public kNNClassifier(List<int[]> X, List<Integer> Y, boolean[] categorical) {
		this.X = X;
		this.Y = Y;
		this.categorical = categorical;
		numCategories = max(Y) + 1;
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
		for (int i=0; i<numCategories; i++) {
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
		int[] votes = new int[numCategories];
		List<Integer>[] charPos = new List[numCategories];
		List<Integer>[] widths = new List[numCategories];
		List<Integer>[] sum = new List[numCategories];
		for (int i=0; i<numCategories; i++) {
			charPos[i] = new ArrayList<>();
			widths[i] = new ArrayList<>();
			sum[i] = new ArrayList<>();
		}
		for (int i=0; i<k; i++) {
			if ( kNN[i].category<0 ) continue;
			votes[kNN[i].category]++;
			int[] features = X.get(kNN[i].corpusVectorIndex);
			charPos[kNN[i].category].add(features[CollectFeatures.INDEX_PREV_END_COLUMN]);
			widths[kNN[i].category].add(features[CollectFeatures.INDEX_ANCESTOR_WIDTH]);
			sum[kNN[i].category].add(features[CollectFeatures.INDEX_PREV_END_COLUMN]+
									 features[CollectFeatures.INDEX_ANCESTOR_WIDTH]);
		}
//		System.out.println(toString(unknown)+"->"+Arrays.toString(kNN)+"->"+Arrays.toString(votes));
//		System.out.println(Arrays.toString(charPos));
//		System.out.println(Arrays.toString(widths));
//		System.out.println(Arrays.toString(sum));
		return votes;
	}

	public Neighbor[] kNN(int k, int[] unknown) {
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
			distances[i] = new Neighbor(Y.get(i), distance(x, unknown), i);
		}
		return distances;
	}

	public abstract double distance(int[] A, int[] B);

	public abstract String toString(int[] features);
}
