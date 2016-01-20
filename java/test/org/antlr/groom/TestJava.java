package org.antlr.groom;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/*
A useful measure would be "stability"; train a doc in isolation then format
that doc and measure similarity. Ideally, we'd see 0 difference but
around 0.01 would be ok not great (1%). 0.001 would be great!
We also want determinism where running format operation always gets same
result for same training corpus and test doc.
 */

public class TestJava {
	public static final String ST4_CORPUS = "../samples/stringtemplate4";

	@Test
	public void testStability() throws Exception {
		String fileName = "src/org/antlr/groom/InputDocument.java";
		Corpus corpus = Tool.train(fileName);
		InputDocument testDoc = Tool.load(fileName);
		String output = Tool.format(corpus, testDoc);
		double d = Tool.whitespaceDifference(new String(testDoc.content), output, JavaLexer.class);
		System.out.println("Diff is "+d);
		System.out.println(output);
		assertTrue(d<0.05);
	}
}
