package org.antlr.codebuff;

import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenSource;

import java.util.Arrays;
import java.util.List;

public class InputDocument {
	public LangDescriptor language;
	public String fileName;
	public String content;
	public List<String> lines; // used for debugging; a cache of lines in this.content
	public int index;
	public ParserRuleContext tree;

	public Parser parser;
	public CodeBuffTokenStream tokens;

	public static InputDocument dup(InputDocument old) throws Exception {
		// reparse to get new tokens, tree
		return Tool.parse(old.fileName, old.content, old.language);
	}

	public InputDocument(String fileName, String content, LangDescriptor language) {
		this.content = content;
		this.fileName = fileName;
		this.language = language;
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

