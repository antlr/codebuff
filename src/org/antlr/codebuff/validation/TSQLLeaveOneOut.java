package org.antlr.codebuff.validation;

import org.antlr.v4.runtime.misc.Pair;

import java.util.List;

import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;

public class TSQLLeaveOneOut {
	public static void main(String[] args) throws Exception {
		LeaveOneOutValidator validator = new LeaveOneOutValidator("corpus/sql/training", TSQL_CLEAN_DESCR);
		Pair<List<Float>,List<Float>> results = validator.validateDocuments(true, true);
		System.out.println(results.a);
		System.out.println(results.b);
	}
}
