package org.antlr.codebuff;

import org.antlr.codebuff.misc.HashBag;
import org.antlr.v4.runtime.misc.Utils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestkNN {
	public static final int CAT0 = 0;	// predicted categories
	public static final int CAT1 = 1;
	public static final int CAT2 = 2;
	public static final int CAT3 = 3;

	public static final int T0 = 0;		// categorical values for features
	public static final int T1 = 1;
	public static final int T2 = 2;
	public static final int T3 = 3;
	public static final int T4 = 4;
	public static final int T5 = 5;
	public static final int T6 = 6;

	public static class CategoricalkNNClassifier extends kNNClassifier {
		public CategoricalkNNClassifier(List<int[]> X, List<Integer> Y, boolean[] categorical) {
			super(null, X, Y);
		}

		public double[] distance(int[] A, int[] B) {
			// compute the L1 (manhattan) distance of numeric and combined categorical
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
			return new double[] {((float)hamming)/num_categorical, 0.0};
		}

		public String toString(int[] features) {
			return String.format("%d %d", features[0], features[1]);
		}
	}

	static List<int[]> X = new ArrayList<>();
	static List<Integer> Y = new ArrayList<>();
	static boolean[] categorical = new boolean[] {true, true};

	static {
		X.add(new int[] {T0,T0});
		X.add(new int[] {T0,T1});
		X.add(new int[] {T0,T2});

		X.add(new int[] {T1,T0});
		X.add(new int[] {T1,T1});
		X.add(new int[] {T1,T2});

		X.add(new int[] {T2,T0});
		X.add(new int[] {T2,T1});
		X.add(new int[] {T2,T2});

		Y.add(CAT0);
		Y.add(CAT1);
		Y.add(CAT2);

		Y.add(CAT1);
		Y.add(CAT1);
		Y.add(CAT2);

		Y.add(CAT1);
		Y.add(CAT1);
		Y.add(CAT2);
	}

	@Test
	public void testDistances() {
		int k = 3;
		kNNClassifier c = new CategoricalkNNClassifier(X, Y, categorical);
		kNNClassifier.Neighbor[] distances = c.distances(new int[] {T0,T0});
		String expecting =
			"(cat=0,d=0.00), (cat=1,d=0.50), (cat=2,d=0.50), " +
			"(cat=1,d=0.50), (cat=1,d=1.00), (cat=2,d=1.00), " +
			"(cat=1,d=0.50), (cat=1,d=1.00), (cat=2,d=1.00)";
		String result = Utils.join(distances, ", ");
		assertEquals(expecting, result);

		distances = c.distances(new int[] {T0,T1});
		expecting =
			"(cat=0,d=0.50), (cat=1,d=0.00), (cat=2,d=0.50), " +
			"(cat=1,d=1.00), (cat=1,d=0.50), (cat=2,d=1.00), " +
			"(cat=1,d=1.00), (cat=1,d=0.50), (cat=2,d=1.00)";
		result = Utils.join(distances, ", ");
		assertEquals(expecting, result);

		distances = c.distances(new int[] {T1,T0});
		expecting =
			"(cat=0,d=0.50), (cat=1,d=1.00), (cat=2,d=1.00), " +
			"(cat=1,d=0.00), (cat=1,d=0.50), (cat=2,d=0.50), " +
			"(cat=1,d=0.50), (cat=1,d=1.00), (cat=2,d=1.00)";
		result = Utils.join(distances, ", ");
		assertEquals(expecting, result);

		distances = c.distances(new int[] {T1,T1});
		expecting =
			"(cat=0,d=1.00), (cat=1,d=0.50), (cat=2,d=1.00), " +
			"(cat=1,d=0.50), (cat=1,d=0.00), (cat=2,d=0.50), " +
			"(cat=1,d=1.00), (cat=1,d=0.50), (cat=2,d=1.00)";
		result = Utils.join(distances, ", ");
		assertEquals(expecting, result);

		distances = c.distances(new int[] {T1,T2});
		expecting =
			"(cat=0,d=1.00), (cat=1,d=1.00), (cat=2,d=0.50), " +
			"(cat=1,d=0.50), (cat=1,d=0.50), (cat=2,d=0.00), " +
			"(cat=1,d=1.00), (cat=1,d=1.00), (cat=2,d=0.50)";
		result = Utils.join(distances, ", ");
		assertEquals(expecting, result);
	}

	@Test
	public void testNeighborsT0T0() {
		kNNClassifier c = new CategoricalkNNClassifier(X, Y, categorical);
		int[] unknown = {T0, T0};
		kNNClassifier.Neighbor[] neighbors = c.kNN(X.size(), unknown);
		// sorted by distance
		String expecting =
			"(cat=0,d=0.00), (cat=1,d=0.50), (cat=2,d=0.50), " +
			"(cat=1,d=0.50), (cat=1,d=0.50), (cat=1,d=1.00), " +
			"(cat=2,d=1.00), (cat=1,d=1.00), (cat=2,d=1.00)";
		String result = Utils.join(neighbors, ", ");
		assertEquals(expecting, result);

		neighbors = c.kNN(1, unknown);
		// sorted by distance
		expecting =	"(cat=0,d=0.00)";
		result = Utils.join(neighbors, ", ");
		assertEquals(expecting, result);

		neighbors = c.kNN(3, unknown);
		// sorted by distance
		expecting =	"(cat=0,d=0.00), (cat=1,d=0.50), (cat=2,d=0.50)";
		result = Utils.join(neighbors, ", ");
		assertEquals(expecting, result);
	}

	@Test
	public void testVotesT0T0() {
		kNNClassifier c = new CategoricalkNNClassifier(X, Y, categorical);
		int[] unknown = {T0, T0};
		HashBag<Integer> votes = c.votes(X.size(), unknown);
		assertEquals("[1, 5, 3]", Arrays.toString(getCounts(votes))); // all categories are equally voted for given X and k=len(X)

		votes = c.votes(1, unknown);
		assertEquals("[1, 0, 0]", Arrays.toString(getCounts(votes)));

		votes = c.votes(2, unknown);
		assertEquals("[1, 1, 0]", Arrays.toString(getCounts(votes)));

		votes = c.votes(3, unknown);
		assertEquals("[1, 1, 1]", Arrays.toString(getCounts(votes)));

		votes = c.votes(4, unknown);
		assertEquals("[1, 2, 1]", Arrays.toString(getCounts(votes)));

		votes = c.votes(5, unknown);
		assertEquals("[1, 3, 1]", Arrays.toString(getCounts(votes)));
	}

	public int[] getCounts(HashBag<Integer> votes) {
		int[] votesA = new int[3];
		for (Integer I : votes.keySet()) {
			votesA[I] = votes.get(I);
		}
		return votesA;
	}

	@Test
	public void testClassificationT0T0() {
		kNNClassifier c = new CategoricalkNNClassifier(X, Y, categorical);
		int[] unknown = {T0, T0};
		int result = c.classify(X.size(), unknown);
		assertEquals(CAT1, result);

		result = c.classify(1, unknown);
		assertEquals(CAT0, result);

		result = c.classify(2, unknown);
		assertEquals(CAT0, result);

		result = c.classify(3, unknown);
		assertEquals(CAT0, result);

		result = c.classify(4, unknown);
		assertEquals(CAT1, result);
	}

	@Test
	public void testNeighborsT1T0() {
		kNNClassifier c = new CategoricalkNNClassifier(X, Y, categorical);
		int[] unknown = {T1, T0};
		kNNClassifier.Neighbor[] neighbors = c.kNN(X.size(), unknown);
		// sorted by distance
		String expecting =
			"(cat=1,d=0.00), (cat=0,d=0.50), (cat=1,d=0.50), " +
			"(cat=2,d=0.50), (cat=1,d=0.50), (cat=1,d=1.00), " +
			"(cat=2,d=1.00), (cat=1,d=1.00), (cat=2,d=1.00)";
		String result = Utils.join(neighbors, ", ");
		assertEquals(expecting, result);

		neighbors = c.kNN(1, unknown);
		// sorted by distance
		expecting =	"(cat=1,d=0.00)";
		result = Utils.join(neighbors, ", ");
		assertEquals(expecting, result);

		neighbors = c.kNN(3, unknown);
		// sorted by distance
		expecting =	"(cat=1,d=0.00), (cat=0,d=0.50), (cat=1,d=0.50)";
		result = Utils.join(neighbors, ", ");
		assertEquals(expecting, result);
	}


	@Test
	public void testVotesT1T0() {
		kNNClassifier c = new CategoricalkNNClassifier(X, Y, categorical);
		int[] unknown = {T1, T0};
		HashBag<Integer> votes = c.votes(X.size(), unknown);
		assertEquals("[1, 5, 3]", Arrays.toString(getCounts(votes))); // all categories are equally voted for given X and k=len(X)

		votes = c.votes(1, unknown);
		assertEquals("[0, 1, 0]", Arrays.toString(getCounts(votes)));

		votes = c.votes(2, unknown);
		assertEquals("[1, 1, 0]", Arrays.toString(getCounts(votes)));

		votes = c.votes(3, unknown);
		assertEquals("[1, 2, 0]", Arrays.toString(getCounts(votes)));

		votes = c.votes(4, unknown);
		assertEquals("[1, 2, 1]", Arrays.toString(getCounts(votes)));

		votes = c.votes(5, unknown);
		assertEquals("[1, 3, 1]", Arrays.toString(getCounts(votes)));
	}


	@Test
	public void testClassificationT1T0() {
		kNNClassifier c = new CategoricalkNNClassifier(X, Y, categorical);
		int[] unknown = {T1, T0};
		int result = c.classify(X.size(), unknown);
		assertEquals(CAT1, result);

		result = c.classify(1, unknown);
		assertEquals(CAT1, result);

		result = c.classify(2, unknown);
		assertEquals(CAT0, result);

		result = c.classify(3, unknown);
		assertEquals(CAT1, result);

		result = c.classify(4, unknown);
		assertEquals(CAT1, result);
	}

}