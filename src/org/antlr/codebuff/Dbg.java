package org.antlr.codebuff;

import org.antlr.codebuff.gui.GUIController;
import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.codebuff.validation.ClassificationAnalysis;
import org.antlr.codebuff.validation.LeaveOneOutValidator;
import org.antlr.codebuff.validation.TokenPositionAnalysis;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;

import java.util.List;

import static org.antlr.codebuff.misc.BuffUtils.filter;

/** Grammar must have WS/comments on hidden channel
 *
 * Testing:
 *
 * Dbg  -antlr     corpus/antlr4/training      grammars/org/antlr/codebuff/tsql.g4
 * Dbg  -antlr     corpus/antlr4/training      corpus/antlr4/training/MASM.g4
 * Dbg  -quorum     corpus/quorum/training      corpus/quorum/training/Containers/List.quorum
 * Dbg  -sqlite    corpus/sqlclean/training      corpus/sqlclean/training/dmart_bits.sql
 * Dbg  -tsql      corpus/sqlclean/training        corpus/sqlclean/training/dmart_bits_PSQLRPT24.sql
 * Dbg  -java      corpus/java/training/stringtemplate4     src/org/antlr/codebuff/Tool.java
 * Dbg  -java_st      corpus/java/training/stringtemplate4/org/stringtemplate/v4/StringRenderer.java
 * Dbg  -java_guava   corpus/java/training/guava/base/Absent.java
 * Dbg  -java      corpus/java/training/antlr4-tool   corpus/java/training/stringtemplate4/org/stringtemplate/v4/AutoIndentWriter.java
 */
public class Dbg {


	public static void main(String[] args)
		throws Exception
	{
		if ( args.length<2 ) {
			System.err.println("Dbg [-leave-one-out] [-java|-java8|-antlr|-sqlite|-tsql] test-file");
		}

		int arg = 0;
		boolean leaveOneOut = true;
		boolean collectAnalysis = true;
		String language = args[arg++];
		language = language.substring(1);
		String testFilename = args[arg];
		String output = "???";
		InputDocument testDoc = null;
		GUIController controller;
		List<TokenPositionAnalysis> analysisPerToken = null;
		Pair<String, List<TokenPositionAnalysis>> results;
		LangDescriptor lang = null;
		long start = 0, stop = 0;
		for (int i = 0; i<Tool.languages.length; i++) {
			if ( Tool.languages[i].name.equals(language) ) {
				lang = Tool.languages[i];
				break;
			}
		}
		if ( lang!=null ) {
			start = System.nanoTime();
			LeaveOneOutValidator validator = new LeaveOneOutValidator(lang.corpusDir, lang);
			Triple<Formatter,Float,Float> val = validator.validateOneDocument(testFilename, null, collectAnalysis);
			testDoc = Tool.parse(testFilename, lang);
			stop = System.nanoTime();
			Formatter formatter = val.a;
			output = formatter.getOutput();
			System.out.println("output len = "+output.length());
			float editDistance = normalizedLevenshteinDistance(testDoc.content, output);
			System.out.println("normalized Levenshtein distance: "+editDistance);
			analysisPerToken = formatter.getAnalysisPerToken();

			CommonTokenStream original_tokens = Tool.tokenize(testDoc.content, lang.lexerClass);
			List<Token> wsTokens = filter(original_tokens.getTokens(),
			                              t -> t.getText().matches("\\s+"));
			String originalWS = tokenText(wsTokens);
			System.out.println("origin ws tokens len: "+originalWS.length());
			CommonTokenStream formatted_tokens = Tool.tokenize(output, lang.lexerClass);
			wsTokens = filter(formatted_tokens.getTokens(),
			                  t -> t.getText().matches("\\s+"));
			String formattedWS = tokenText(wsTokens);
			System.out.println("formatted ws tokens len: "+formattedWS.length());
			editDistance = levenshteinDistance(originalWS, formattedWS);
			editDistance /= Math.max(testDoc.content.length(), output.length());
			System.out.println("Levenshtein distance of ws normalized to output len: "+editDistance);

			ClassificationAnalysis analysis = new ClassificationAnalysis(testDoc, analysisPerToken);
			System.out.println(analysis);
		}

		if ( lang!=null ) {
			controller = new GUIController(analysisPerToken, testDoc, output, lang.lexerClass);
			controller.show();
//			System.out.println(output);
			System.out.printf("formatting time %ds\n", (stop-start)/1_000_000);
			System.out.printf("classify calls %d, hits %d rate %f\n",
			                  kNNClassifier.nClassifyCalls, kNNClassifier.nClassifyCacheHits,
			                  kNNClassifier.nClassifyCacheHits/(float) kNNClassifier.nClassifyCalls);
			System.out.printf("kNN calls %d, hits %d rate %f\n",
			                  kNNClassifier.nNNCalls, kNNClassifier.nNNCacheHits,
			                  kNNClassifier.nNNCacheHits/(float) kNNClassifier.nNNCalls);
		}
	}

	/** from https://en.wikipedia.org/wiki/Levenshtein_distance
	 *  "It is always at least the difference of the sizes of the two strings."
	 *  "It is at most the length of the longer string."
	 */
	public static float normalizedLevenshteinDistance(String s, String t) {
		float d = levenshteinDistance(s, t);
		int max = Math.max(s.length(), t.length());
		return d / (float)max;
	}

	public static float levenshteinDistance(String s, String t) {
	    // degenerate cases
	    if (s.equals(t)) return 0;
	    if (s.length() == 0) return t.length();
	    if (t.length() == 0) return s.length();

	    // create two work vectors of integer distances
	    int[] v0 = new int[t.length() + 1];
	    int[] v1 = new int[t.length() + 1];

	    // initialize v0 (the previous row of distances)
	    // this row is A[0][i]: edit distance for an empty s
	    // the distance is just the number of characters to delete from t
	    for (int i = 0; i < v0.length; i++) {
			v0[i] = i;
		}

	    for (int i = 0; i < s.length(); i++) {
	        // calculate v1 (current row distances) from the previous row v0

	        // first element of v1 is A[i+1][0]
	        //   edit distance is delete (i+1) chars from s to match empty t
	        v1[0] = i + 1;

	        // use formula to fill in the rest of the row
	        for (int j = 0; j < t.length(); j++)
	        {
	            int cost = s.charAt(i) == t.charAt(j) ? 0 : 1;
	            v1[j + 1] = Math.min(
								Math.min(v1[j] + 1, v0[j + 1] + 1),
								v0[j] + cost);
	        }

	        // copy v1 (current row) to v0 (previous row) for next iteration
			System.arraycopy(v1, 0, v0, 0, v0.length);
	    }

	    int d = v1[t.length()];
		return d;
	}

	/* Compare whitespace and give an approximate Levenshtein distance /
	   edit distance. MUCH faster to use this than pure Levenshtein which
	   must consider all of the "real" text that is in common.

		when only 1 kind of char, just substract lengths
		Orig    Altered Distance
		AB      A B     1
		AB      A  B    2
		AB      A   B   3
		A B     A  B    1

		A B     AB      1
		A  B    AB      2
		A   B   AB      3

		when ' ' and '\n', we count separately.

		A\nB    A B     spaces delta=1, newline delete=1, distance = 2
		A\nB    A  B    spaces delta=2, newline delete=1, distance = 3
		A\n\nB  A B     spaces delta=1, newline delete=2, distance = 3
		A\n \nB A B     spaces delta=0, newline delete=2, distance = 2
		A\n \nB A\nB    spaces delta=1, newline delete=1, distance = 2
		A \nB   A\n B   spaces delta=0, newline delete=0, distance = 0
						levenshtein would count this as 2 I think but
						for our doc distance, I think it's ok to measure as same
	 */
//	public static int editDistance(String s, String t) {
//	}

	/*
			A \nB   A\n B   spaces delta=0, newline delete=0, distance = 0
						levenshtein would count this as 2 I think but
						for our doc distance, I think it's ok to measure as same
	 */
	public static int whitespaceEditDistance(String s, String t) {
		int s_spaces = Tool.count(s, ' ');
		int s_nls = Tool.count(s, '\n');
		int t_spaces = Tool.count(t, ' ');
		int t_nls = Tool.count(t, '\n');
		return Math.abs(s_spaces - t_spaces) + Math.abs(s_nls - t_nls);
	}

	/** Compute a document difference metric 0-1.0 between two documents that
	 *  are identical other than (likely) the whitespace and comments.
	 *
	 *  1.0 means the docs are maximally different and 0 means docs are identical.
	 *
	 *  The Levenshtein distance between the docs counts only
	 *  whitespace diffs as the non-WS content is identical.
	 *  Levenshtein distance is bounded by 0..max(len(doc1),len(doc2)) so
	 *  we normalize the distance by dividing by max WS count.
	 *
	 *  TODO: can we simplify this to a simple walk with two
	 *  cursors through the original vs formatted counting
	 *  mismatched whitespace? real text are like anchors.
	 */
	public static double docDiff(String original,
	                             String formatted,
	                             Class<? extends Lexer> lexerClass)
		throws Exception
	{
		// Grammar must strip all but real tokens and whitespace (and put that on hidden channel)
		CodeBuffTokenStream original_tokens = Tool.tokenize(original, lexerClass);
//		String s = original_tokens.getText();
		CodeBuffTokenStream formatted_tokens = Tool.tokenize(formatted, lexerClass);
//		String t = formatted_tokens.getText();

		// walk token streams and examine whitespace in between tokens
		int i = -1;
		int ws_distance = 0;
		int original_ws = 0;
		int formatted_ws = 0;
		while ( true ) {
			Token ot = original_tokens.LT(i); // TODO: FIX THIS! can't use LT()
			if ( ot==null || ot.getType()==Token.EOF ) break;
			List<Token> ows = original_tokens.getHiddenTokensToLeft(ot.getTokenIndex());
			original_ws += tokenText(ows).length();

			Token ft = formatted_tokens.LT(i); // TODO: FIX THIS! can't use LT()
			if ( ft==null || ft.getType()==Token.EOF ) break;
			List<Token> fws = formatted_tokens.getHiddenTokensToLeft(ft.getTokenIndex());
			formatted_ws += tokenText(fws).length();

			ws_distance += whitespaceEditDistance(tokenText(ows), tokenText(fws));
			i++;
		}
		// it's probably ok to ignore ws diffs after last real token

		int max_ws = Math.max(original_ws, formatted_ws);
		double normalized_ws_distance = ((float) ws_distance)/max_ws;
		return normalized_ws_distance;
	}

	/** Compare an input document's original text with its formatted output
	 *  and return the ratio of the incorrectWhiteSpaceCount to total whitespace
	 *  count in the original document text. It is a measure of document
	 *  similarity.
	 */
//	public static double compare(InputDocument doc,
//	                             String formatted,
//	                             Class<? extends Lexer> lexerClass)
//		throws Exception {
//	}

	public static String tokenText(List<Token> tokens) {
		return tokenText(tokens, null);
	}

	public static String tokenText(List<Token> tokens, String separator) {
		if ( tokens==null ) return "";
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (Token t : tokens) {
			if ( separator!=null && !first ) {
				buf.append(separator);
			}
			buf.append(t.getText());
			first = false;
		}
		return buf.toString();
	}

	public static void printOriginalFilePiece(InputDocument doc, CommonToken originalCurToken) {
		System.out.println(doc.getLine(originalCurToken.getLine()-1));
		System.out.println(doc.getLine(originalCurToken.getLine()));
		System.out.print(Tool.spaces(originalCurToken.getCharPositionInLine()));
		System.out.println("^");
	}

	public static class Foo {
		public static void main(String[] args) throws Exception {
			ANTLRv4Lexer lexer = new ANTLRv4Lexer(new ANTLRFileStream("grammars/org/antlr/codebuff/ANTLRv4Lexer.g4"));
			CommonTokenStream tokens = new CodeBuffTokenStream(lexer);
			ANTLRv4Parser parser = new ANTLRv4Parser(tokens);
			ANTLRv4Parser.GrammarSpecContext tree = parser.grammarSpec();
			System.out.println(tree.toStringTree(parser));
		}
	}
}
