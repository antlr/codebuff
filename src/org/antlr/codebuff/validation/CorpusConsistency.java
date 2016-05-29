package org.antlr.codebuff.validation;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.FeatureMetaData;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.Trainer;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.MultiMap;

import java.util.ArrayList;
import java.util.List;

import static org.antlr.codebuff.misc.BuffUtils.mean;
import static org.antlr.codebuff.misc.BuffUtils.median;
import static org.antlr.codebuff.misc.BuffUtils.sumDoubles;
import static org.antlr.codebuff.validation.Entropy.getCategoryRatios;
import static org.antlr.codebuff.validation.Entropy.getNormalizedCategoryEntropy;

public class CorpusConsistency {
	public static void main(String[] args) throws Exception {
		boolean report = false;
		if ( args.length>0 && args[0].equals("-report") ) {
			report = true;
		}
		for (LangDescriptor language : Tool.languages) {
			computeConsistency(language, report);
		}
	}

	public static void computeConsistency(LangDescriptor language, boolean report) throws Exception {
		if ( report ) {
			System.out.println("-----------------------------------");
			System.out.println(language.name);
			System.out.println("-----------------------------------");
		}
		Corpus corpus = new Corpus(language.corpusDir, language);
		corpus.train();
		// a map of feature vector to list of exemplar indexes of that feature
		MultiMap<FeatureVectorAsObject,Integer> wsContextToIndex = new MultiMap<>();
		MultiMap<FeatureVectorAsObject,Integer> hposContextToIndex = new MultiMap<>();

		int n = corpus.featureVectors.size();
		for (int i = 0; i<n; i++) {
			int[] features = corpus.featureVectors.get(i);
			wsContextToIndex.map(new FeatureVectorAsObject(features, Trainer.FEATURES_INJECT_WS), i);
			hposContextToIndex.map(new FeatureVectorAsObject(features, Trainer.FEATURES_HPOS), i);
		}

		int num_ambiguous_ws_vectors = 0;
		int num_ambiguous_hpos_vectors = 0;

		// Dump output grouped by ws vs hpos then feature vector then category
		if ( report ) System.out.println(" --- INJECT WS ---");
		List<Double> ws_entropies = new ArrayList<>();
		for (FeatureVectorAsObject fo : wsContextToIndex.keySet()) {
			List<Integer> exemplarIndexes = wsContextToIndex.get(fo);

			// we have group by feature vector, now group by cat with that set for ws
			MultiMap<Integer,Integer> wsCatToIndexes = new MultiMap<>();
			for (Integer i : exemplarIndexes) {
				wsCatToIndexes.map(corpus.injectWhitespace.get(i), i);
			}
			if ( wsCatToIndexes.size()==1 ) continue;
			if ( report ) System.out.println("Feature vector has "+exemplarIndexes.size()+" exemplars");
			List<Integer> catCounts = BuffUtils.map(wsCatToIndexes.values(), List::size);
			double wsEntropy = getNormalizedCategoryEntropy(getCategoryRatios(catCounts));
			if ( report ) System.out.printf("entropy=%5.4f\n", wsEntropy);
			wsEntropy *= exemplarIndexes.size();
			ws_entropies.add(wsEntropy);
			num_ambiguous_ws_vectors += exemplarIndexes.size();
			if ( report ) System.out.print(Trainer.featureNameHeader(Trainer.FEATURES_INJECT_WS));

			if ( report ) {
				for (Integer cat : wsCatToIndexes.keySet()) {
					List<Integer> indexes = wsCatToIndexes.get(cat);
					for (Integer i : indexes) {
						String display = getExemplarDisplay(Trainer.FEATURES_INJECT_WS, corpus, corpus.injectWhitespace, i);
						System.out.println(display);
					}
					System.out.println();
				}
			}
		}

		if ( report ) System.out.println(" --- HPOS ---");
		List<Double> hpos_entropies = new ArrayList<>();
		for (FeatureVectorAsObject fo : hposContextToIndex.keySet()) {
			List<Integer> exemplarIndexes = hposContextToIndex.get(fo);

			// we have group by feature vector, now group by cat with that set for hpos
			MultiMap<Integer,Integer> hposCatToIndexes = new MultiMap<>();
			for (Integer i : exemplarIndexes) {
				hposCatToIndexes.map(corpus.hpos.get(i), i);
			}
			if ( hposCatToIndexes.size()==1 ) continue;
			if ( report ) System.out.println("Feature vector has "+exemplarIndexes.size()+" exemplars");
			List<Integer> catCounts = BuffUtils.map(hposCatToIndexes.values(), List::size);
			double hposEntropy = getNormalizedCategoryEntropy(getCategoryRatios(catCounts));
			if ( report ) System.out.printf("entropy=%5.4f\n", hposEntropy);
			hposEntropy *= exemplarIndexes.size();
			hpos_entropies.add(hposEntropy);
			num_ambiguous_hpos_vectors += exemplarIndexes.size();
			if ( report ) System.out.print(Trainer.featureNameHeader(Trainer.FEATURES_HPOS));

			if ( report ) {
				for (Integer cat : hposCatToIndexes.keySet()) {
					List<Integer> indexes = hposCatToIndexes.get(cat);
					for (Integer i : indexes) {
						String display = getExemplarDisplay(Trainer.FEATURES_HPOS, corpus, corpus.hpos, i);
						System.out.println(display);
					}
					System.out.println();
				}
			}
		}
		System.out.println();
		System.out.println(language.name);
		System.out.println("There are "+wsContextToIndex.size()+" unique ws feature vectors out of "+n+" = "+
			                   String.format("%3.1f%%",100.0*wsContextToIndex.size()/n));
		System.out.println("There are "+hposContextToIndex.size()+" unique hpos feature vectors out of "+n+" = "+
			                   String.format("%3.1f%%",100.0*hposContextToIndex.size()/n));
		float prob_ws_ambiguous = num_ambiguous_ws_vectors/(float) n;
		System.out.printf("num_ambiguous_ws_vectors   = %5d/%5d = %5.3f\n", num_ambiguous_ws_vectors, n, prob_ws_ambiguous);
		float prob_hpos_ambiguous = num_ambiguous_hpos_vectors/(float) n;
		System.out.printf("num_ambiguous_hpos_vectors = %5d/%5d = %5.3f\n", num_ambiguous_hpos_vectors, n, prob_hpos_ambiguous);
//		Collections.sort(ws_entropies);
//		System.out.println("ws_entropies="+ws_entropies);
		System.out.println("ws median,mean = "+median(ws_entropies)+","+mean(ws_entropies));
		double expected_ws_entropy = (sumDoubles(ws_entropies)/num_ambiguous_ws_vectors) * prob_ws_ambiguous;
		System.out.println("expected_ws_entropy="+expected_ws_entropy);

		System.out.println("hpos median,mean = "+median(hpos_entropies)+","+mean(hpos_entropies));
		double expected_hpos_entropy = (sumDoubles(hpos_entropies)/num_ambiguous_hpos_vectors) * prob_hpos_ambiguous;
		System.out.println("expected_hpos_entropy="+expected_hpos_entropy);
	}

	public static String getExemplarDisplay(FeatureMetaData[] FEATURES, Corpus corpus, List<Integer> Y, int corpusVectorIndex) {
		int[] X = corpus.featureVectors.get(corpusVectorIndex);
		InputDocument doc = corpus.documentsPerExemplar.get(corpusVectorIndex);
		String features = Trainer._toString(FEATURES, doc, X);
		int line = X[Trainer.INDEX_INFO_LINE];
		String lineText = doc.getLine(line);
		int col = X[Trainer.INDEX_INFO_CHARPOS];
		// insert a dot right before char position
		if ( lineText!=null ) {
			lineText = lineText.substring(0, col)+'\u00B7'+lineText.substring(col, lineText.length());
		}
		int cat = Y.get(corpusVectorIndex);
		String displayCat;
		if ( (cat&0xFF) == Trainer.CAT_INJECT_WS || (cat&0xFF) == Trainer.CAT_INJECT_NL) {
			displayCat = Formatter.getWSCategoryStr(cat);
		}
		else {
			displayCat = Formatter.getHPosCategoryStr(cat);
		}

		return String.format("%s %9s %s", features, displayCat, lineText);
	}
}
