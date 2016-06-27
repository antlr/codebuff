package org.antlr.codebuff.validation;

import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.misc.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.antlr.codebuff.Tool.ANTLR4_DESCR;
import static org.antlr.codebuff.Tool.JAVA8_DESCR;
import static org.antlr.codebuff.Tool.JAVA_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.getFilenames;

public class TestK extends LeaveOneOutValidator {
	public static final boolean FORCE_SINGLE_THREADED = false;
	public final int k;

	public TestK(String rootDir, LangDescriptor language, int k) {
		super(rootDir, language);
		this.k = k;
	}

	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[] {
			JAVA_DESCR,
			JAVA8_DESCR,
//			JAVA_GUAVA_DESCR,
//			JAVA8_GUAVA_DESCR,
			ANTLR4_DESCR,
			SQLITE_CLEAN_DESCR,
			TSQL_CLEAN_DESCR,
		};

		int MAX_K = 98; // should be odd
		int OUTLIER_K = 99;
		List<Integer> ks = new ArrayList<>();
		for (int i = 1; i<=MAX_K; i+=2) {
			ks.add(i);
		}
		ks.add(OUTLIER_K);
		// track medians[language][k]
		Float[][] medians = new Float[languages.length+1][];

		int ncpu = Runtime.getRuntime().availableProcessors();
		if ( FORCE_SINGLE_THREADED ) {
			ncpu = 2;
		}
		ExecutorService pool = Executors.newFixedThreadPool(ncpu-1);
		List<Callable<Void>> jobs = new ArrayList<>();

		for (int i = 0; i<languages.length; i++) {
			final LangDescriptor language = languages[i];
			final int langIndex = i;
			System.out.println(language.name);
			for (int k : ks) {
				medians[langIndex] = new Float[OUTLIER_K+1];
				Callable<Void> job = () -> {
					try {
						TestK tester = new TestK(language.corpusDir, language, k);
						List<Float> errorRates = tester.scoreDocuments();
						Collections.sort(errorRates);
						int n = errorRates.size();
						float median = errorRates.get(n/2);
//						double var = BuffUtils.varianceFloats(errorRates);
//						String display = String.format("%5.4f, %5.4f, %5.4f, %5.4f, %5.4f", min, quart, median, quart3, max);
						medians[langIndex][k] = median;
					}
					catch (Throwable t) {
						t.printStackTrace(System.err);
					}
					return null;
				};
				jobs.add(job);
			}
		}

		pool.invokeAll(jobs);
		pool.shutdown();
		boolean terminated = pool.awaitTermination(60, TimeUnit.MINUTES);

		writePython(languages, ks, medians);
	}

	public static void writePython(LangDescriptor[] languages, List<Integer> ks, Float[][] medians) throws IOException {
		StringBuilder data = new StringBuilder();
		StringBuilder plot = new StringBuilder();
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			List<Float> filteredMedians = BuffUtils.filter(Arrays.asList(medians[i]), m -> m!=null);
			data.append(language.name+'='+filteredMedians+'\n');
			plot.append(String.format("ax.plot(ks, %s, label=\"%s\", marker='%s', color='%s')\n",
			                          language.name, language.name,
			                          nameToGraphMarker.get(language.name),
									  nameToGraphColor.get(language.name)));
		}

		String python =
			"#\n"+
			"# AUTO-GENERATED FILE. DO NOT EDIT\n" +
			"# CodeBuff %s '%s'\n" +
			"#\n"+
			"import numpy as np\n"+
			"import matplotlib.pyplot as plt\n\n" +
			"%s\n" +
			"ks = %s\n"+
			"fig = plt.figure()\n"+
			"ax = plt.subplot(111)\n"+
			"%s"+
			"ax.tick_params(axis='both', which='major', labelsize=18)\n" +
			"ax.set_xlabel(\"$k$ nearest neighbors\", fontsize=20)\n"+
			"ax.set_ylabel(\"Median error rate\", fontsize=20)\n" +
			"#ax.set_title(\"k Nearest Neighbors vs\\nLeave-one-out Validation Error Rate\")\n"+
			"plt.legend(fontsize=18)\n\n" +
			"fig.savefig('images/vary_k.pdf', format='pdf')\n"+
			"plt.show()\n";
		String code = String.format(python, Tool.version, new Date(), data, ks, plot);

		String fileName = "python/src/vary_k.py";
		Utils.writeFile(fileName, code);
		System.out.println("wrote python code to "+fileName);
	}

	/** Return error rate for each document using leave-one-out validation */
	public List<Float> scoreDocuments() throws Exception {
		List<String> allFiles = getFilenames(new File(rootDir), language.fileRegex);
		List<InputDocument> documents = Tool.load(allFiles, language);
		List<Float> errors = new ArrayList<>();
		for (int i = 0; i<documents.size(); i++) {
			Triple<Formatter,Float,Float> results =
				validate(language, documents, documents.get(i).fileName, k, null, false, false);
			Float errorRate = results.c;
			errors.add(errorRate);
		}
		return errors;
	}
}
