package org.antlr.codebuff;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class ProfileJava {
	public static void main(String[] args) throws Exception {
		Thread.sleep(10000);
		ANTLRFileStream input = new ANTLRFileStream(args[0]);
		JavaLexer lexer = new JavaLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaParser parser = new JavaParser(tokens);
		JavaParser.CompilationUnitContext tree = parser.compilationUnit();
//		System.out.println(tree.toStringTree(parser));
		Thread.sleep(10000);
	}
}
