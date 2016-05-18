package org.antlr.codebuff.validation;

import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.antlr.codebuff.Tool.ANTLR4_DESCR;
import static org.antlr.codebuff.Tool.JAVA8_DESCR;
import static org.antlr.codebuff.Tool.JAVA_DESCR;
import static org.antlr.codebuff.Tool.QUORUM_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_NOISY_DESCR;
import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.TSQL_NOISY_DESCR;

public class Stability {
	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[]{
			QUORUM_DESCR,
			JAVA_DESCR,
			JAVA8_DESCR,
			ANTLR4_DESCR,
			SQLITE_NOISY_DESCR,
			SQLITE_CLEAN_DESCR,
			TSQL_NOISY_DESCR,
			TSQL_CLEAN_DESCR,
		};

		Map<String, List<Float>> results = new HashMap<>();
		for (LangDescriptor language : languages) {
			List<Float> errorRates = checkStability(language);
			System.out.println(language.name+" "+errorRates);
			results.put(language.name, errorRates);
		}
		for (String name : results.keySet()) {
			System.out.println(name+" "+results.get(name));
		}
	}

	public static List<Float> checkStability(LangDescriptor language) throws Exception {
		int stages = 10;
		List<Float> errorRates = new ArrayList<>();

		LeaveOneOutValidator validator0 = new LeaveOneOutValidator(language.corpusDir, language);
		Triple<List<Formatter>, List<Float>, List<Float>> results0 = validator0.validateDocuments(false, "/tmp/stability/1");
		errorRates.add( BuffUtils.median(results0.c) );

		for (int i=1; i<=stages; i++) {
			String inputDir  = "/tmp/stability/"+i;
			String outputDir = "/tmp/stability/"+(i+1);
			LeaveOneOutValidator validator = new LeaveOneOutValidator(inputDir, language);
			Triple<List<Formatter>, List<Float>, List<Float>> results =
				validator.validateDocuments(false, outputDir);
			errorRates.add( BuffUtils.median(results.c) );
		}

		return errorRates;
	}
}
