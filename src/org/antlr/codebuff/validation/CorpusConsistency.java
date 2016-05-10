package org.antlr.codebuff.validation;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.FeatureMetaData;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.Trainer;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.MultiMap;

import java.util.List;

public class CorpusConsistency {
	public static void main(String[] args) throws Exception {
		dumpConsistencyReport(Tool.ANTLR4_DESCR);
	}

	public static void dumpConsistencyReport(LangDescriptor language) throws Exception {
		Corpus corpus = new Corpus(language.corpusDir, language);
		corpus.train();
		// a map of feature vector to list of exemplar indexes of that feature
		MultiMap<FeatureVectorAsObject,Integer> groupByFeatures = new MultiMap<>();
		MultiMap<FeatureVectorAsObject,Integer> featuresToHPosCat = new MultiMap<>();

		for (int i = 0; i<corpus.featureVectors.size(); i++) {
			int[] features = corpus.featureVectors.get(i);
			groupByFeatures.map(new FeatureVectorAsObject(features), i);
		}

		int num_ambiguous_ws_vectors = 0;
		int num_ambiguous_hpos_vectors = 0;

		// Dump output grouped by ws vs hpos then feature vector then category
		System.out.println(" --- INJECT WS ---");
		for (FeatureVectorAsObject fo : groupByFeatures.keySet()) {
			List<Integer> exemplarIndexes = groupByFeatures.get(fo);

			// we have group by feature vector, now group by cat with that set for ws
			MultiMap<Integer,Integer> wsCatToIndexes = new MultiMap<>();
			for (Integer i : exemplarIndexes) {
				wsCatToIndexes.map(corpus.injectWhitespace.get(i), i);
			}
			if ( wsCatToIndexes.size()==1 ) continue;
			System.out.println("Feature vector has "+exemplarIndexes.size()+" exemplars");
			num_ambiguous_ws_vectors += exemplarIndexes.size();
			System.out.print(Trainer.featureNameHeader(Trainer.FEATURES_INJECT_WS));

			for (Integer cat : wsCatToIndexes.keySet()) {
				List<Integer> indexes = wsCatToIndexes.get(cat);
				for (Integer i : indexes) {
					String display = getExemplarDisplay(Trainer.FEATURES_INJECT_WS, corpus, corpus.injectWhitespace, i);
					System.out.println(display);
				}
				System.out.println();
			}
		}

		System.out.println(" --- HPOS ---");
		for (FeatureVectorAsObject fo : groupByFeatures.keySet()) {
			List<Integer> exemplarIndexes = groupByFeatures.get(fo);

			// we have group by feature vector, now group by cat with that set for hpos
			MultiMap<Integer,Integer> hposCatToIndexes = new MultiMap<>();
			for (Integer i : exemplarIndexes) {
				hposCatToIndexes.map(corpus.hpos.get(i), i);
			}
			if ( hposCatToIndexes.size()==1 ) continue;
			System.out.println("Feature vector has "+exemplarIndexes.size()+" exemplars");
			num_ambiguous_hpos_vectors += exemplarIndexes.size();
			System.out.print(Trainer.featureNameHeader(Trainer.FEATURES_HPOS));

			for (Integer cat : hposCatToIndexes.keySet()) {
				List<Integer> indexes = hposCatToIndexes.get(cat);
				for (Integer i : indexes) {
					String display = getExemplarDisplay(Trainer.FEATURES_HPOS, corpus, corpus.hpos, i);
					System.out.println(display);
				}
				System.out.println();
			}
		}
		System.out.println("There are "+groupByFeatures.size()+" unique feature vectors out of "+corpus.featureVectors.size());
		System.out.println("num_ambiguous_ws_vectors="+num_ambiguous_ws_vectors);
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
