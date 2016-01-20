package org.antlr.groom;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestJava {
	public static final String ST4_CORPUS = "../samples/stringtemplate4";

	@Test
	public void testFoo() throws Exception {
		Corpus corpus = Tool.train(ST4_CORPUS);
		InputDocument testDoc = Tool.load("src/org/antlr/groom/InputDocument.java");
		String output = Tool.format(corpus, testDoc);
		int d = Tool.levenshteinDistance(new String(testDoc.content), output);
		System.out.println("Diff is "+d);
		System.out.println(output);
		assertTrue(d<5);
	}
}
