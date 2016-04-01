package org.antlr.codebuff;

import org.antlr.v4.runtime.misc.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

/*
A useful measure would be "stability"; train a doc in isolation then format
that doc and measure similarity. Ideally, we'd see 0 difference but
around 0.01 would be ok not great (1%). 0.001 would be great!
We also want determinism where running format operation always gets same
result for same training corpus and test doc.
 */

@RunWith(Parameterized.class)
public class TestJavaStability {
	public static final String ST4_CORPUS = "../samples/stringtemplate4/org";

	public String fileName;

	public TestJavaStability(String fileName) {
		this.fileName = fileName;
	}

	@Test
	public void testStability() throws Exception {
		Corpus corpus = Tool.train(fileName, ".*\\.java", JavaLexer.class, JavaParser.class, "compilationUnit", 4);
		InputDocument testDoc = Tool.load(fileName, JavaLexer.class, 4);
		Pair<String,List<TokenPositionAnalysis>> results = Tool.format(corpus, testDoc, JavaLexer.class, JavaParser.class, "compilationUnit", 4);
		String output = results.a;
		List<TokenPositionAnalysis> analysisPerToken = results.b;
		double d = Tool.docDiff(testDoc.content, output, JavaLexer.class);
		System.out.println("Diff is "+d);
		System.out.println(output);
		assertTrue(d<0.05);
	}

	@Parameterized.Parameters(name="{0}")
	public static Collection<Object[]> findInputFiles() throws Exception {
		List<Object[]> args = new ArrayList<>();
		List<String> filenames = Tool.getFilenames(new File(ST4_CORPUS), ".*\\.java");
		for (String fname : filenames) {
			args.add(new Object[] {fname});
		}
		return args;
	}
}
