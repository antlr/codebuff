package org.antlr.codebuff;

import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;
import java.util.Map;

public class TestTokenDependencies {
	public static void main(String[] args) throws Exception {
		Vocabulary vocab = JavaParser.VOCABULARY;

		InputDocument testDoc = Tool.load(args[0], JavaLexer.class, 4);
		Tool.parse(testDoc, JavaLexer.class, JavaParser.class, "compilationUnit");
		CollectTokenDependencies listener = new CollectTokenDependencies(vocab);
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
}
