package org.antlr.codebuff;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestSQLCapture extends BaseTest {
	public static final String CORPUS = "corpus/sql/training";

	public TestSQLCapture(String fileName) {
		super(fileName);
	}

	@Test
	public void testCapture() throws Exception {
		Corpus corpus = new Corpus(fileName, Tool.SQLITE_DESCR.fileRegex, Tool.SQLITE_DESCR);
		corpus.train();
		InputDocument testDoc = Tool.load(fileName, corpus.language);
		Formatter formatter = new Formatter(corpus);
		String output = formatter.format(testDoc, false);
		float editDistance = formatter.getEditDistance();
		System.out.println("edit distance "+editDistance);
		assertTrue("WS edit distance too high "+editDistance, editDistance < 0.05);
	}

	@Parameterized.Parameters(name="{0}")
	public static Collection<Object[]> findInputFiles() throws Exception {
		List<Object[]> args = new ArrayList<>();
		List<String> filenames = Tool.getFilenames(new File(CORPUS), Tool.SQLITE_DESCR.fileRegex);
		for (String fname : filenames) {
			args.add(new Object[] {fname});
		}
		return args;
	}
}
