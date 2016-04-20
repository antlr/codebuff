package org.antlr.codebuff;

import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Arrays;
import java.util.List;

public class InputDocument {
	public Corpus corpus;
	public String fileName;
	public String content;
	public List<String> lines;
	public int index;
	public ParserRuleContext tree;
	public Parser parser;
	public CodeBuffTokenStream tokens;
	public List<int[]> featureVectors;
	public List<Integer> injectWhitespace;
	public List<Integer> align;
	public int allWhiteSpaceCount = 0;
	public int incorrectWhiteSpaceCount = 0;
	public int misclassifiedNewLineCount = 0;
	public int misclassifiedWSCount = 0;
	public boolean dumpIncorrectWS = false;
	public boolean dumpVotes = false;

	public InputDocument(Corpus corpus, String fileName, String content) {
		this.corpus = corpus;
		this.content = content;
		this.fileName = fileName;
	}

	public String getLine(int line) {
		if ( lines==null ) {
			lines = Arrays.asList(content.split("\n"));
		}
		if ( line>0 ) {
			return lines.get(line-1);
		}
		return null;
	}

	public double getIncorrectWSRate() {
		if (allWhiteSpaceCount == 0) {
			System.err.printf("File: %s's all white space count is zero\n", fileName);
			return -1.0;
		}
		return (double) incorrectWhiteSpaceCount / allWhiteSpaceCount;
	}

	@Override
	public String toString() {
		return fileName+"["+content.length()+"]"+"@"+index;
	}
}

