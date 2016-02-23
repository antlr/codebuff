package org.antlr.codebuff;

import org.antlr.codebuff.misc.HashBag;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A kNN (k-Nearest Neighbor) classifier */
public abstract class kNNClassifier {
	protected Corpus corpus;
	public boolean dumpVotes = false;

	public kNNClassifier(Corpus corpus) {
		this.corpus = corpus;
	}

	/**
	 * Walk all training samples and compute distance(). Return indexes of k
	 * smallest distance values.  Categories can be any negative or positive
	 * integer (and 0).
	 */
	public int classify(int k, int[] unknown, List<Integer> Y, double distanceThreshold) {
		HashBag<Integer> votes = votes(k, unknown, Y, distanceThreshold);
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

	public HashBag<Integer> votes(int k, int[] unknown, List<Integer> Y, double distanceThreshold) {
		Neighbor[] kNN = kNN(k, distanceThreshold, unknown);
		HashBag<Integer> votes = new HashBag<>();
		for (int i = 0; i<k && i<kNN.length; i++) {
			// Don't count any votes for training samples too distant.
			if ( kNN[i].distance>distanceThreshold ) {
				break;
			}
			votes.add(Y.get(kNN[i].corpusVectorIndex));
		}
		if ( dumpVotes && kNN.length>0 ) {
			System.out.print(CollectFeatures.featureNameHeader());
			InputDocument firstDoc = corpus.documents.get(kNN[0].corpusVectorIndex); // pick any neighbor to get parser
			System.out.println(CollectFeatures._toString(firstDoc.parser.getVocabulary(), firstDoc.parser.getRuleNames(), unknown)+"->"+votes);
			kNN = Arrays.copyOfRange(kNN, 0, Math.min(25,kNN.length));
			System.out.println(Utils.join(kNN, "\n"));
		}
		return votes;
	}

	public Neighbor[] kNN(int k, double distanceThreshold, int[] unknown) {
		Neighbor[] distances = distances(unknown, distanceThreshold);
		Arrays.sort(distances,
		            (Neighbor o1, Neighbor o2) -> Double.compare(o1.distance, o2.distance));
		return Arrays.copyOfRange(distances, 0, Math.min(k, distances.length));
	}

	public Neighbor[] distances(int[] unknown, double distanceThreshold) {
		int curTokenRuleIndex = unknown[CollectFeatures.INDEX_RULE];
		int prevTokenRuleIndex = unknown[CollectFeatures.INDEX_PREV_RULE];
		Pair<Integer, Integer> key = new Pair<>(curTokenRuleIndex, prevTokenRuleIndex);
		List<Integer> vectorIndexesMatchingTokenContext = corpus.curAndPrevTokenRuleIndexToVectorsMap.get(key);
		List<Neighbor> distances = new ArrayList<>();
		if ( vectorIndexesMatchingTokenContext==null ) {
			// no matching contexts for this feature, must rely on full training set
			int n = corpus.X.size(); // num training samples
			for (int i = 0; i<n; i++) {
				int[] x = corpus.X.get(i);
				Neighbor neighbor = new Neighbor(corpus, distance(x, unknown), i);
				distances.add(neighbor);
			}
		}
		else {
			int n = vectorIndexesMatchingTokenContext.size(); // num training samples
			for (int i = 0; i<n; i++) {
				Integer vectorIndex = vectorIndexesMatchingTokenContext.get(i);
				int[] x = corpus.X.get(vectorIndex);
				double d = distance(x, unknown);
				if ( d<=distanceThreshold ) {
					Neighbor neighbor = new Neighbor(corpus, d, vectorIndex);
					distances.add(neighbor);
				}
			}
		}
		return distances.toArray(new Neighbor[distances.size()]);
	}

	public abstract double distance(int[] A, int[] B);
}
