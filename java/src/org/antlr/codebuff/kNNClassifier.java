package org.antlr.codebuff;

import org.antlr.codebuff.misc.HashBag;
import org.antlr.codebuff.misc.MutableDouble;
import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.antlr.codebuff.CollectFeatures.MAX_CONTEXT_DIFF_THRESHOLD2;

/** A kNN (k-Nearest Neighbor) classifier */
public abstract class kNNClassifier {
	protected final Corpus corpus;
	protected final FeatureMetaData[] FEATURES;
	protected final int maxDistanceCount;

	public boolean dumpVotes = false;

	public kNNClassifier(Corpus corpus, FeatureMetaData[] FEATURES) {
		this.corpus = corpus;
		this.FEATURES = FEATURES;
		assert FEATURES.length <= CollectFeatures.NUM_FEATURES;
		int n = 0;
		for (FeatureMetaData FEATURE : FEATURES) {
			n += FEATURE.mismatchCost;
		}
		maxDistanceCount = n;
	}

	/**
	 * Walk all training samples and compute distance(). Return indexes of k
	 * smallest distance values.  Categories can be any negative or positive
	 * integer (and 0).
	 */
	public int classify(int k, int[] unknown, List<Integer> Y, double distanceThreshold) {
		HashBag<Integer> votes = votes(k, unknown, Y, distanceThreshold);
		if ( votes.size()==0 ) {
			// try with less strict match threshold to get some indication of alignment
			votes = votes(k, unknown, Y, MAX_CONTEXT_DIFF_THRESHOLD2);
		}
		return getCategoryWithMostVotes(votes);
	}

	public int classify2(int k, int[] unknown, List<Integer> Y, double distanceThreshold) {
		Neighbor[] kNN = kNN(unknown, k, distanceThreshold);
		Map<Integer,MutableDouble> similarities = getCategoryToSimilarityMap(kNN, k, Y);
		int cat = getCategoryWithMaxValue(similarities);

		if ( cat==-1 ) {
			// try with less strict match threshold to get some indication of alignment
			kNN = kNN(unknown, k, MAX_CONTEXT_DIFF_THRESHOLD2);
			similarities = getCategoryToSimilarityMap(kNN, k, Y);
			cat = getCategoryWithMaxValue(similarities);
		}
		return cat;
	}

	public int getCategoryWithMostVotes(HashBag<Integer> votes) {
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
		Neighbor[] kNN = kNN(unknown, k, distanceThreshold);
		return getVotesBag(kNN, k, unknown, Y);
	}

	public HashBag<Integer> getVotesBag(Neighbor[] kNN, int k, int[] unknown, List<Integer> Y) {
		HashBag<Integer> votes = new HashBag<>();
		for (int i = 0; i<k && i<kNN.length; i++) {
			votes.add(Y.get(kNN[i].corpusVectorIndex));
		}
		if ( dumpVotes && kNN.length>0 ) {
			System.out.print(CollectFeatures.featureNameHeader(FEATURES));
			InputDocument firstDoc = corpus.documents.get(kNN[0].corpusVectorIndex); // pick any neighbor to get parser
			System.out.println(CollectFeatures._toString(FEATURES, firstDoc, unknown)+"->"+votes);
			kNN = Arrays.copyOfRange(kNN, 0, Math.min(k, kNN.length));
			StringBuilder buf = new StringBuilder();
			for (Neighbor n : kNN) {
				buf.append(n.toString(FEATURES, Y));
				buf.append("\n");
			}
			System.out.println(buf);
		}
		return votes;
	}

	// get category similarity (1.0-distance) so we can weight votes. Just add up similarity.
	// I.e., weight votes with similarity 1 (distances of 0) more than votes with lower similarity
	// If we have 2 votes of distance 0.2 and 1 vote of distance 0, it means
	// we have 2 votes of similarity .8 and 1 of similarity of 1, 1.6 vs 6.
	// The votes still outweigh the similarity in this case. For a tie, however,
	// the weights will matter.
	public Map<Integer,MutableDouble> getCategoryToSimilarityMap(Neighbor[] kNN, int k, List<Integer> Y) {
		Map<Integer,MutableDouble> catSimilarities = new HashMap<>();
		for (int i = 0; i<k && i<kNN.length; i++) {
			int y = Y.get(kNN[i].corpusVectorIndex);
			MutableDouble d = catSimilarities.get(y);
			if ( d==null ) {
				d = new MutableDouble(0.0);
				catSimilarities.put(y, d);
			}
			d.add(1.0 - kNN[i].distance);
		}
		return catSimilarities;
	}

	public int getCategoryWithMaxValue(Map<Integer,MutableDouble> catSimilarities) {
		double max = Integer.MIN_VALUE;
		int catWithMaxSimilarity = -1;
		for (Integer category : catSimilarities.keySet()) {
			MutableDouble mutableDouble = catSimilarities.get(category);
			if ( mutableDouble.d>max ) {
				max = mutableDouble.d;
				catWithMaxSimilarity = category;
			}
		}

		return catWithMaxSimilarity;
	}

	public String getPredictionAnalysis(InputDocument doc, int k, int[] unknown, List<Integer> Y, double distanceThreshold) {
		Neighbor[] kNN = kNN(unknown, k, distanceThreshold);
		Map<Integer,MutableDouble> similarities = getCategoryToSimilarityMap(kNN, k, Y);
		int cat = getCategoryWithMaxValue(similarities);

		if ( cat==-1 ) {
			// try with less strict match threshold to get some indication of alignment
			kNN = kNN(unknown, k, MAX_CONTEXT_DIFF_THRESHOLD2);
			similarities = getCategoryToSimilarityMap(kNN, k, Y);
			cat = getCategoryWithMaxValue(similarities);
		}
		int[] elements = CollectFeatures.unaligncat(cat);
		String displayCat = String.format("%d|%d|%d", cat&0xFF, elements[0], elements[1]);

		StringBuilder buf = new StringBuilder();
		buf.append(CollectFeatures.featureNameHeader(FEATURES));
		buf.append(CollectFeatures._toString(FEATURES, doc, unknown)+"->"+similarities+" predicts "+displayCat);
		buf.append("\n");
		if ( kNN.length>0 ) {
			kNN = Arrays.copyOfRange(kNN, 0, Math.min(k, kNN.length));
			for (Neighbor n : kNN) {
				buf.append(n.toString(FEATURES, Y));
				buf.append("\n");
			}
		}
		return buf.toString();
	}

	public Neighbor[] kNN(int[] unknown, int k, double distanceThreshold) {
		Neighbor[] distances = distances(unknown, distanceThreshold);
		Arrays.sort(distances,
		            (Neighbor o1, Neighbor o2) -> Double.compare(o1.distance, o2.distance));
		return Arrays.copyOfRange(distances, 0, Math.min(k, distances.length));
	}

	public Neighbor[] distances(int[] unknown, double distanceThreshold) {
		int curTokenRuleIndex = unknown[CollectFeatures.INDEX_PREV_EARLIEST_RIGHT_ANCESTOR];
		int prevTokenRuleIndex = unknown[CollectFeatures.INDEX_EARLIEST_LEFT_ANCESTOR];
		int pr = CollectFeatures.unrulealt(prevTokenRuleIndex)[0];
		int cr = CollectFeatures.unrulealt(curTokenRuleIndex)[0];
		Pair<Integer, Integer> key =  new Pair<>(pr, cr);
		List<Integer> vectorIndexesMatchingTokenContext = corpus.curAndPrevTokenRuleIndexToVectorsMap.get(key);
		List<Neighbor> distances = new ArrayList<>();
		if ( vectorIndexesMatchingTokenContext==null ) {
			// no matching contexts for this feature, must rely on full training set
			int n = corpus.X.size(); // num training samples
			for (int i = 0; i<n; i++) {
				int[] x = corpus.X.get(i);
				double d = distance(x, unknown);
				if ( d<=distanceThreshold ) {
					Neighbor neighbor = new Neighbor(corpus, d, i);
					distances.add(neighbor);
				}
			}
		}
		else {
			for (Integer vectorIndex : vectorIndexesMatchingTokenContext) {
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
