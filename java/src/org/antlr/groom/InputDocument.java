package org.antlr.groom;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class InputDocument {
	public String fileName;
	public char[] content;
	public int index;
	public ParserRuleContext tree;
	public CommonTokenStream tokens;
	public List<int[]> features;
	public List<Integer> injectNewlines;

	public InputDocument(InputDocument d, int index) {
		this.fileName = d.fileName;
		this.content = d.content;
		this.index = index;
	}

	public InputDocument(String fileName, char[] content) {
		this.content = content;
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return fileName+"["+content.length+"]"+"@"+index;
	}
}

