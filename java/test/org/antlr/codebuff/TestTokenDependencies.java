package org.antlr.codebuff;

import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.HashBag;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Try to identify word dependencies like { }, [], etc...
 *
 * Sample counts of rule nodes and unique token lists:

 80 block: ['{', '}']
197 blockStatement:
  3 classBody: ['{', '}']
 52 classBodyDeclaration:
  5 typeArguments: ['<', ',', '>']
  9 typeArguments: ['<', '>']
 14 qualifiedName: [Identifier, '.', Identifier, '.', Identifier]
  6 qualifiedName: [Identifier]
  1 typeDeclaration:
  8 typeSpec: ['[', ']']
173 typeSpec:
 40 parExpression: ['(', ')']
  1 classDeclaration: ['class', Identifier, 'extends']
  2 classDeclaration: ['class', Identifier]
  1 enumDeclaration: ['enum', Identifier, '{', '}']
  1 expression: ['!']
  9 expression: ['!=']
  2 expression: ['&&']
109 expression: ['(', ')']
  1 forControl: [';', ';']
  3 forControl:
...

 Repeated tokens should not be counted as most likely they are separators or terminators
 but not always. E.g., forControl: [';', ';']

 Build r->(a,b) tuples for every a,b in tokens list for r and unique a,b.
 E.g., typeSpec: ['[', ']']

 gives

 typeSpec -> ('[',']')

 For any a=b, assume it's not a dependency so build set r->{a} for each rule r.
 E.g., qualifiedName: [Identifier, '.', Identifier, '.', Identifier]

 gives

 qualifiedName -> (Id,.), (Id,Id), (.,Id)
 qualifiedName -> {Id,.}

 meaning we disregard all tuples for qualifiedName.

 With too few samples, we might get confused. E.g.,

 5 typeArguments: ['<', ',', '>']
 9 typeArguments: ['<', '>']

 gives

 typeArguments -> (<,','), (<,>), (',',>)

 Hmm..the count is 9+5 for (<,>), but just 5 for (<,',') and (',',>).
 If we pick just one tuple for each rule, we get (<,>) as winner. cool.
 By that argument, these would yield (class,Id) which is probably ok.

 1 classDeclaration: ['class', Identifier, 'extends']
 2 classDeclaration: ['class', Identifier]

 If there are multiple unique tokens every time like:

 enumDeclaration: ['enum', Identifier, '{', '}']

 we can't decide which token to pair with which. For now we can choose
 arbitrarily but later maybe choose first to last token.

 Oh! Actually, we should only consider tokens that are literals. Tokens
 like Id won't be that useful. That would give

 enumDeclaration: ['enum', '{', '}']

 which is easier.

 Can't really choose most frequent all the time. E.g., statement yields:

 statement: 11:'if','else' 11:'throw',';' 4:'for','(' 4:'for',')' 35:'return',';' 4:'(',')'

 and then would pick ('return',';') as the dependency. Ah. We need pairs
 not by rule but rule and which alternative. Otherwise rules with lots of
 alts will not be able to pick stuff out. Well, that info isn't available
 except for interpreted parsing. dang.
  */
public class TestTokenDependencies {
	public static class Foo extends JavaBaseListener {
		// ack. Map a rule name to a bag of (t1,t2) tuples that counts occurrences
		public Map<String,HashBag<Pair<Integer,Integer>>> ruleToPairsBag = new HashMap<>();
		public Map<String,Set<Integer>> ruleToDupTokensSet = new HashMap<>();

		@Override
		public void enterEveryRule(ParserRuleContext ctx) {
			String ruleName = JavaParser.ruleNames[ctx.getRuleIndex()];
			List<TerminalNode> tnodes = getDirectTerminalChildren(ctx);
			List<String> ttypes = BuffUtils.map(tnodes,
												t -> t.getSymbol().getType()>=0 ?
													JavaParser.tokenNames[t.getSymbol().getType()] :
													String.valueOf(t));
			Vocabulary vocab = JavaParser.VOCABULARY;
			// Find all ordered unique pairs; i.e., no (a,a) pairs
			for (int i=0; i<tnodes.size(); i++) {
				for (int j = i+1; j<tnodes.size(); j++) {
					TerminalNode t1 = tnodes.get(i);
					TerminalNode t2 = tnodes.get(j);
					int t1type = t1.getSymbol().getType();
					int t2type = t2.getSymbol().getType();
					if ( vocab.getLiteralName(t1type)==null || vocab.getLiteralName(t2type)==null ) {
						continue; // only include literals like '{' and ':' not IDENTIFIER etc...
					}

					if ( t1type==t2type ) {
						Set<Integer> dupTokensSet = ruleToDupTokensSet.get(ruleName);
						if ( dupTokensSet==null ) {
							dupTokensSet = new HashSet<>();
							ruleToDupTokensSet.put(ruleName, dupTokensSet);
						}
						dupTokensSet.add(t1type);
					}
					else {
						Pair<Integer, Integer> pair = new Pair<>(t1type, t2type);
						HashBag<Pair<Integer, Integer>> pairsBag = ruleToPairsBag.get(ruleName);
						if ( pairsBag==null ) {
							pairsBag = new HashBag<>();
							ruleToPairsBag.put(ruleName, pairsBag);
						}
						pairsBag.add(pair);
					}
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		InputDocument testDoc = Tool.load(args[0], JavaLexer.class, 4);
		Tool.parse(testDoc, JavaLexer.class, JavaParser.class, "compilationUnit");
		Foo listener = new Foo();
		ParseTreeWalker.DEFAULT.walk(listener, testDoc.tree);

		/* strip out tuples (a,b) with a or b in rule dup token set
		 E.g., before removing ',' dup, we have:
		 elementValueArrayInitializer: 4:'{',',' 1:'{','}' 4:',','}'
		 then after we get:
		 elementValueArrayInitializer: 1:'{','}'
		*/
		for (String ruleName : listener.ruleToPairsBag.keySet()) {
			Set<Integer> ruleDups = listener.ruleToDupTokensSet.get(ruleName);
			HashBag<Pair<Integer, Integer>> pairsBag = listener.ruleToPairsBag.get(ruleName);
			if ( ruleDups!=null ) {
				List<Pair<Integer,Integer>> toRemove = new ArrayList<>();
				for (Pair<Integer,Integer> p : pairsBag.keySet()) {
					if ( ruleDups.contains(p.a) || ruleDups.contains(p.b) ) {
						toRemove.add(p);
					}
				}
				toRemove.forEach(p -> pairsBag.remove(p));
			}
		}

		for (String ruleName : listener.ruleToPairsBag.keySet()) {
			HashBag<Pair<Integer, Integer>> pairsBag = listener.ruleToPairsBag.get(ruleName);
			Pair<Integer, Integer> mostFrequentPair = pairsBag.getMostFrequent();
			String t1name = JavaParser.tokenNames[mostFrequentPair.a];
			String t2name = JavaParser.tokenNames[mostFrequentPair.b];
//			System.out.println(ruleName+": "+t1name+" "+t2name);
			Set<Integer> ruleDups = listener.ruleToDupTokensSet.get(ruleName);
			System.out.print(ruleName+": ");
			for (Pair<Integer,Integer> p : pairsBag.keySet()) {
				System.out.print(pairsBag.get(p)+":"+JavaParser.tokenNames[p.a]+","+JavaParser.tokenNames[p.b]+" ");
			}
			System.out.println();
		}
	}

	public static List<TerminalNode> getDirectTerminalChildren(ParserRuleContext ctx) {
		if ( ctx.children==null ) {
			return Collections.emptyList();
		}

		List<TerminalNode> tokenNodes = new ArrayList<>();
		for (ParseTree o : ctx.children) {
			if ( o instanceof TerminalNode ) {
				TerminalNode tnode = (TerminalNode)o;
				tokenNodes.add(tnode);
			}
		}

		return tokenNodes;
	}
}
