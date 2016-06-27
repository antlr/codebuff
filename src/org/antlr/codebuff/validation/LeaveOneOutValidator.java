package org.antlr.codebuff.validation;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.FeatureMetaData;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.Trainer;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.LangDescriptor;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.antlr.codebuff.Dbg.normalizedLevenshteinDistance;
import static org.antlr.codebuff.Tool.ANTLR4_DESCR;
import static org.antlr.codebuff.Tool.JAVA8_DESCR;
import static org.antlr.codebuff.Tool.JAVA8_GUAVA_DESCR;
import static org.antlr.codebuff.Tool.JAVA_DESCR;
import static org.antlr.codebuff.Tool.JAVA_GUAVA_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_NOISY_DESCR;
import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.TSQL_NOISY_DESCR;
import static org.antlr.codebuff.Tool.getFilenames;
import static org.antlr.codebuff.misc.BuffUtils.filter;
import static org.antlr.codebuff.misc.BuffUtils.map;
import static org.antlr.codebuff.misc.BuffUtils.median;

public class LeaveOneOutValidator {
	public static final int DOCLIST_RANDOM_SEED = 951413; // need randomness but use same seed to get reproducibility
	final Random random = new Random();

	public static boolean FORCE_SINGLE_THREADED = false;

	public static final Map<String,String> nameToGraphMarker = new HashMap<String,String>() {{
		put("antlr", ".");
		put("java_st", "s");
		put("java8_st", "+");
		put("java_guava", ">");
		put("java8_guava", "d");
		put("sqlite", "o");
		put("tsql", "p");
	}};

	public static final Map<String,String> nameToGraphColor = new HashMap<String,String>() {{
		put("antlr", "k");
		put("java_st", "g");
		put("java8_st", "b");
		put("java_guava", "m");
		put("java8_guava", "c");
		put("sqlite", "y");
		put("tsql", "r");
	}};

	public String rootDir;
	public LangDescriptor language;

	List<Double> trainingTimes = new ArrayList<>();
	List<Double> formattingTokensPerMS = new ArrayList<>();

	public LeaveOneOutValidator(String rootDir, LangDescriptor language) {
		this.rootDir = rootDir;
		this.language = language;
		random.setSeed(DOCLIST_RANDOM_SEED);
	}

	public Triple<Formatter,Float,Float> validateOneDocument(String fileToExclude,
	                                                         String outputDir,
	                                                         boolean collectAnalysis)
		throws Exception
	{
		List<String> allFiles = getFilenames(new File(rootDir), language.fileRegex);
		List<InputDocument> documents = Tool.load(allFiles, language);
		return validate(language, documents, fileToExclude,
		                Formatter.DEFAULT_K, outputDir, false, collectAnalysis);
	}

	public Triple<List<Formatter>,List<Float>,List<Float>> validateDocuments(boolean computeEditDistance,
	                                                                         String outputDir)
		throws Exception
	{
		return validateDocuments(Trainer.FEATURES_INJECT_WS, Trainer.FEATURES_HPOS,
		                         computeEditDistance, outputDir);
	}

	public Triple<List<Formatter>,List<Float>,List<Float>> validateDocuments(FeatureMetaData[] injectWSFeatures,
	                                                                         FeatureMetaData[] alignmentFeatures,
	                                                                         boolean computeEditDistance,
	                                                                         String outputDir)
		throws Exception
	{
		List<Formatter> formatters = Collections.synchronizedList(new ArrayList<>());
		List<Float> distances = Collections.synchronizedList(new ArrayList<>());
		List<Float> errors = Collections.synchronizedList(new ArrayList<>());
		long start = System.nanoTime();
		try {
			List<String> allFiles = getFilenames(new File(rootDir), language.fileRegex);
			final List<InputDocument> documents = Tool.load(allFiles, language);
			final List<InputDocument> parsableDocuments = filter(documents, d -> d.tree!=null);
			long stop = System.nanoTime();
			System.out.printf("Load/parse all docs from %s time %d ms\n",
			                  rootDir,
			                  (stop-start)/1_000_000);

			int ncpu = Runtime.getRuntime().availableProcessors();
			if ( FORCE_SINGLE_THREADED ) {
				ncpu = 2;
			}
			ExecutorService pool = Executors.newFixedThreadPool(ncpu-1);
			List<Callable<Void>> jobs = new ArrayList<>();

			for (int i = 0; i<parsableDocuments.size(); i++) {
				final String fileName = parsableDocuments.get(i).fileName;
				Callable<Void> job = () -> {
					try {
						Triple<Formatter, Float, Float> results =
							validate(language, parsableDocuments, fileName,
							         Formatter.DEFAULT_K, injectWSFeatures, alignmentFeatures,
							         outputDir, computeEditDistance, false);
						formatters.add(results.a);
						float editDistance = results.b;
						distances.add(editDistance);
						Float errorRate = results.c;
						errors.add(errorRate);
					}
					catch (Throwable t) {
						t.printStackTrace(System.err);
					}
					return null;
				};
				jobs.add(job);
			}

			pool.invokeAll(jobs);
			pool.shutdown();
			pool.awaitTermination(60, TimeUnit.MINUTES);
		}
		finally {
			long final_stop = System.nanoTime();
			Double medianTrainingTime = median(trainingTimes);
			double medianFormattingPerMS = median(formattingTokensPerMS);
			System.out.printf("Total time %dms\n", (final_stop-start)/1_000_000);
			System.out.printf("Median training time %dms\n",
			                  medianTrainingTime.intValue());
			System.out.printf("Median formatting time tokens per ms %5.4fms, min %5.4f max %5.4f\n",
			                  medianFormattingPerMS,
			                  BuffUtils.min(formattingTokensPerMS),
			                  BuffUtils.max(formattingTokensPerMS));
		}
		return new Triple<>(formatters,distances,errors);
	}

	public Triple<Formatter,Float,Float> validate(LangDescriptor language,
	                                              List<InputDocument> documents,
	                                              String fileToExclude,
	                                              int k,
	                                              String outputDir,
	                                              boolean computeEditDistance,
	                                              boolean collectAnalysis)
		throws Exception
	{
		return validate(language, documents, fileToExclude,
		                k, Trainer.FEATURES_INJECT_WS, Trainer.FEATURES_HPOS,
		                outputDir, computeEditDistance, collectAnalysis);
	}

	public Triple<Formatter,Float,Float> validate(LangDescriptor language,
	                                              List<InputDocument> documents,
	                                              String fileToExclude,
	                                              int k,
	                                              FeatureMetaData[] injectWSFeatures,
	                                              FeatureMetaData[] alignmentFeatures,
	                                              String outputDir,
	                                              boolean computeEditDistance,
	                                              boolean collectAnalysis)
		throws Exception
	{
		final String path = new File(fileToExclude).getAbsolutePath();
		List<InputDocument> others = filter(documents, d -> !d.fileName.equals(path));
		List<InputDocument> excluded = filter(documents, d -> d.fileName.equals(path));
		assert others.size() == documents.size() - 1;
//		kNNClassifier.resetCache();
		if ( excluded.size()==0 ) {
			System.err.println("Doc not in corpus: "+path);
			return null;
		}
		InputDocument testDoc = excluded.get(0);
		long start = System.nanoTime();
		Corpus corpus = new Corpus(others, language);
		corpus.train();
		long stop = System.nanoTime();
		Formatter formatter = new Formatter(corpus, language.indentSize, k, injectWSFeatures, alignmentFeatures);
		InputDocument originalDoc = testDoc;
		long format_start = System.nanoTime();
		String output = formatter.format(testDoc, collectAnalysis);
		long format_stop = System.nanoTime();
		float editDistance = 0;
		if ( computeEditDistance ) {
			editDistance = normalizedLevenshteinDistance(testDoc.content, output);
		}
		ClassificationAnalysis analysis = new ClassificationAnalysis(originalDoc, formatter.getAnalysisPerToken());
		System.out.println(testDoc.fileName+": edit distance = "+editDistance+", error rate = "+analysis.getErrorRate());
		if ( outputDir!=null ) {
			File dir = new File(outputDir+"/"+language.name+"/"+Tool.version);
			if ( !dir.exists() ) {
				dir.mkdirs();
			}
			Utils.writeFile(dir.getPath()+"/"+new File(testDoc.fileName).getName(), output);
		}
		long tms = (stop - start) / 1_000_000;
		long fms = (format_stop - format_start) / 1_000_000;
		trainingTimes.add((double)tms);
		float tokensPerMS = testDoc.tokens.size() / (float) fms;
		formattingTokensPerMS.add((double)tokensPerMS);
		System.out.printf("Training time = %d ms, formatting %d ms, %5.3f tokens/ms (%d tokens)\n",
		                  tms,
		                  fms,
		                  tokensPerMS, testDoc.tokens.size());
//		System.out.printf("classify calls %d, hits %d rate %f\n",
//		                  kNNClassifier.nClassifyCalls, kNNClassifier.nClassifyCacheHits,
//		                  kNNClassifier.nClassifyCacheHits/(float) kNNClassifier.nClassifyCalls);
//		System.out.printf("kNN calls %d, hits %d rate %f\n",
//						  kNNClassifier.nNNCalls, kNNClassifier.nNNCacheHits,
//						  kNNClassifier.nNNCacheHits/(float) kNNClassifier.nNNCalls);
		return new Triple<>(formatter, editDistance, analysis.getErrorRate());
	}

	public static String testAllLanguages(LangDescriptor[] languages, String[] corpusDirs, String imageFileName) throws Exception {
		List<String> languageNames = map(languages, l -> l.name+"_err");
//		Collections.sort(languageNames);
		Map<String, Integer> corpusSizes = new HashMap<>();
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			List<String> filenames = Tool.getFilenames(new File(corpusDirs[i]), language.fileRegex);
			corpusSizes.put(language.name, filenames.size());
		}
		List<String> languageNamesAsStr = map(languages, l -> '"'+l.name+"\\nn="+corpusSizes.get(l.name)+'"');
//		Collections.sort(languageNamesAsStr);

		StringBuilder data = new StringBuilder();
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			String corpus = corpusDirs[i];
			LeaveOneOutValidator validator = new LeaveOneOutValidator(corpus, language);
			Triple<List<Formatter>,List<Float>,List<Float>> results = validator.validateDocuments(true, "/tmp");
			List<Formatter> formatters = results.a;
			List<Float> distances = results.b;
			List<Float> errors = results.c;
//			data.append(language.name+"_dist = "+distances+"\n");
			data.append(language.name+"_err = "+errors+"\n");
		}

		String python =
			"#\n"+
				"# AUTO-GENERATED FILE. DO NOT EDIT\n" +
				"# CodeBuff %s '%s'\n" +
				"#\n"+
				"import numpy as np\n"+
				"import pylab\n" +
				"import matplotlib.pyplot as plt\n\n" +
				"%s\n" +
				"language_data = %s\n"+
				"labels = %s\n"+
				"fig = plt.figure()\n"+
				"ax = plt.subplot(111)\n"+
				"ax.boxplot(language_data,\n"+
				"           whis=[10, 90], # 10 and 90 %% whiskers\n"+
				"           widths=.35,\n"+
				"           labels=labels,\n"+
				"           showfliers=False)\n"+
				"ax.set_xticklabels(labels, rotation=60, fontsize=18)\n"+
				"ax.tick_params(axis='both', which='major', labelsize=18)\n"+
				"plt.xticks(range(1,len(labels)+1), labels, rotation=60, fontsize=18)\n" +
				"pylab.ylim([0,.28])\n"+
				"ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)\n" +
				"ax.set_xlabel(\"Grammar and corpus size\", fontsize=20)\n"+
				"ax.set_ylabel(\"Misclassification Error Rate\", fontsize=20)\n" +
				"# ax.set_title(\"Leave-one-out Validation Using Error Rate\\nBetween Formatted and Original File\")\n"+
				"plt.tight_layout()\n" +
				"fig.savefig('images/%s', format='pdf')\n"+
				"plt.show()\n";
		return String.format(python, Tool.version, new Date(), data, languageNames, languageNamesAsStr, imageFileName);
	}

	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[] {
			JAVA_DESCR,
			JAVA8_DESCR,
			JAVA_GUAVA_DESCR,
			JAVA8_GUAVA_DESCR,
			ANTLR4_DESCR,
			SQLITE_CLEAN_DESCR,
			TSQL_CLEAN_DESCR,
			SQLITE_NOISY_DESCR,
			TSQL_NOISY_DESCR,
//			QUORUM_DESCR,
		};
		List<String> corpusDirs = map(languages, l -> l.corpusDir);
		String[] dirs = corpusDirs.toArray(new String[languages.length]);
		String python = testAllLanguages(languages, dirs, "leave_one_out.pdf");
		String fileName = "python/src/leave_one_out.py";
		Utils.writeFile(fileName, python);
		System.out.println("wrote python code to "+fileName);
	}
}
