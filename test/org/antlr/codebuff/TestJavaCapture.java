package org.antlr.codebuff;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.antlr.codebuff.Tool.levenshteinDistance;
import static org.junit.Assert.assertTrue;

/*
A useful measure would be "stability"; train a doc in isolation then format
that doc and measure similarity. Ideally, we'd see 0 difference but
around 0.01 would be ok not great (1%). 0.001 would be great!
We also want determinism where running format operation always gets same
result for same training corpus and test doc.
 */

@RunWith(Parameterized.class)
public class TestJavaCapture extends BaseTest {
	public static final String CORPUS1 = "corpus/java/training/stringtemplate4";
	public static final String CORPUS2 = "corpus/java/training/antlr4-tool";

	public TestJavaCapture(String fileName) {
		super(fileName);
	}

	@Test
	public void testCapture() throws Exception {
		Corpus corpus = new Corpus(fileName, Tool.JAVA_DESCR);
		corpus.train();
		InputDocument testDoc = Tool.parse(fileName, corpus.language);
		Formatter formatter = new Formatter(corpus);
		String output = formatter.format(testDoc, false);
//		System.out.println(output);
		float editDistance = levenshteinDistance(testDoc.content, output);
		System.out.println("edit distance "+editDistance);
		assertTrue("WS edit distance too high "+editDistance, editDistance < 0.05);
	}

	@Parameterized.Parameters(name="{0}")
	public static Collection<Object[]> findInputFiles() throws Exception {
		List<Object[]> args = new ArrayList<>();
		List<String> filenames = Tool.getFilenames(new File(CORPUS1), Tool.JAVA_DESCR.fileRegex);
		filenames.addAll(Tool.getFilenames(new File(CORPUS2), Tool.JAVA_DESCR.fileRegex));
		for (String fname : filenames) {
			args.add(new Object[] {fname});
		}
		return args;
	}
}
