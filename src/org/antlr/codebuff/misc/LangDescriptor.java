package org.antlr.codebuff.misc;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

public class LangDescriptor {
	public String name;
	public String fileRegex;
	public Class<? extends Lexer> lexerClass;
	public Class<? extends Parser> parserClass;
	public String startRuleName;
	public int tabSize;

	public LangDescriptor(String name,
	                      String fileRegex,
	                      Class<? extends Lexer> lexerClass,
	                      Class<? extends Parser> parserClass,
	                      String startRuleName,
	                      int tabSize)
	{
		this.name = name;
		this.fileRegex = fileRegex;
		this.lexerClass = lexerClass;
		this.parserClass = parserClass;
		this.startRuleName = startRuleName;
		this.tabSize = tabSize;
	}
}
