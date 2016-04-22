package org.antlr.codebuff;

import org.antlr.codebuff.walkers.CollectTokenDependencies;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;
import java.util.Map;

public class TestTokenDependencies {
	public static void main(String[] args) throws Exception {
		if ( args[0].endsWith(".java") ) dumpJavaPairs(args[0]);
		else dumpANTLRPairs(args[0]);
	}

	public static void dumpJavaPairs(String fileName) throws Exception {
		Vocabulary vocab = JavaParser.VOCABULARY;

		InputDocument testDoc = Tool.load(fileName, 4);
		Tool.parse(testDoc, JavaLexer.class, JavaParser.class, "compilationUnit");
		CollectTokenDependencies listener = new CollectTokenDependencies(vocab, JavaParser.ruleNames);
		ParseTreeWalker.DEFAULT.walk(listener, testDoc.tree);
		Map<String, List<Pair<Integer, Integer>>> ruleToPairsBag = listener.getDependencies();

		for (String ruleName : ruleToPairsBag.keySet()) {
			List<Pair<Integer, Integer>> pairs = ruleToPairsBag.get(ruleName);
			System.out.print(ruleName+": ");
			for (Pair<Integer,Integer> p : pairs) {
				System.out.print(JavaParser.tokenNames[p.a]+","+JavaParser.tokenNames[p.b]+" ");
			}
			System.out.println();
		}
	}

	public static void dumpANTLRPairs(String fileName) throws Exception {
		Vocabulary vocab = ANTLRv4Parser.VOCABULARY;

		InputDocument testDoc = Tool.load(fileName, 4);
		Tool.parse(testDoc, ANTLRv4Lexer.class, ANTLRv4Parser.class, "grammarSpec");
		CollectTokenDependencies listener = new CollectTokenDependencies(vocab, ANTLRv4Parser.ruleNames);
		ParseTreeWalker.DEFAULT.walk(listener, testDoc.tree);
		Map<String, List<Pair<Integer, Integer>>> ruleToPairsBag = listener.getDependencies();

		for (String ruleName : ruleToPairsBag.keySet()) {
			List<Pair<Integer, Integer>> pairs = ruleToPairsBag.get(ruleName);
			System.out.print(ruleName+": ");
			for (Pair<Integer,Integer> p : pairs) {
				System.out.print(ANTLRv4Parser.tokenNames[p.a]+","+ANTLRv4Parser.tokenNames[p.b]+" ");
			}
			System.out.println();
		}
	}
}
