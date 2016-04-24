package org.antlr.codebuff;

import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenSource;

import java.util.Arrays;
import java.util.List;

public class InputDocument {
	public String fileName;
	public String content;
	public List<String> lines;
	public int index;
	public ParserRuleContext tree;

	public List<int[]> featureVectors;
	public List<Integer> injectWhitespace;
	public List<Integer> align;

	public Parser parser;
	public CodeBuffTokenStream tokens;

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

	public Class<? extends Parser> getParserClass() {
		return parser.getClass();
	}

	public Class<? extends TokenSource> getLexerClass() {
		return tokens.getTokenSource().getClass();
	}

	@Override
	public String toString() {
		return fileName+"["+content.length()+"]"+"@"+index;
	}
}

