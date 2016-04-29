package org.antlr.codebuff.validation;

import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.misc.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.antlr.codebuff.Tool.ANTLR4_DESCR;
import static org.antlr.codebuff.Tool.JAVA8_DESCR;
import static org.antlr.codebuff.Tool.JAVA_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.getFilenames;
import static org.antlr.codebuff.Tool.load;

public class TestK extends LeaveOneOutValidator {
	public final int k;

	public TestK(String rootDir, LangDescriptor language, int k) {
		super(rootDir, language);
		this.k = k;
	}

	/** Return error rate for each document using leave-one-out validation */
	public List<Float> scoreDocuments() throws Exception {
		List<String> allFiles = getFilenames(new File(rootDir), language.fileRegex);
		List<InputDocument> documents = load(allFiles, language);
		List<Float> errors = new ArrayList<>();
		for (int i = 0; i<documents.size(); i++) {
			Triple<Formatter,Float,Float> results =
				validate(language, documents, documents.get(i).fileName, k, false, false, false);
			Float errorRate = results.c;
			errors.add(errorRate);
		}
		return errors;
	}

	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[] {
			JAVA_DESCR,
			JAVA8_DESCR,
			ANTLR4_DESCR,
//			SQLITE_NOISY_DESCR,
			SQLITE_CLEAN_DESCR,
//			TSQL_NOISY_DESCR,
			TSQL_CLEAN_DESCR,
		};

		int MAX_K = 31; // should be odd
		int OUTLIER_K = 131;
		List<Integer> ks = new ArrayList<>();
		for (int i = 1; i<=MAX_K; i+=2) {
			ks.add(i);
		}
		ks.add(OUTLIER_K);
		// track medians[language][k]
		List<Float>[] medians = new List[languages.length];
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			System.out.println(language.name);
			medians[i] = new ArrayList<>();
			for (int k : ks) {
				TestK tester = new TestK(language.corpusDir, language, k);
				List<Float> errorRates = tester.scoreDocuments();
				Collections.sort(errorRates);
				int n = errorRates.size();
				float min = errorRates.get(0);
				float quart = errorRates.get((int)(0.27*n));
				float median = errorRates.get(n/2);
				float quart3 = errorRates.get((int)(0.75*n));
				float max = errorRates.get(n-1);
				double var = BuffUtils.varianceFloats(errorRates);
				String display = String.format("%5.4f, %5.4f, %5.4f, %5.4f, %5.4f", min, quart, median, quart3, max);
				medians[i].add(median);
			}
		}

		StringBuilder data = new StringBuilder();
		StringBuilder plot = new StringBuilder();
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			data.append(language.name+'='+medians[i]+'\n');
			plot.append(String.format("ax.plot(ks, %s, label=\"%s\", marker='o')\n", language.name, language.name));
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
			"ax.set_xlabel(\"k nearest neighbors\")\n"+
			"ax.set_ylabel(\"Error rate\")\n" +
			"ax.set_title(\"k Nearest Neighbors vs\\nLeave-one-out Validation Error Rate\")\n"+
			"plt.legend()\n" +
			"plt.show()\n";
		String code = String.format(python, Tool.version, new Date(), data, ks, plot);

		String fileName = "python/src/vary_k.py";
		Utils.writeFile(fileName, code);
		System.out.println("wrote python code to "+fileName);
	}
}
