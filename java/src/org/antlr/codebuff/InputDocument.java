package org.antlr.codebuff;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Arrays;
import java.util.List;

public class InputDocument {
	public String fileName;
	public String content;
	public List<String> lines;
	public int index;
	public ParserRuleContext tree;
	public CommonTokenStream tokens;
	public List<int[]> featureVectors;
	public List<Integer> injectNewlines;
	public List<Integer> injectWS;
	public List<Integer> indent;
	public List<Integer> levelsToCommonAncestor;

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
		return lines.get(line-1);
	}

	@Override
	public String toString() {
		return fileName+"["+content.length()+"]"+"@"+index;
	}
}

