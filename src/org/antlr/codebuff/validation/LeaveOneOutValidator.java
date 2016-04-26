package org.antlr.codebuff.validation;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.kNNClassifier;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
import static org.antlr.codebuff.misc.BuffUtils.filter;
import static org.antlr.codebuff.misc.BuffUtils.map;

public class LeaveOneOutValidator {
	public static final int DOCLIST_RANDOM_SEED = 951413; // need randomness but use same seed to get reproducibility

	public static final String outputDir = "/tmp";

	final Random random = new Random();

	public String rootDir;
	public LangDescriptor language;

	public List<InputDocument> documents;

	public LeaveOneOutValidator(String rootDir, LangDescriptor language) {
		this.rootDir = rootDir;
		this.language = language;
		random.setSeed(DOCLIST_RANDOM_SEED);
	}

	public Pair<Formatter,Float> validateOneDocument(String fileToExclude, boolean collectAnalysis, boolean saveOutput)
		throws Exception
	{
		List<String> allFiles = getFilenames(new File(rootDir), language.fileRegex);
		documents = load(allFiles, language);
		return validate(fileToExclude, collectAnalysis, saveOutput);
	}

	public List<Float> validateDocuments(boolean saveOutput) throws Exception {
		List<String> allFiles = getFilenames(new File(rootDir), language.fileRegex);
		documents = load(allFiles, language);
		List<Float> distances = new ArrayList<>();
		for (int i = 0; i<documents.size(); i++) {
			Pair<Formatter,Float> results = validate(documents.get(i).fileName, false, saveOutput);
			float editDistance = results.b;
			distances.add(editDistance);
		}
		return distances;
	}

	public Pair<Formatter,Float> validate(String fileToExclude, boolean collectAnalysis, boolean saveOutput)
		throws Exception
	{
		List<InputDocument> others = filter(documents, d -> !d.fileName.endsWith(fileToExclude));
		List<InputDocument> excluded = filter(documents, d -> d.fileName.endsWith(fileToExclude));
		kNNClassifier.resetCache();
		InputDocument testDoc = excluded.get(0);
		Corpus corpus = new Corpus(others, language);
		corpus.train();
		Formatter formatter = new Formatter(corpus);
		String output = formatter.format(testDoc, collectAnalysis);
		float editDistance = levenshteinDistance(testDoc.content, output);
		System.out.println(testDoc.fileName+": "+editDistance);
		if ( saveOutput ) {
			File dir = new File(outputDir+"/"+language.name);
			if ( saveOutput ) {
				dir = new File(outputDir+"/"+language.name);
				dir.mkdir();
			}
			Utils.writeFile(dir.getPath()+"/"+new File(testDoc.fileName).getName(), output);
		}
		return new Pair<>(formatter, editDistance);
	}

	/** From input documents, grab n in random order w/o replacement */
	public List<InputDocument> getRandomDocuments(List<InputDocument> documents, int n) {
		List<InputDocument> documents_ = new ArrayList<>(documents);
		Collections.shuffle(documents_, random);
		List<InputDocument> contentList = new ArrayList<>(n);
		for (int i=0; i<n; i++) { // get first n files from shuffle and set file index for it
			contentList.add(documents.get(i));
		}
		return contentList;
	}

	/** From input documents, grab n in random order w replacement */
	public List<InputDocument> getRandomDocumentsWithRepl(List<InputDocument> documents, int n) {
		List<InputDocument> contentList = new ArrayList<>(n);
		for (int i=1; i<=n; i++) {
			int r = random.nextInt(documents.size()); // get random index from 0..|inputfiles|-1
			contentList.add(documents.get(r));
		}
		return contentList;
	}

	public static String testAllLanguages(LangDescriptor[] languages, String[] corpusDirs) throws Exception {
		List<String> languageNames = map(languages, l -> l.name);
		Map<String, Integer> corpusSizes = new HashMap<>();
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			List<String> filenames = Tool.getFilenames(new File(corpusDirs[i]), language.fileRegex);
			corpusSizes.put(language.name, filenames.size());
		}
		List<String> languageNamesAsStr = map(languages, l -> '"'+l.name+"\\nn="+corpusSizes.get(l.name)+'"');

		StringBuilder data = new StringBuilder();
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			String corpus = corpusDirs[i];
			LeaveOneOutValidator validator = new LeaveOneOutValidator(corpus, language);
			List<Float> distances = validator.validateDocuments(true);
			data.append(language.name+" = "+distances+"\n");
		}

		String python =
			"# CodeBuff AUTO-GENERATED FILE. DO NOT EDIT\n"+
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
			"ax.set_title(\"Leave-one-out Validation Using Edit Distance\\nBetween Formatted and Original File\")\n"+
			"plt.show()\n";
		return String.format(python, data, languageNames, languageNamesAsStr);
	}

	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[] {
//			JAVA_DESCR,
//			JAVA8_DESCR,
//			ANTLR4_DESCR,
			SQLITE_NOISY_DESCR,
			SQLITE_CLEAN_DESCR,
			TSQL_NOISY_DESCR,
			TSQL_CLEAN_DESCR,
		};
		List<String> corpusDirs = map(languages, l -> l.corpusDir);
		String[] dirs = corpusDirs.toArray(new String[languages.length]);
		String python = testAllLanguages(languages, dirs);
		System.out.println(python);
	}
}
