package org.antlr.codebuff.validation;

import java.util.List;

import static org.antlr.codebuff.Tool.JAVA_DESCR;

public class JavaLeaveOneOut {
	public static void main(String[] args) throws Exception {
		LeaveOneOutValidator validator = new LeaveOneOutValidator("corpus/java/training/antlr4-tool", JAVA_DESCR);
		List<Float> distances = validator.validateDocuments(true);
		System.out.println(distances);
	}
}
