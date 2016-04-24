package org.antlr.codebuff.validation;

import java.util.List;

import static org.antlr.codebuff.Tool.JAVA8_DESCR;

public class Java8LeaveOneOut {
	public static void main(String[] args) throws Exception {
		LeaveOneOutValidator validator = new LeaveOneOutValidator("corpus/java/training", JAVA8_DESCR);
		List<Float> distances = validator.validate(true);
		System.out.println(distances);
	}
}
