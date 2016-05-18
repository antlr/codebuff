package org.antlr.codebuff.validation;

import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Triple;

import java.util.ArrayList;
import java.util.List;

import static org.antlr.codebuff.Tool.JAVA_DESCR;

public class JavaStability {
	public static void main(String[] args) throws Exception {
		int stages = 5;
		List<Float> errorRates = new ArrayList<>();

		LangDescriptor language = JAVA_DESCR;

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

		System.out.println();
		System.out.println(errorRates);
	}
}
