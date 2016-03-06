package org.antlr.codebuff;

import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Corpus {
	public static final int NUM_DEPENDENT_VARS = 4;
	public static final int INDEX_FEATURE_NEWLINES = 0;
	public static final int INDEX_FEATURE_WS = 1;
	public static final int INDEX_FEATURE_INDENT = 2;
	public static final int INDEX_FEATURE_LEVELS_TO_ANCESTOR = 3;

	List<InputDocument> documents; // an entry for each X
	List<int[]> X;
	List<Integer> injectNewlines;
	List<Integer> injectWS;
	List<Integer> indent;
	List<Integer> alignWithPrevious; // steps to common ancestor whose first token is alignment anchor

	/** an index to narrow down the number of vectors we compute distance() on each classification.
	 *  The key is (previous token's rule index, current token's rule index). It yields
	 *  a list of vectors with same key. Created by {@link #buildTokenContextIndex}.
	 */
	Map<Pair<Integer,Integer>, List<Integer>> curAndPrevTokenRuleIndexToVectorsMap;

	public Corpus(List<InputDocument> documents,
				  List<int[]> X,
				  List<Integer> injectNewlines,
				  List<Integer> injectWS,
				  List<Integer> indent,
				  List<Integer> alignWithPrevious)
	{
		this.documents = documents;
		this.X = X;
		this.injectNewlines = injectNewlines;
		this.injectWS = injectWS;
		this.indent = indent;
		this.alignWithPrevious = alignWithPrevious;
	}

	public void buildTokenContextIndex() {
		curAndPrevTokenRuleIndexToVectorsMap = new HashMap<>();
		for (int i=0; i<X.size(); i++) {
			int curTokenRuleIndex = X.get(i)[CollectFeatures.INDEX_RULE];
			int prevTokenRuleIndex = X.get(i)[CollectFeatures.INDEX_PREV_RULE];
			Pair<Integer, Integer> key = new Pair<>(curTokenRuleIndex, prevTokenRuleIndex);
			List<Integer> vectorIndexes = curAndPrevTokenRuleIndexToVectorsMap.get(key);
			if ( vectorIndexes==null ) {
				vectorIndexes = new ArrayList<>();
				curAndPrevTokenRuleIndexToVectorsMap.put(key, vectorIndexes);
			}
			vectorIndexes.add(i);
		}
	}
}
