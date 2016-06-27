package org.antlr.codebuff.validation;

import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Utils;

import java.util.List;

import static org.antlr.codebuff.Tool.JAVA8_DESCR;
import static org.antlr.codebuff.Tool.JAVA_DESCR;
import static org.antlr.codebuff.Tool.JAVA_GUAVA_DESCR;
import static org.antlr.codebuff.misc.BuffUtils.map;
import static org.antlr.codebuff.validation.LeaveOneOutValidator.testAllLanguages;

public class AllJavaLeaveOneOutValidation {
	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[] {
			JAVA_DESCR,
			JAVA8_DESCR,
			JAVA_GUAVA_DESCR,
		};
		List<String> corpusDirs = map(languages, l -> l.corpusDir);
		String[] dirs = corpusDirs.toArray(new String[languages.length]);
		String python = testAllLanguages(languages, dirs, "all_java_leave_one_out.pdf");
		String fileName = "python/src/all_java_leave_one_out.py";
		Utils.writeFile(fileName, python);
		System.out.println("wrote python code to "+fileName);
	}
}
