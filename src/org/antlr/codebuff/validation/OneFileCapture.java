package org.antlr.codebuff.validation;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.misc.LangDescriptor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.antlr.codebuff.Tool.languages;
import static org.antlr.codebuff.Tool.levenshteinDistance;
import static org.antlr.codebuff.misc.BuffUtils.map;

public class OneFileCapture {

	public static String testAllLanguages(LangDescriptor[] languages, String[] corpusDirs) throws Exception {
		List<String> languageNames = map(languages, l -> l.name);
		Map<String, Integer> corpusSizes = new HashMap<>();
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			List<String> filenames = Tool.getFilenames(new File(corpusDirs[i]), language.fileRegex);
			for (String fileName : filenames) {
				corpusSizes.put(language.name, filenames.size());
				Corpus corpus = new Corpus(fileName, Tool.ANTLR4_DESCR);
				corpus.train();
				InputDocument testDoc = Tool.parse(fileName, corpus.language);
				Formatter formatter = new Formatter(corpus);
				String output = formatter.format(testDoc, false);
				float editDistance = levenshteinDistance(testDoc.content, output);
				System.out.println("edit distance "+editDistance);
			}
		}
		return "";
	}

	public static void main(String[] args) throws Exception {
		List<String> corpusDirs = map(languages, l -> l.corpusDir);
		String[] dirs = corpusDirs.toArray(new String[languages.length]);
		testAllLanguages(languages, dirs);
	}
}
