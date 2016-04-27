package org.antlr.codebuff.validation;

import org.antlr.v4.runtime.misc.Pair;

import java.util.List;

import static org.antlr.codebuff.Tool.JAVA8_DESCR;

public class Java8LeaveOneOut {
	public static void main(String[] args) throws Exception {
		LeaveOneOutValidator validator = new LeaveOneOutValidator("corpus/java/training/antlr4-tool", JAVA8_DESCR);
		Pair<List<Float>,List<Float>> results = validator.validateDocuments(true);
		System.out.println(results.a);
		System.out.println(results.b);
	}
}
