package org.antlr.codebuff.validation;

import org.antlr.codebuff.Formatter;
import org.antlr.v4.runtime.misc.Triple;

import java.util.List;

import static org.antlr.codebuff.Tool.QUORUM_DESCR;

public class FormatQuorum {
	public static void main(String[] args) throws Exception {
		LeaveOneOutValidator validator = new LeaveOneOutValidator(QUORUM_DESCR.corpusDir, QUORUM_DESCR);
		Triple<List<Formatter>,List<Float>,List<Float>> results = validator.validateDocuments(false, "output");
		System.out.println(results.b);
		System.out.println(results.c);
	}

}
