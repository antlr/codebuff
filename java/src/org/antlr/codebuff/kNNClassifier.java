package org.antlr.codebuff;

import org.antlr.codebuff.misc.HashBag;
import org.antlr.codebuff.misc.MutableDouble;
import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		for (int i=0; i<FEATURES.length; i++) {
			n += FEATURES[i].mismatchCost;
		}
		maxDistanceCount = n;
	}

	/** Classify unknown for all Y at once */
	public int[] classify(int k, int[] unknown, double distanceThreshold) {
		int[] categories = new int[Corpus.NUM_DEPENDENT_VARS];

		Neighbor[] kNN = kNN(unknown, k, distanceThreshold);
		HashBag<Integer> votesBag = getVotesBag(kNN, k, unknown, corpus.injectNewlines);
		categories[Corpus.INDEX_FEATURE_NEWLINES] = getCategoryWithMostVotes(votesBag);

		votesBag = getVotesBag(kNN, k, unknown, corpus.injectWS);
		categories[Corpus.INDEX_FEATURE_WS] = getCategoryWithMostVotes(votesBag);

		votesBag = getVotesBag(kNN, k, unknown, corpus.indent);
		categories[Corpus.INDEX_FEATURE_INDENT] = getCategoryWithMostVotes(votesBag);

		votesBag = getVotesBag(kNN, k, unknown, corpus.alignWithPrevious);
		categories[Corpus.INDEX_FEATURE_ALIGN_WITH_PREVIOUS] = getCategoryWithMostVotes(votesBag);

		return categories;
	}

	/**
	 * Walk all training samples and compute distance(). Return indexes of k
	 * smallest distance values.  Categories can be any negative or positive
	 * integer (and 0).
	 */
	public int classify(int k, int[] unknown, List<Integer> Y, double distanceThreshold) {
		HashBag<Integer> votes = votes(k, unknown, Y, distanceThreshold);
		return getCategoryWithMostVotes(votes);
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
			System.out.println(CollectFeatures._toString(FEATURES, firstDoc.parser.getVocabulary(), firstDoc.parser.getRuleNames(), unknown)+"->"+votes);
			kNN = Arrays.copyOfRange(kNN, 0, Math.min(k, kNN.length));
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i<kNN.length; i++) {
				Neighbor n = kNN[i];
				buf.append(n.toString(FEATURES, Y));
				buf.append("\n");
			}
			System.out.println(buf);
		}
		return votes;
	}

	/** Same as getVotesBag except sum the distances for each category rather than just count the instances */
	// TODO: not using just yet. I think we need to specialize features per classification
	public Map<Integer, MutableDouble> getCategoryDistanceMap(Neighbor[] kNN, int k, int[] unknown, List<Integer> Y) {
		Map<Integer, MutableDouble> catToDist = new HashMap<>();
		for (int i = 0; i<k && i<kNN.length; i++) {
			Integer category = Y.get(kNN[i].corpusVectorIndex);
			MutableDouble sum = catToDist.get(category);
			if ( sum==null ) {
				sum = new MutableDouble(0.0);
				catToDist.put(category, sum);
			}
			sum.add(kNN[i].distance);
		}
		return catToDist;
	}

	public Neighbor[] kNN(int[] unknown, int k, double distanceThreshold) {
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
