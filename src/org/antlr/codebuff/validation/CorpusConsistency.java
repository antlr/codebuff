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
		foo(Tool.ANTLR4_DESCR);
	}

	public static void foo(LangDescriptor language) throws Exception {
		Corpus corpus = new Corpus(language.corpusDir, language);
		corpus.train();
		// a map of feature vector to list of exemplar indexes of that feature
		MultiMap<FeatureVectorAsObject,Integer> groupByFeatures = new MultiMap<>();
		MultiMap<FeatureVectorAsObject,Integer> featuresToHPosCat = new MultiMap<>();

		for (int i = 0; i<corpus.featureVectors.size(); i++) {
			int[] features = corpus.featureVectors.get(i);
			groupByFeatures.map(new FeatureVectorAsObject(features), i);
		}

		// Dump output grouped by feature vector then category
		for (FeatureVectorAsObject fo : groupByFeatures.keySet()) {
			List<Integer> exemplarIndexes = groupByFeatures.get(fo);

			// we have group by feature vector, now group by cat with that set for ws, hpos
			MultiMap<Integer,Integer> wsCatToIndexes = new MultiMap<>();
			MultiMap<Integer,Integer> hposCatToIndexes = new MultiMap<>();
			for (Integer i : exemplarIndexes) {
				wsCatToIndexes.map(corpus.injectWhitespace.get(i), i);
				hposCatToIndexes.map(corpus.hpos.get(i), i);
			}
			if ( wsCatToIndexes.size()==1 ) continue;

			System.out.println();
			System.out.print(Trainer.featureNameHeader(Trainer.FEATURES_INJECT_WS));

			for (Integer cat : wsCatToIndexes.keySet()) {
				List<Integer> indexes = wsCatToIndexes.get(cat);
				String displayCat = Formatter.getWSCategoryStr(cat);
				Integer corpusVectorIndex = indexes.get(0);
				InputDocument doc = corpus.documentsPerExemplar.get(corpusVectorIndex);
				String features = Trainer._toString(Trainer.FEATURES_INJECT_WS, doc, fo.features, false);
//				System.out.println();
//				System.out.println(features+" -> "+displayCat);
				for (Integer i : indexes) {
					String display = getExemplarDisplay(Trainer.FEATURES_INJECT_WS, corpus, corpus.injectWhitespace, i);
					System.out.println(display);
				}
				System.out.println();
			}
		}
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
		int[] elements = Trainer.triple(cat);
//		String display = String.format("%d|%d|%d", cat&0xFF, elements[0], elements[1]);
		String displayCat = Formatter.getWSCategoryStr(cat);

		return String.format("%s %9s %s", features, displayCat, lineText);
	}
}
