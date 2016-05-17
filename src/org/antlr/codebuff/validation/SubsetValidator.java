package org.antlr.codebuff.validation;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.misc.Utils;
import org.stringtemplate.v4.ST;

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
import static org.antlr.codebuff.Tool.JAVA_GUAVA_DESCR;
import static org.antlr.codebuff.Tool.QUORUM_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.getFilenames;
import static org.antlr.codebuff.Tool.load;
import static org.antlr.codebuff.Tool.normalizedLevenshteinDistance;
import static org.antlr.codebuff.Tool.version;
import static org.antlr.codebuff.misc.BuffUtils.filter;
import static org.antlr.codebuff.misc.BuffUtils.map;

public class SubsetValidator {
	public static final int DOCLIST_RANDOM_SEED = 777111333; // need randomness but use same seed to get reproducibility

	public static final String outputDir = "/tmp";

	final static Random random = new Random();
	static {
		random.setSeed(DOCLIST_RANDOM_SEED);
	}

	public String rootDir;
	public LangDescriptor language;
	List<String> allFiles;

	public SubsetValidator(String rootDir, LangDescriptor language) throws Exception {
		this.rootDir = rootDir;
		this.language = language;
		allFiles = getFilenames(new File(rootDir), language.fileRegex);
	}

	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[] {
			QUORUM_DESCR,
			JAVA_DESCR,
			JAVA8_DESCR,
			JAVA_GUAVA_DESCR,
			ANTLR4_DESCR,
//			SQLITE_NOISY_DESCR,
			SQLITE_CLEAN_DESCR,
//			TSQL_NOISY_DESCR,
			TSQL_CLEAN_DESCR,
		};

		int maxNumFiles = 20;
		int trials = 50;
		Map<String,float[]> results = new HashMap<>();
		for (LangDescriptor language : languages) {
			float[] medians = getMedianErrorRates(language, maxNumFiles, trials);
			results.put(language.name, medians);
		}
		String python =
			"#\n"+
			"# AUTO-GENERATED FILE. DO NOT EDIT\n" +
			"# CodeBuff <version> '<date>'\n" +
			"#\n"+
			"import numpy as np\n"+
			"import matplotlib.pyplot as plt\n\n" +
			"fig = plt.figure()\n"+
			"ax = plt.subplot(111)\n"+
			"N = <maxNumFiles>\n" +
			"sizes = range(1,N+1)\n" +
			"<results:{r |\n" +
			"<r> = [<rest(results.(r)); separator={,}>]\n"+
			"ax.plot(sizes, <r>, label=\"<r>\", marker='o')\n" +
			"}>\n" +
			"ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)\n" +
			"ax.set_xlabel(\"Number n of training files in sample subset corpus\")\n"+
			"ax.set_ylabel(\"Median Error rate for <trials> trials\")\n" +
			"ax.set_title(\"Effect of Corpus size on Median Leave-one-out Validation Error Rate\")\n"+
			"plt.legend()\n" +
			"plt.tight_layout()\n" +
			"fig.savefig('images/subset_validator.pdf', format='pdf')\n"+
			"plt.show()\n";
		ST pythonST = new ST(python);
		pythonST.add("results", results);
		pythonST.add("version", version);
		pythonST.add("date", new Date());
		pythonST.add("trials", trials);
		pythonST.add("maxNumFiles", maxNumFiles);
		List<String> corpusDirs = map(languages, l -> l.corpusDir);
		String[] dirs = corpusDirs.toArray(new String[languages.length]);
		String fileName = "python/src/subset_validator.py";
		Utils.writeFile(fileName, pythonST.render());
		System.out.println("wrote python code to "+fileName);
	}

	public static float[] getMedianErrorRates(LangDescriptor language, int maxNumFiles, int trials) throws Exception {
		SubsetValidator validator = new SubsetValidator(language.corpusDir, language);
		List<InputDocument> documents = load(validator.allFiles, language);
		float[] medians = new float[maxNumFiles+1];
		for (int i = 1; i<=Math.min(validator.allFiles.size(), maxNumFiles); i++) { // i is corpus subset size
			List<Float> errorRates = new ArrayList<>();
			for (int trial = 1; trial<=trials; trial++) { // multiple trials per subset size
				Pair<InputDocument, List<InputDocument>> sample = validator.selectSample(documents, i);
				Triple<Formatter, Float, Float> results = validate(language, sample.b, sample.a, true, false);
				System.out.println(sample.a.fileName+" n="+i+": error="+results.c);
//				System.out.println("\tcorpus =\n\t\t"+Utils.join(sample.b.iterator(), "\n\t\t"));
				errorRates.add(results.c);
			}
			Collections.sort(errorRates);
			int n = errorRates.size();
			float min = errorRates.get(0);
			float quart = errorRates.get((int)(0.27*n));
			float median = errorRates.get(n/2);
			float quart3 = errorRates.get((int)(0.75*n));
			float max = errorRates.get(n-1);
			System.out.println("median error rate for n="+i+" is "+median);
			medians[i] = median;
		}
		return medians;
	}

	/** Select one document at random, then n others w/o replacement as corpus */
	public Pair<InputDocument, List<InputDocument>> selectSample(List<InputDocument> documents, int n) {
		int i = random.nextInt(documents.size());
		InputDocument testDoc = documents.get(i);
		List<InputDocument> others = filter(documents, d -> d!=testDoc);
		List<InputDocument> corpusSubset = getRandomDocuments(others, n);
		return new Pair<>(testDoc, corpusSubset);
	}

	public static Triple<Formatter,Float,Float> validate(LangDescriptor language,
	                                                     List<InputDocument> documents,
	                                                     InputDocument testDoc,
	                                                     boolean saveOutput,
	                                                     boolean computeEditDistance)
		throws Exception
	{
//		kNNClassifier.resetCache();
		Corpus corpus = new Corpus(documents, language);
		corpus.train();
//		System.out.printf("%d feature vectors\n", corpus.featureVectors.size());
		Formatter formatter = new Formatter(corpus, language.indentSize);
		String output = formatter.format(testDoc, false);
		float editDistance = 0;
		if ( computeEditDistance ) {
			editDistance = normalizedLevenshteinDistance(testDoc.content, output);
		}
		ClassificationAnalysis analysis = new ClassificationAnalysis(testDoc, formatter.getAnalysisPerToken());
//		System.out.println(testDoc.fileName+": edit distance = "+editDistance+", error rate = "+analysis.getErrorRate());
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


	/** From input documents, grab n in random order w/o replacement */
	public static List<InputDocument> getRandomDocuments(List<InputDocument> documents, int n) {
		List<InputDocument> documents_ = new ArrayList<>(documents);
		Collections.shuffle(documents_, random);
		List<InputDocument> contentList = new ArrayList<>(n);
		// get first n files from shuffle and set file index for it
		for (int i=0; i<Math.min(documents_.size(),n); i++) {
			contentList.add(documents_.get(i));
		}
		return contentList;
	}

	/** From input documents, grab n in random order w replacement */
	public static List<InputDocument> getRandomDocumentsWithRepl(List<InputDocument> documents, int n) {
		List<InputDocument> contentList = new ArrayList<>(n);
		for (int i=1; i<=n; i++) {
			int r = random.nextInt(documents.size()); // get random index from 0..|inputfiles|-1
			contentList.add(documents.get(r));
		}
		return contentList;
	}
}
