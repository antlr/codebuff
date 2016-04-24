package org.antlr.codebuff.validation;

import java.util.List;

import static org.antlr.codebuff.Tool.ANTLR4_DESCR;

public class ANTLR4LeaveOneOut {
	public static void main(String[] args) throws Exception {
		LeaveOneOutValidator validator = new LeaveOneOutValidator("corpus/antlr4/training", ANTLR4_DESCR);
		List<Float> distances = validator.validate(true);
		System.out.println(distances);
	}
}
