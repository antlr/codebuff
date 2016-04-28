package org.antlr.codebuff.validation;

import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.misc.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.antlr.codebuff.Tool.ANTLR4_DESCR;
import static org.antlr.codebuff.Tool.JAVA8_DESCR;
import static org.antlr.codebuff.Tool.JAVA_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_NOISY_DESCR;
import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.TSQL_NOISY_DESCR;
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
			Triple<Formatter,Float,Float> results = validate(documents, documents.get(i).fileName, k, false, false);
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
			SQLITE_NOISY_DESCR,
			SQLITE_CLEAN_DESCR,
			TSQL_NOISY_DESCR,
			TSQL_CLEAN_DESCR,
		};

		LangDescriptor language = TSQL_CLEAN_DESCR;
		List<String> data = new ArrayList<>();
		for (int k = 1; k<=31; k+=2) {
			if ( k==31 ) { // jump
				k = 111;
			}
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
			data.add(display);
			System.out.println();
			System.out.printf("k = %3d, (min,quartile1,median,quartile3,max) = (%s)\n", k, display);
		}
		System.out.println(Utils.join(data.iterator(), "\n"));
	}
}
