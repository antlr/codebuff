package org.antlr.codebuff.validation;

import org.antlr.v4.runtime.misc.Pair;

import java.util.List;

import static org.antlr.codebuff.Tool.ANTLR4_DESCR;

public class ANTLR4LeaveOneOut {
	public static void main(String[] args) throws Exception {
		LeaveOneOutValidator validator = new LeaveOneOutValidator("corpus/antlr4/training", ANTLR4_DESCR);
		Pair<List<Float>,List<Float>> results = validator.validateDocuments(true, true);
		System.out.println(results.a);
		System.out.println(results.b);
	}
}
