package org.antlr.codebuff.validation;

import java.util.List;

import static org.antlr.codebuff.Tool.TSQL_DESCR;

public class TSQLLeaveOneOut {
	public static void main(String[] args) throws Exception {
		LeaveOneOutValidator validator = new LeaveOneOutValidator("corpus/sql/training", TSQL_DESCR);
		List<Float> distances = validator.validate(true);
		System.out.println(distances);
	}
}
