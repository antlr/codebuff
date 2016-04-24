package org.antlr.codebuff;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BaseTest {
	public String fileName;

	public BaseTest(String fileName) {
		this.fileName = fileName;
	}

	@Test
	public void testCapture() throws Exception {
		Corpus corpus = new Corpus(fileName, ".*\\.java", Tool.JAVA_DESCR);
		corpus.train();
		InputDocument testDoc = Tool.load(fileName, corpus.language);
		Formatter formatter = new Formatter(corpus);
		String output = formatter.format(testDoc, false);
		float editDistance = formatter.getEditDistance();
		System.out.println("edit distance "+editDistance);
		assertTrue("WS edit distance too high "+editDistance, editDistance < 0.05);
	}
}
