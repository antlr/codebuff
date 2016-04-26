package org.antlr.codebuff.validation;

import java.util.List;

import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;

public class TSQLLeaveOneOut {
	public static void main(String[] args) throws Exception {
		LeaveOneOutValidator validator = new LeaveOneOutValidator("corpus/sql/training", TSQL_CLEAN_DESCR);
		List<Float> distances = validator.validateDocuments(true);
		System.out.println(distances);
	}
}
