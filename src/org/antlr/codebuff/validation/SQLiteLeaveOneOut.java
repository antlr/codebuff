package org.antlr.codebuff.validation;

import java.util.List;

import static org.antlr.codebuff.Tool.SQLITE_DESCR;

public class SQLiteLeaveOneOut {
	public static void main(String[] args) throws Exception {
		LeaveOneOutValidator validator = new LeaveOneOutValidator("corpus/sql/training", SQLITE_DESCR);
		List<Float> distances = validator.validateDocuments(true);
		System.out.println(distances);
	}
}
