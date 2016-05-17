package org.antlr.codebuff;

import org.antlr.codebuff.misc.HashBag;
import org.antlr.codebuff.misc.MutableDouble;
import org.antlr.codebuff.validation.FeatureVectorAsObject;
import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.antlr.codebuff.Trainer.CAT_INJECT_NL;
import static org.antlr.codebuff.Trainer.CAT_INJECT_WS;
import static org.antlr.codebuff.Trainer.MAX_CONTEXT_DIFF_THRESHOLD2;

/** A kNN (k-Nearest Neighbor) classifier */
public abstract class kNNClassifier {
	protected final Corpus corpus;
	protected final FeatureMetaData[] FEATURES;
	protected final int maxDistanceCount;

	public boolean dumpVotes = false;

	public static Map<Pair<FeatureVectorAsObject,Integer>,Integer> classifyCache = new HashMap<>();
	public static int nClassifyCalls=0;
	public static int nClassifyCacheHits=0;

	public static Map<FeatureVectorAsObject, Neighbor[]> neighborCache = new HashMap<>();
	public static int nNNCalls=0;
	public static int nNNCacheHits=0;

	public kNNClassifier(Corpus corpus, FeatureMetaData[] FEATURES) {
		this.corpus = corpus;
		this.FEATURES = FEATURES;
		assert FEATURES.length <= Trainer.NUM_FEATURES;
		int n = 0;
		for (FeatureMetaData FEATURE : FEATURES) {
			n += FEATURE.mismatchCost;
		}
		maxDistanceCount = n;
	}

	public static void resetCache() {
		classifyCache.clear();
		neighborCache.clear();
		nClassifyCacheHits = 0;
		nClassifyCalls=0;
		nClassifyCacheHits=0;
		nNNCalls=0;
		nNNCacheHits=0;
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
		Pair<FeatureVectorAsObject,Integer> key = new Pair<>(new FeatureVectorAsObject(unknown),
		                                                     Y==corpus.injectWhitespace? 0 : 1);
		Integer catI = classifyCache.get(key);
		nClassifyCalls++;
		if ( catI!=null ) {
			nClassifyCacheHits++;
			return catI;
		}
		Neighbor[] kNN = kNN(unknown, k, distanceThreshold);
		Map<Integer,MutableDouble> similarities = getCategoryToSimilarityMap(kNN, k, Y);
		int cat = getCategoryWithMaxValue(similarities);

		if ( cat==-1 ) {
			// try with less strict match threshold to get some indication of alignment
			kNN = kNN(unknown, k, MAX_CONTEXT_DIFF_THRESHOLD2);
			similarities = getCategoryToSimilarityMap(kNN, k, Y);
			cat = getCategoryWithMaxValue(similarities);
		}

		classifyCache.put(key, cat);
		return cat;
	}

	public static int getCategoryWithMostVotes(HashBag<Integer> votes) {
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
			System.out.print(Trainer.featureNameHeader(FEATURES));
			InputDocument firstDoc = corpus.documentsPerExemplar.get(kNN[0].corpusVectorIndex); // pick any neighbor to get parser
			System.out.println(Trainer._toString(FEATURES, firstDoc, unknown)+"->"+votes);
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
			double emphasizedDistance = Math.pow(kNN[i].distance, 1.0/3); // cube root
			d.add(1.0 - emphasizedDistance);
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
		FeatureVectorAsObject key = new FeatureVectorAsObject(unknown);
		Neighbor[] kNN = neighborCache.get(key);
		nNNCalls++;
		if ( kNN==null ) {
			kNN = kNN(unknown, k, distanceThreshold);
			neighborCache.put(key, kNN);
		}
		else {
			nNNCacheHits++;
		}
		Map<Integer, MutableDouble> similarities = getCategoryToSimilarityMap(kNN, k, Y);
		int cat = getCategoryWithMaxValue(similarities);
		if (cat == -1) {
			// try with less strict match threshold to get some indication of alignment
			kNN = kNN(unknown, k, MAX_CONTEXT_DIFF_THRESHOLD2);
			similarities = getCategoryToSimilarityMap(kNN, k, Y);
			cat = getCategoryWithMaxValue(similarities);
		}

		String displayCat;
		int c = cat&0xFF;
		if ( c==CAT_INJECT_NL||c==CAT_INJECT_WS ) {
			displayCat = Formatter.getWSCategoryStr(cat);
		}
		else {
			displayCat = Formatter.getHPosCategoryStr(cat);
		}
		displayCat = displayCat!=null ? displayCat : "none";

		StringBuilder buf = new StringBuilder();
		buf.append(Trainer.featureNameHeader(FEATURES));
		buf.append(Trainer._toString(FEATURES, doc, unknown)+"->"+similarities+" predicts "+displayCat);
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
		Neighbor[] distances = distances(unknown, k, distanceThreshold);
		Arrays.sort(distances,
		            (Neighbor o1, Neighbor o2) -> Double.compare(o1.distance, o2.distance));
		return Arrays.copyOfRange(distances, 0, Math.min(k, distances.length));
	}

	public Neighbor[] distances(int[] unknown, int k, double distanceThreshold) {
		int curTokenRuleIndex = unknown[Trainer.INDEX_PREV_EARLIEST_RIGHT_ANCESTOR];
		int prevTokenRuleIndex = unknown[Trainer.INDEX_EARLIEST_LEFT_ANCESTOR];
		int pr = Trainer.unrulealt(prevTokenRuleIndex)[0];
		int cr = Trainer.unrulealt(curTokenRuleIndex)[0];
		Pair<Integer, Integer> key =  new Pair<>(pr, cr);
		List<Integer> vectorIndexesMatchingTokenContext = corpus.curAndPrevTokenRuleIndexToVectorsMap.get(key);
		if ( distanceThreshold==MAX_CONTEXT_DIFF_THRESHOLD2 ) { // couldn't find anything, open it all up.
			vectorIndexesMatchingTokenContext = null;
		}
		List<Neighbor> distances = new ArrayList<>();
		if ( vectorIndexesMatchingTokenContext==null ) {
			// no matching contexts for this feature, must rely on full training set
			int n = corpus.featureVectors.size(); // num training samples
			int num0 = 0; // how many 0-distance elements have we seen? If k we can stop!
			for (int i = 0; i<n; i++) {
				int[] x = corpus.featureVectors.get(i);
				double d = distance(x, unknown);
				if ( d<=distanceThreshold ) {
					Neighbor neighbor = new Neighbor(corpus, d, i);
					distances.add(neighbor);
					if ( d==0.0 ) {
						num0++;
						if ( num0==k ) break;
					}
				}
			}
		}
		else {
			int num0 = 0; // how many 0-distance elements have we seen? If k we can stop!
			for (Integer vectorIndex : vectorIndexesMatchingTokenContext) {
				int[] x = corpus.featureVectors.get(vectorIndex);
				double d = distance(x, unknown);
				if ( d<=distanceThreshold ) {
					Neighbor neighbor = new Neighbor(corpus, d, vectorIndex);
					distances.add(neighbor);
					if ( d==0.0 ) {
						num0++;
						if ( num0==k ) break;
					}
				}
			}
		}
		return distances.toArray(new Neighbor[distances.size()]);
	}

	public abstract double distance(int[] A, int[] B);
}
