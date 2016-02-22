package org.antlr.codebuff;

import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Corpus {
	List<InputDocument> documents; // an entry for each X
	List<int[]> X;
	List<Integer> injectNewlines;
	List<Integer> injectWS;
	List<Integer> indent;
	List<Integer> levelsToCommonAncestor; // steps to common ancestor whose first token is alignment anchor

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
				  List<Integer> levelsToCommonAncestor)
	{
		this.documents = documents;
		this.X = X;
		this.injectNewlines = injectNewlines;
		this.injectWS = injectWS;
		this.indent = indent;
		this.levelsToCommonAncestor = levelsToCommonAncestor;
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
