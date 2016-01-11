package org.antlr.groom;

import org.antlr.v4.runtime.tree.ParseTree;

public class InputDocument {
	public String fileName;
	public char[] content;
	public int index;
	public ParseTree tree;

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

