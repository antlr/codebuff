package org.antlr.codebuff;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Arrays;
import java.util.List;

public class InputDocument {
	public String fileName;
	public String content;
	public List<String> lines;
	public int index;
	public ParserRuleContext tree;
	public Parser parser;
	public CommonTokenStream tokens;
	public List<int[]> featureVectors;
	public List<Integer> injectNewlines;
	public List<Integer> injectWS;
	public List<Integer> alignWithPrevious;
	public int allWhiteSpaceCount = 0;
	public int incorrectWhiteSpaceCount = 0;
	public int misclassifiedNewLineCount = 0;
	public int misclassifiedWSCount = 0;
	public boolean dumpIncorrectWS = false;
	public boolean dumpVotes = false;

	public InputDocument(InputDocument d, int index) {
		this.fileName = d.fileName;
		this.content = d.content;
		this.index = index;
	}

	public InputDocument(String fileName, String content) {
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

