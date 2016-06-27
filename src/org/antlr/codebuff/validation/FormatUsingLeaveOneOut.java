package org.antlr.codebuff.validation;

import org.antlr.codebuff.misc.LangDescriptor;

import static org.antlr.codebuff.Tool.ANTLR4_DESCR;
import static org.antlr.codebuff.Tool.JAVA8_DESCR;
import static org.antlr.codebuff.Tool.JAVA_DESCR;
import static org.antlr.codebuff.Tool.JAVA_GUAVA_DESCR;
import static org.antlr.codebuff.Tool.QUORUM_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;

public class FormatUsingLeaveOneOut extends LeaveOneOutValidator {
	public FormatUsingLeaveOneOut(String rootDir, LangDescriptor language) {
		super(rootDir, language);
	}

	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[] {
			QUORUM_DESCR,
			JAVA_DESCR,
			JAVA8_DESCR,
			JAVA_GUAVA_DESCR,
			ANTLR4_DESCR,
//			SQLITE_NOISY_DESCR,
			SQLITE_CLEAN_DESCR,
//			TSQL_NOISY_DESCR,
			TSQL_CLEAN_DESCR,
		};

		// walk and generator output but no edit distance
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			LeaveOneOutValidator validator = new LeaveOneOutValidator(language.corpusDir, language);
			validator.validateDocuments(false, "output");
		}
	}
}
