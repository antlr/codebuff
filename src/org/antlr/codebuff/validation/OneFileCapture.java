package org.antlr.codebuff.validation;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.LangDescriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_NOISY_DESCR;
import static org.antlr.codebuff.Tool.levenshteinDistance;

public class OneFileCapture {
	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[] {
//			JAVA_DESCR,
//			JAVA8_DESCR,
//			ANTLR4_DESCR,
			SQLITE_NOISY_DESCR,
			SQLITE_CLEAN_DESCR,
//			TSQL_NOISY_DESCR,
//			TSQL_CLEAN_DESCR,
		};
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			List<String> filenames = Tool.getFilenames(new File(language.corpusDir), language.fileRegex);
			List<Float> selfEditDistances = new ArrayList<>();
			for (String fileName : filenames) {
				Corpus corpus = new Corpus(fileName, language);
				corpus.train();
				InputDocument testDoc = Tool.parse(fileName, corpus.language);
				Formatter formatter = new Formatter(corpus, language.indentSize);
				String output = formatter.format(testDoc, false);
				//		System.out.println(output);
				float editDistance = levenshteinDistance(testDoc.content, output);
				System.out.println(fileName+" edit distance "+editDistance);
				selfEditDistances.add(editDistance);
			}

			List<Float> corpusEditDistances = new ArrayList<>();
			for (String fileName : filenames) {
				Corpus corpus = new Corpus(language.corpusDir, language);
				corpus.train();
				InputDocument testDoc = Tool.parse(fileName, corpus.language);
				Formatter formatter = new Formatter(corpus, language.indentSize);
				String output = formatter.format(testDoc, false);
				//		System.out.println(output);
				float editDistance = levenshteinDistance(testDoc.content, output);
				System.out.println(fileName+"+corpus edit distance "+editDistance);
				corpusEditDistances.add(editDistance);
			}
			// heh this gives info on within-corpus variability. i.e., how good/consistent is my corpus?
			// those files with big difference are candidates for dropping from corpus or for cleanup.
			System.out.println(selfEditDistances+"\nvs\n"+corpusEditDistances);
			List<Double> diffs = BuffUtils.diffFloats(corpusEditDistances, selfEditDistances);
			System.out.println(diffs);
			Collections.sort(selfEditDistances);
			Collections.sort(corpusEditDistances);
			float selfMedian = selfEditDistances.get(selfEditDistances.size()/2);
			float corpusMedian = corpusEditDistances.get(corpusEditDistances.size()/2);
			System.out.println(selfMedian+", "+corpusMedian);
		}
	}
}
