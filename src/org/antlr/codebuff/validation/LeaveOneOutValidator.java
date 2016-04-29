package org.antlr.codebuff.validation;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.kNNClassifier;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.misc.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.antlr.codebuff.Tool.ANTLR4_DESCR;
import static org.antlr.codebuff.Tool.JAVA8_DESCR;
import static org.antlr.codebuff.Tool.JAVA_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_NOISY_DESCR;
import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.TSQL_NOISY_DESCR;
import static org.antlr.codebuff.Tool.getFilenames;
import static org.antlr.codebuff.Tool.levenshteinDistance;
import static org.antlr.codebuff.Tool.load;
import static org.antlr.codebuff.Tool.parse;
import static org.antlr.codebuff.misc.BuffUtils.filter;
import static org.antlr.codebuff.misc.BuffUtils.map;

public class LeaveOneOutValidator {
	public static final int DOCLIST_RANDOM_SEED = 951413; // need randomness but use same seed to get reproducibility

	public static final String outputDir = "/tmp";

	final Random random = new Random();


	public String rootDir;
	public LangDescriptor language;

	public LeaveOneOutValidator(String rootDir, LangDescriptor language) {
		this.rootDir = rootDir;
		this.language = language;
		random.setSeed(DOCLIST_RANDOM_SEED);
	}

	public Triple<Formatter,Float,Float> validateOneDocument(String fileToExclude, boolean saveOutput)
		throws Exception
	{
		List<String> allFiles = getFilenames(new File(rootDir), language.fileRegex);
		List<InputDocument> documents = load(allFiles, language);
		return validate(language, documents, fileToExclude, Formatter.DEFAULT_K, saveOutput, true);
	}

	public Pair<List<Float>,List<Float>> validateDocuments(boolean saveOutput) throws Exception {
		List<String> allFiles = getFilenames(new File(rootDir), language.fileRegex);
		List<InputDocument> documents = load(allFiles, language);
		List<Float> distances = new ArrayList<>();
		List<Float> errors = new ArrayList<>();
		for (int i = 0; i<documents.size(); i++) {
			Triple<Formatter,Float,Float> results =
				validate(language, documents, documents.get(i).fileName,
				         Formatter.DEFAULT_K, saveOutput, true);
			float editDistance = results.b;
			distances.add(editDistance);
			Float errorRate = results.c;
			errors.add(errorRate);
		}
		return new Pair<>(distances,errors);
	}

	public static Triple<Formatter,Float,Float> validate(LangDescriptor language,
	                                                     List<InputDocument> documents,
	                                                     String fileToExclude,
	                                                     int k,
	                                                     boolean saveOutput,
	                                                     boolean computeEditDistance)
		throws Exception
	{
		final String path = new File(fileToExclude).getCanonicalPath();
		List<InputDocument> others = filter(documents, d -> !d.fileName.equals(path));
		List<InputDocument> excluded = filter(documents, d -> d.fileName.equals(path));
		assert others.size() == documents.size() - 1;
		kNNClassifier.resetCache();
		InputDocument testDoc = excluded.get(0);
		Corpus corpus = new Corpus(others, language);
		corpus.train();
		Formatter formatter = new Formatter(corpus, k);
		InputDocument originalDoc = testDoc;
		String output = formatter.format(testDoc, false);
		// doc.tokens is now corrupt, find test doc in list and freshen (yuck)
		for (int i = 0; i<documents.size(); i++) {
			if ( documents.get(i)==testDoc ) {
				documents.set(i, parse(testDoc.fileName, language));
				break;
			}
		}
		float editDistance = 0;
		if ( computeEditDistance ) {
			editDistance = levenshteinDistance(testDoc.content, output);
		}
		ClassificationAnalysis analysis = new ClassificationAnalysis(originalDoc, formatter.getAnalysisPerToken());
		System.out.println(testDoc.fileName+": edit distance = "+editDistance+", error rate = "+analysis.getErrorRate());
		if ( saveOutput ) {
			File dir = new File(outputDir+"/"+language.name);
			if ( saveOutput ) {
				dir = new File(outputDir+"/"+language.name);
				dir.mkdir();
			}
			Utils.writeFile(dir.getPath()+"/"+new File(testDoc.fileName).getName(), output);
		}
		return new Triple<>(formatter, editDistance, analysis.getErrorRate());
	}

	public static String testAllLanguages(LangDescriptor[] languages, String[] corpusDirs) throws Exception {
		List<String> languageNames = map(languages, l -> l.name+"_dist");
		languageNames.addAll(map(languages, l -> l.name+"_err"));
		Collections.sort(languageNames);
		Map<String, Integer> corpusSizes = new HashMap<>();
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			List<String> filenames = Tool.getFilenames(new File(corpusDirs[i]), language.fileRegex);
			corpusSizes.put(language.name, filenames.size());
		}
		List<String> languageNamesAsStr = map(languages, l -> '"'+l.name+"\\nn="+corpusSizes.get(l.name)+'"');
		languageNamesAsStr.addAll(map(languages, l -> '"'+l.name+"_err\\nn="+corpusSizes.get(l.name)+'"'));
		Collections.sort(languageNamesAsStr);

		StringBuilder data = new StringBuilder();
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			String corpus = corpusDirs[i];
			LeaveOneOutValidator validator = new LeaveOneOutValidator(corpus, language);
			Pair<List<Float>,List<Float>> results = validator.validateDocuments(true);
			List<Float> distances = results.a;
			List<Float> errors = results.b;
			data.append(language.name+"_dist = "+distances+"\n");
			data.append(language.name+"_err = "+errors+"\n");
		}

		String python =
			"#\n"+
				"# AUTO-GENERATED FILE. DO NOT EDIT\n" +
				"# CodeBuff %s '%s'\n" +
				"#\n"+
				"import numpy as np\n"+
				"import matplotlib.pyplot as plt\n\n" +
				"%s\n" +
				"language_data = %s\n"+
				"labels = %s\n"+
				"fig = plt.figure()\n"+
				"ax = plt.subplot(111)\n"+
				"ax.boxplot(language_data,\n"+
				"           whis=[10, 90], # 10 and 90 %% whiskers\n"+
				"           widths=.35,\n"+
				"           labels=labels)\n"+
				"ax.set_xlabel(\"Grammar and corpus size\")\n"+
				"ax.set_ylabel(\"Edit distance / size of file\")\n" +
				"ax.set_title(\"Leave-one-out Validation Using Edit Distance / Error Rate\\nBetween Formatted and Original File\")\n"+
				"plt.show()\n";
		return String.format(python, Tool.version, new Date(), data, languageNames, languageNamesAsStr);
	}

	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[] {
			JAVA_DESCR,
			JAVA8_DESCR,
			ANTLR4_DESCR,
			SQLITE_NOISY_DESCR,
			SQLITE_CLEAN_DESCR,
			TSQL_NOISY_DESCR,
			TSQL_CLEAN_DESCR,
		};
		List<String> corpusDirs = map(languages, l -> l.corpusDir);
		String[] dirs = corpusDirs.toArray(new String[languages.length]);
		String python = testAllLanguages(languages, dirs);
		String fileName = "python/src/leave_one_out.py";
		Utils.writeFile(fileName, python);
		System.out.println("wrote python code to "+fileName);
	}
}
