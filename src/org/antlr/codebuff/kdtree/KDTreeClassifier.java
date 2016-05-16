package org.antlr.codebuff.kdtree;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.FeatureMetaData;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Neighbor;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.Trainer;
import org.antlr.codebuff.misc.MutableDouble;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.antlr.codebuff.Trainer.CAT_INJECT_NL;
import static org.antlr.codebuff.Trainer.CAT_INJECT_WS;
import static org.antlr.codebuff.Trainer.MAX_CONTEXT_DIFF_THRESHOLD2;
import static org.antlr.codebuff.kNNClassifier.getCategoryToSimilarityMap;
import static org.antlr.codebuff.kNNClassifier.getCategoryWithMaxValue;

public class KDTreeClassifier {
	protected final Corpus corpus;
	protected final FeatureMetaData[] FEATURES;
	protected final int maxDistanceCount;

	public KDTreeClassifier(Corpus corpus, FeatureMetaData[] FEATURES) {
		this.corpus = corpus;
		this.FEATURES = FEATURES;
		int n = 0;
		for (FeatureMetaData FEATURE : FEATURES) {
			n += FEATURE.mismatchCost;
		}
		maxDistanceCount = n;
	}

	public int classify(int k, int[] unknown, List<Integer> Y, double distanceThreshold) {
		Neighbor[] kNN = kNN(unknown, k, distanceThreshold);
		Map<Integer, MutableDouble> similarities = getCategoryToSimilarityMap(kNN, k, Y);
		int cat = getCategoryWithMaxValue(similarities);

		if ( cat==-1 ) {
			// try with less strict match threshold to get some indication of alignment
			kNN = kNN(unknown, k, MAX_CONTEXT_DIFF_THRESHOLD2);
			similarities = getCategoryToSimilarityMap(kNN, k, Y);
			cat = getCategoryWithMaxValue(similarities);
		}

		return cat;
	}

	public Neighbor[] kNN(int[] unknown, int k, double distanceThreshold) {
//		NearestNeighbors<Exemplar,Integer> nn = new NearestNeighbors<>(new HammingDistance(FEATURES,maxDistanceCount));
//		NearestNeighbors.Entry<Exemplar, Integer>[] results = nn.get(corpus.kdtree, new Exemplar(corpus, unknown, -1), k, false);
//		List<Neighbor> neighbors = new ArrayList<>();
//		for (NearestNeighbors.Entry<Exemplar, Integer> entry : results) {
//			Map.Entry<Exemplar, Integer> neighbor = entry.getNeighbor();
//			if ( entry.getDistance()<=distanceThreshold ) {
//				System.out.println(entry.getDistance()+"     "+neighbor.getKey());
//				neighbors.add(new Neighbor(corpus, entry.getDistance(), neighbor.getValue()));
//			}
//		}
//
//		return neighbors.toArray(new Neighbor[neighbors.size()]);
		return null;
	}

	public String getPredictionAnalysis(InputDocument doc, int k, int[] unknown, List<Integer> Y, double distanceThreshold) {
		Neighbor[] kNN = kNN(unknown, k, distanceThreshold);
		Map<Integer, MutableDouble> similarities = getCategoryToSimilarityMap(kNN, k, Y);
		int cat = getCategoryWithMaxValue(similarities);
		if ( cat==-1 ) {
			// try with less strict match threshold to get some indication of alignment
			kNN = kNN(unknown, k, MAX_CONTEXT_DIFF_THRESHOLD2);
			similarities = getCategoryToSimilarityMap(kNN, k, Y);
			cat = getCategoryWithMaxValue(similarities);
		}

		String displayCat;
		int c = cat&0xFF;
		if ( c==CAT_INJECT_NL || c==CAT_INJECT_WS ) {
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

	/**
	 * Compute distance as a probability of match, based
	 * solely on context information.
	 * <p>
	 * Ratio of num differences / num total context positions.
	 */
	public double distance(int[] A, int[] B) {
//		return ((float)Tool.L0_Distance(categorical, A, B))/num_categorical;
		double d = Tool.weightedL0_Distance(FEATURES, A, B);
		return d/maxDistanceCount;
	}

}
