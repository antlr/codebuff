package org.antlr.codebuff;

import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Corpus {
	public static final int FEATURE_VECTOR_RANDOM_SEED = 314159; // need randomness but use same seed to get reproducibility

	public static final int NUM_DEPENDENT_VARS = 3;
	public static final int INDEX_FEATURE_NEWLINES = 0;
	public static final int INDEX_FEATURE_WS = 1;
	public static final int INDEX_FEATURE_ALIGN_WITH_PREVIOUS = 2;

	List<InputDocument> documents; // an entry for each X
	List<int[]> X;
	List<Integer> injectNewlines;
	List<Integer> align; // steps to common ancestor whose first token is alignment anchor
	List<Integer> injectWS;

	/** an index to narrow down the number of vectors we compute distance() on each classification.
	 *  The key is (previous token's rule index, current token's rule index). It yields
	 *  a list of vectors with same key. Created by {@link #buildTokenContextIndex}.
	 */
	Map<Pair<Integer,Integer>, List<Integer>> curAndPrevTokenRuleIndexToVectorsMap;

	public Corpus(List<InputDocument> documents,
				  List<int[]> X,
				  List<Integer> injectNewlines,
				  List<Integer> align,
				  List<Integer> injectWS)
	{
		this.documents = documents;
		this.X = X;
		this.injectNewlines = injectNewlines;
		this.injectWS = injectWS;
		this.align = align;
	}

	/** Feature vectors in X are lumped together as they are read in each
	 *  document. In kNN, this tends to find features from the same document
	 *  rather than from across the corpus since we grab k neighbors.
	 *  For k=11, we might only see exemplars from a single corpus document.
	 *  If all exemplars fit in k, this wouldn't be an issue.
	 *
	 *  Fisher-Yates / Knuth shuffling
	 *  "To shuffle an array a of n elements (indices 0..n-1)":
	 *  https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
	 */
	public void randomShuffleInPlace() {
		Random r = new Random();
		r.setSeed(FEATURE_VECTOR_RANDOM_SEED);
		// for i from n−1 downto 1 do
		int n = X.size();
		for (int i=n-1; i>=1; i--) {
			// j ← random integer such that 0 ≤ j ≤ i
			int j = r.nextInt(i+1);
			// exchange a[j] and a[i]
			// Swap X
			int[] tmp = X.get(i);
			X.set(i, X.get(j));
			X.set(j, tmp);
			// And now swap all prediction lists
			Integer tmpI = injectNewlines.get(i);
			injectNewlines.set(i, injectNewlines.get(j));
			injectNewlines.set(j, tmpI);
			tmpI = align.get(i);
			align.set(i, align.get(j));
			align.set(j, tmpI);
			tmpI = injectWS.get(i);
			injectWS.set(i, injectWS.get(j));
			injectWS.set(j, tmpI);
			// Finally, swap documents
			InputDocument tmpD = documents.get(i);
			documents.set(i, documents.get(j));
			documents.set(j, tmpD);
		}
	}

	public void buildTokenContextIndex() {
		curAndPrevTokenRuleIndexToVectorsMap = new HashMap<>();
		for (int i=0; i<X.size(); i++) {
			int curTokenRuleIndex = X.get(i)[CollectFeatures.INDEX_RULE];
			int prevTokenRuleIndex = X.get(i)[CollectFeatures.INDEX_PREV_RULE];
			int pr = CollectFeatures.unrulealt(prevTokenRuleIndex)[0];
			int cr = CollectFeatures.unrulealt(curTokenRuleIndex)[0];
			Pair<Integer, Integer> key = new Pair<>(pr, cr);
			List<Integer> vectorIndexes = curAndPrevTokenRuleIndexToVectorsMap.get(key);
			if ( vectorIndexes==null ) {
				vectorIndexes = new ArrayList<>();
				curAndPrevTokenRuleIndexToVectorsMap.put(key, vectorIndexes);
			}
			vectorIndexes.add(i);
		}
	}
}
