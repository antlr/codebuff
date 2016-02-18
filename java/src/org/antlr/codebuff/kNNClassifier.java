package org.antlr.codebuff;

import org.antlr.codebuff.misc.HashBag;
import org.antlr.v4.runtime.misc.Utils;

import java.util.Arrays;
import java.util.List;

/** A kNN (k-Nearest Neighbor) classifier */
public abstract class kNNClassifier {
	protected List<InputDocument> documents;
	protected List<int[]> X;
	protected List<Integer> Y;
	public boolean dumpVotes = false;

	public class Neighbor {
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
			int[] X = kNNClassifier.this.X.get(corpusVectorIndex);
			InputDocument doc = documents.get(corpusVectorIndex);
			String features = CollectFeatures._toString(doc.parser.getVocabulary(), doc.parser.getRuleNames(), X);
			int line = CollectFeatures.getInfoLine(X);
			return String.format("%s (cat=%d,d=%1.3f): %s", features, category, distance, doc.getLine(line));
		}
	}

	public kNNClassifier(List<InputDocument> documents, List<int[]> X, List<Integer> Y) {
		this.documents = documents;
		this.X = X;
		this.Y = Y;
	}

	public int classify(int k, int[] unknown) {
		return classify(k, unknown, 1.0);
	}

	/**
	 * Walk all training samples and compute distance(). Return indexes of k
	 * smallest distance values.  Categories can be any negative or positive
	 * integer (and 0).
	 */
	public int classify(int k, int[] unknown, double distanceThreshold) {
		HashBag<Integer> votes = votes(k, unknown, distanceThreshold);
		int max = Integer.MIN_VALUE;
		int catWithMostVotes = 0;
		for (Integer category : votes.keySet()) {
			if ( votes.get(category)>max ) {
				max = votes.get(category);
				catWithMostVotes = category;
			}
		}

		return catWithMostVotes;
	}

	public HashBag<Integer> votes(int k, int[] unknown) {
		return votes(k, unknown, 1.0);
	}

	public HashBag<Integer> votes(int k, int[] unknown, double distanceThreshold) {
		Neighbor[] kNN = kNN(k, unknown);
		HashBag<Integer> votes = new HashBag<>();
		for (int i = 0; i<k && i<kNN.length; i++) {
			// Don't count any votes for training samples too distant.
			if ( kNN[i].distance>distanceThreshold ) {
				break;
			}
			votes.add(kNN[i].category);
		}
		if ( dumpVotes ) {
			System.out.print(CollectFeatures.featureNameHeader());
			InputDocument firstDoc = documents.get(kNN[0].corpusVectorIndex); // pick any neighbor to get parser
			System.out.println(CollectFeatures._toString(firstDoc.parser.getVocabulary(), firstDoc.parser.getRuleNames(), unknown)+"->"+votes);
			kNN = Arrays.copyOfRange(kNN, 0, 25);
			System.out.println(Utils.join(kNN, "\n"));
		}
		return votes;
	}

	public Neighbor[] kNN(int k, int[] unknown) {
		Neighbor[] distances = distances(unknown);
		Arrays.sort(distances,
		            (Neighbor o1, Neighbor o2) -> Double.compare(o1.distance, o2.distance));
		return Arrays.copyOfRange(distances, 0, k);
	}

	public Neighbor[] distances(int[] unknown) {
		int n = X.size(); // num training samples
		Neighbor[] distances = new Neighbor[n];
		for (int i = 0; i<n; i++) {
			int[] x = X.get(i);
			distances[i] = new Neighbor(Y.get(i), distance(x, unknown), i);
		}
		return distances;
	}

	public abstract double distance(int[] A, int[] B);
}
