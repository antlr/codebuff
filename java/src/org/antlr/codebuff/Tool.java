package org.antlr.codebuff;

import org.antlr.codebuff.gui.GUIController;
import org.antlr.codebuff.misc.Quad;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.antlr.codebuff.CollectFeatures.ANALYSIS_START_TOKEN_INDEX;

/** Ok, changed requirements. Grammar must have WS on hidden channel and comments on non-HIDDEN channel
 *
 * Testing:
 *
 * Tool  -antlr     ../corpus/antlr4/training/Java.g4      grammars/org/antlr/codebuff/SQLite.g4
 * Tool  -sqlite    ../corpus/sqlite/training      ../corpus/sqlite/testing/t1.sql
 * Tool  -tsql      ../corpus/tsql/training        ../corpus/tsql/testing/select1.sql
 * Tool  -plsql     ../corpus/plsql/training       ../corpus/plsql/testing/condition15.sql
 * Tool  -java      ../samples/stringtemplate4     src/org/antlr/codebuff/Tool.java
 * Tool  -java      ../corpus/java/training/stringtemplate4     ../corpus/java/training/stringtemplate4/org/stringtemplate/v4/AutoIndentWriter.java
 */
public class Tool {
	public static boolean showFileNames = false;
	public static boolean showTokens = false;

	public static void main(String[] args)
		throws Exception
	{
		if ( args.length<2 ) {
			System.err.println("ExtractFeatures [-java|-antlr|-sqlite|-tsql|-plsql] root-dir-of-samples test-file");
		}
		int tabSize = 4; // TODO: MAKE AN ARGUMENT
		String language = args[0];
		String corpusDir = args[1];
		String testFilename = args[2];
		String output = "???";
		Corpus corpus;
		InputDocument testDoc;
		GUIController controller;
		List<TokenPositionAnalysis> analysisPerToken;
		Pair<String, List<TokenPositionAnalysis>> results;
		switch ( language ) {
			case "-java":
				corpus = train(corpusDir, ".*\\.java", JavaLexer.class, JavaParser.class, "compilationUnit", tabSize, true);
				testDoc = load(testFilename, JavaLexer.class, tabSize);
				results = format(corpus, testDoc, JavaLexer.class, JavaParser.class, "compilationUnit", tabSize);
				output = results.a;
				analysisPerToken = results.b;
				controller = new GUIController(analysisPerToken, testDoc, output, JavaLexer.class);
				controller.show();
				break;
			case "-antlr":
				corpus = train(corpusDir, ".*\\.g4", ANTLRv4Lexer.class, ANTLRv4Parser.class, "grammarSpec", tabSize, true);
				testDoc = load(testFilename, ANTLRv4Lexer.class, tabSize);
				results = format(corpus, testDoc, ANTLRv4Lexer.class, ANTLRv4Parser.class, "grammarSpec", tabSize);
				output = results.a;
				analysisPerToken = results.b;
				controller = new GUIController(analysisPerToken, testDoc, output, ANTLRv4Lexer.class);
				controller.show();
				break;
			case "-sqlite":
				corpus = train(corpusDir, ".*\\.sql", SQLiteLexer.class, SQLiteParser.class, "parse", tabSize, true);
				testDoc = load(testFilename, SQLiteLexer.class, tabSize);
				results = format(corpus, testDoc, SQLiteLexer.class, SQLiteParser.class, "parse", tabSize);
				output = results.a;
				analysisPerToken = results.b;
				controller = new GUIController(analysisPerToken, testDoc, output, SQLiteLexer.class);
				controller.show();
				break;
			case "-tsql":
				corpus = train(corpusDir, ".*\\.sql", tsqlLexer.class, tsqlParser.class, "tsql_file", tabSize, true);
				testDoc = load(testFilename, tsqlLexer.class, tabSize);
				results = format(corpus, testDoc, tsqlLexer.class, tsqlParser.class, "tsql_file", tabSize);
				output = results.a;
				analysisPerToken = results.b;
				controller = new GUIController(analysisPerToken, testDoc, output, tsqlLexer.class);
				controller.show();
				break;
			case "-plsql":
				corpus = train(corpusDir, ".*\\.sql", plsqlLexer.class, plsqlParser.class, "compilation_unit", tabSize, true);
				testDoc = load(testFilename, plsqlLexer.class, tabSize);
				results = format(corpus, testDoc, plsqlLexer.class, plsqlParser.class, "compilation_unit", tabSize);
				output = results.a;
				analysisPerToken = results.b;
				controller = new GUIController(analysisPerToken, testDoc, output, plsqlLexer.class);
				controller.show();
				break;
		}
		System.out.println(output);
	}

	/** Given a corpus, format the document by tokenizing and using the
	 *  corpus to locate newline and whitespace injection points.
	 */
	public static Pair<String,List<TokenPositionAnalysis>> format(Corpus corpus, InputDocument testDoc,
	                                                              Class<? extends Lexer> lexerClass,
	                                                              Class<? extends Parser> parserClass,
	                                                              String startRuleName,
	                                                              int tabSize)
		throws Exception
	{
		return format(corpus, testDoc, lexerClass, parserClass, startRuleName, tabSize, true);
	}

	public static Pair<String,List<TokenPositionAnalysis>> format(Corpus corpus,
	                                                              InputDocument testDoc,
	                                                              Class<? extends Lexer> lexerClass,
	                                                              Class<? extends Parser> parserClass,
	                                                              String startRuleName,
	                                                              int tabSize,
	                                                              boolean showFormattedResult)
		throws Exception
	{
		testDoc.corpus = corpus;
		parse(testDoc, lexerClass, parserClass, startRuleName);
		Formatter formatter = new Formatter(corpus, testDoc, tabSize);
		String formattedOutput = formatter.format();
		List<TokenPositionAnalysis> analysisPerToken = formatter.getAnalysisPerToken();
		testDoc.dumpIncorrectWS = false;
		Tool.compare(testDoc, formattedOutput, lexerClass);
		if (showFormattedResult) System.out.printf("\n\nIncorrect_WS / All_WS: %d / %d = %3.1f%%\n", testDoc.incorrectWhiteSpaceCount, testDoc.allWhiteSpaceCount, 100*testDoc.getIncorrectWSRate());
		if (showFormattedResult) System.out.println("misclassified: "+formatter.misclassified_NL);
		double d = Tool.docDiff(testDoc.content, formattedOutput, lexerClass);
		if (showFormattedResult) System.out.println("Diff is "+d);

		return new Pair<>(formattedOutput, analysisPerToken);
	}

	public static Corpus train(String rootDir,
	                           String fileRegex,
							   Class<? extends Lexer> lexerClass,
							   Class<? extends Parser> parserClass,
							   String startRuleName,
							   int tabSize,
	                           boolean shuffleFeatureVectors)
		throws Exception
	{
		List<String> allFiles = getFilenames(new File(rootDir), fileRegex);
		List<InputDocument> documents = load(allFiles, lexerClass, tabSize);

		// Parse all documents into parse trees before training begins
		for (InputDocument doc : documents) {
			if ( showFileNames ) System.out.println(doc);
			parse(doc, lexerClass, parserClass, startRuleName);
		}

		// Walk all documents to compute matching token dependencies (we need this for feature computation)
		// While we're at it, find sibling lists
		Vocabulary vocab = getLexer(lexerClass, null).getVocabulary();
		String[] ruleNames = getParser(parserClass, null).getRuleNames();
		CollectTokenDependencies collectTokenDependencies = new CollectTokenDependencies(vocab, ruleNames);
		CollectSiblingLists collectSiblingLists = new CollectSiblingLists();
		for (InputDocument doc : documents) {
			ParseTreeWalker.DEFAULT.walk(collectTokenDependencies, doc.tree);
			ParseTreeWalker.DEFAULT.walk(collectSiblingLists, doc.tree);
		}
		Map<String, List<Pair<Integer, Integer>>> ruleToPairsBag = collectTokenDependencies.getDependencies();
		Set<Quad<Integer,Integer,Integer,Integer>> rootAndChildListPairs = collectSiblingLists.getRootAndChildListPairs();

		if ( false ) {
			for (String ruleName : ruleToPairsBag.keySet()) {
				List<Pair<Integer, Integer>> pairs = ruleToPairsBag.get(ruleName);
				System.out.print(ruleName+": ");
				for (Pair<Integer, Integer> p : pairs) {
					System.out.print(vocab.getDisplayName(p.a)+","+vocab.getDisplayName(p.b)+" ");
				}
				System.out.println();
			}
		}

		if ( false ) {
			for (Quad<Integer,Integer,Integer,Integer> siblingPairs : rootAndChildListPairs) {
				String parent = ruleNames[siblingPairs.a];
				parent = parent.replace("Context","");
				String siblingListName = ruleNames[siblingPairs.c];
				siblingListName = siblingListName.replace("Context","");
				System.out.println(parent+":"+siblingPairs.b+"->"+siblingListName+":"+siblingPairs.d);
			}
		}

		Corpus corpus = processSampleDocs(documents, tabSize, ruleToPairsBag, rootAndChildListPairs);
		if ( shuffleFeatureVectors ) corpus.randomShuffleInPlace();
		corpus.buildTokenContextIndex();
		return corpus;
	}

	public static Corpus processSampleDocs(List<InputDocument> docs,
										   int tabSize,
										   Map<String, List<Pair<Integer, Integer>>> ruleToPairsBag,
										   Set<Quad<Integer,Integer,Integer,Integer>> rootAndChildListPairs)
		throws Exception
	{
		List<InputDocument> documents = new ArrayList<>();
		List<int[]> featureVectors = new ArrayList<>();
		List<Integer> injectNewlines = new ArrayList<>();
		List<Integer> alignWithPrevious = new ArrayList<>();
		Corpus corpus = new Corpus(documents, featureVectors, injectNewlines, alignWithPrevious);
		corpus.ruleToPairsBag = ruleToPairsBag;
		corpus.rootAndChildListPairs = rootAndChildListPairs;

		for (InputDocument doc : docs) {
			if ( showFileNames ) System.out.println(doc);
			doc.corpus = corpus; // we know the corpus object now
			process(doc, tabSize);

			for (int i=0; i<doc.featureVectors.size(); i++) {
				documents.add(doc);
				int[] featureVec = doc.featureVectors.get(i);
				injectNewlines.add(doc.injectWhitespace.get(i));
				alignWithPrevious.add(doc.align.get(i));
				featureVectors.add(featureVec);
			}
		}
		System.out.printf("%d feature vectors\n", featureVectors.size());
		return corpus;
	}

	/** Parse document, save feature vectors to the doc but return it also */
	public static void process(InputDocument doc, int tabSize) {
		CollectFeatures collector = new CollectFeatures(doc, tabSize);
		collector.computeFeatureVectors();

		doc.featureVectors = collector.getFeatures();
		doc.injectWhitespace = collector.getInjectWhitespace();
		doc.align = collector.getAlign();
	}

	public static CommonTokenStream tokenize(String doc, Class<? extends Lexer> lexerClass)
		throws Exception
	{
		ANTLRInputStream input = new ANTLRInputStream(doc);
		Lexer lexer = getLexer(lexerClass, input);

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();
		return tokens;
	}

	/** Parse doc and fill tree and tokens fields */
	public static void parse(InputDocument doc,
							 Class<? extends Lexer> lexerClass,
							 Class<? extends Parser> parserClass,
							 String startRuleName)
		throws Exception
	{
		ANTLRInputStream input = new ANTLRInputStream(doc.content);
		Lexer lexer = getLexer(lexerClass, input);
		input.name = doc.fileName;

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		if ( showTokens ) {
			tokens.fill();
			for (Object tok : tokens.getTokens()) {
				System.out.println(tok);
			}
		}

		doc.parser = getParser(parserClass, tokens);
		doc.parser.setBuildParseTree(true);
		Method startRule = parserClass.getMethod(startRuleName);
		ParserRuleContext tree = (ParserRuleContext)startRule.invoke(doc.parser, (Object[]) null);

		doc.tokens = tokens;
		doc.tree = tree;
	}

	public static Parser getParser(Class<? extends Parser> parserClass, CommonTokenStream tokens) throws NoSuchMethodException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
		Constructor<? extends Parser> parserCtor =
			parserClass.getConstructor(TokenStream.class);
		return parserCtor.newInstance(tokens);
	}

	public static Lexer getLexer(Class<? extends Lexer> lexerClass, ANTLRInputStream input) throws NoSuchMethodException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
		Constructor<? extends Lexer> lexerCtor =
			lexerClass.getConstructor(CharStream.class);
		return lexerCtor.newInstance(input);
	}

	/** Get all file contents into input array */
	public static List<InputDocument> load(List<String> fileNames,
										   Class<? extends Lexer> lexerClass,
										   int tabSize)
		throws Exception
	{
		List<InputDocument> input = new ArrayList<>(fileNames.size());
		int i = 0;
		for (String f : fileNames) {
			InputDocument doc = load(f, lexerClass, tabSize);
			doc.index = i++;
			input.add(doc);
		}
		System.out.println(input.size()+" files");
		return input;
	}

	public static InputDocument load(String fileName,
									 Class<? extends Lexer> lexerClass,
									 int tabSize)
		throws Exception
	{
		Path path = FileSystems.getDefault().getPath(fileName);
		byte[] filearray = Files.readAllBytes(path);
		String content = new String(filearray);
		String notabs = expandTabs(content, tabSize);
		CommonTokenStream tokens = tokenize(notabs, lexerClass);
		// delete any whitespace on a line by itself, including the newline
		// most likely left over from a comment skipped by lexer
		StringBuilder buf = new StringBuilder();
		int i=0;
		while ( i<tokens.size()-1 ) {
			Token t = tokens.get(i);
			buf.append(t.getText());
			// if we see whitespace followed by whitespace, it must have been
			// split up by a comment or other skipped token. Assume we want to
			// delete the 2nd one.
			// "\n    " then "   " should become "\n    "
			// "\n\n    " then "   " should become "\n\n    "
			if ( t.getText().matches("\n+ +") ) {
				Token next = tokens.get(i+1);
				if ( next.getText().matches("\n +") ) {
					// delete by bumping i so we don't see next in next iteration
					i++;
				}
			}
			i++;
		}

		return new InputDocument(null, fileName, buf.toString());
	}

	public static List<String> getFilenames(File f, String inputFilePattern) throws Exception {
		List<String> files = new ArrayList<>();
		getFilenames_(f, inputFilePattern, files);
		return files;
	}

	public static void getFilenames_(File f, String inputFilePattern, List<String> files) {
		// If this is a directory, walk each file/dir in that directory
		if (f.isDirectory()) {
			String flist[] = f.list();
			for (String aFlist : flist) {
				getFilenames_(new File(f, aFlist), inputFilePattern, files);
			}
		}

		// otherwise, if this is an input file, load it!
		else if ( inputFilePattern==null || f.getName().matches(inputFilePattern) ) {
		  	files.add(f.getAbsolutePath());
		}
	}

	public static String join(int[] array, String separator) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			builder.append(array[i]);
			if (i < array.length - 1) {
				builder.append(separator);
			}
		}

		return builder.toString();
	}

	public static String join(String[] array, String separator) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			builder.append(array[i]);
			if (i < array.length - 1) {
				builder.append(separator);
			}
		}

		return builder.toString();
	}

	public static void wipeLineAndPositionInfo(CommonTokenStream tokens) {
		tokens.fill();
		for (int i = ANALYSIS_START_TOKEN_INDEX; i<tokens.size(); i++) { // can't process first 1 token so leave it alone
			CommonToken t = (CommonToken)tokens.get(i);
			t.setLine(0);
			t.setCharPositionInLine(-1);
		}
	}

	public static List<CommonToken> copy(CommonTokenStream tokens) {
		List<CommonToken> copy = new ArrayList<>();
		tokens.fill();
		for (Token t : tokens.getTokens()) {
			copy.add(new CommonToken(t));
		}
		return copy;
	}

	public static int L0_Distance(boolean[] categorical, int[] A, int[] B) {
		int count = 0; // count how many mismatched categories there are
		for (int i=0; i<A.length; i++) {
			if ( categorical[i] ) {
				if ( A[i] != B[i] ) {
					count++;
				}
			}
		}
		return count;
	}

	/** A distance of 0 should count much more than non-0. Also, penalize
	 *  mismatches closer to current token than those farther away.
	 */
	public static double weightedL0_Distance(FeatureMetaData[] featureTypes, int[] A, int[] B) {
		double count = 0; // count how many mismatched categories there are
		for (int i=0; i<A.length; i++) {
			if ( featureTypes[i].type==FeatureType.TOKEN ||
				featureTypes[i].type==FeatureType.RULE  ||
				featureTypes[i].type==FeatureType.INT  ||
				featureTypes[i].type==FeatureType.BOOL)
			{
				if ( A[i] != B[i] ) {
					count += featureTypes[i].mismatchCost;
				}
			}
		}
		return count;
	}

	public static double sigmoid(int x, float center) {
		return 1.0 / (1.0 + Math.exp(-0.2*(x-center)));
	}

	public static int max(List<Integer> Y) {
		int max = 0;
		for (int y : Y) max = Math.max(max, y);
		return max;
	}

	public static int sum(int[] a) {
		int s = 0;
		for (int x : a) s += x;
		return s;
	}

	// from https://en.wikipedia.org/wiki/Levenshtein_distance
	public static int levenshteinDistance(String s, String t) {
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

	    return v1[t.length()];
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
		int s_spaces = count(s, ' ');
		int s_nls = count(s, '\n');
		int t_spaces = count(t, ' ');
		int t_nls = count(t, '\n');
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
		CommonTokenStream original_tokens = tokenize(original, lexerClass);
//		String s = original_tokens.getText();
		CommonTokenStream formatted_tokens = tokenize(formatted, lexerClass);
//		String t = formatted_tokens.getText();

		// walk token streams and examine whitespace in between tokens
		int i = 1;
		int ws_distance = 0;
		int original_ws = 0;
		int formatted_ws = 0;
		while ( true ) {
			Token ot = original_tokens.LT(i);
			if ( ot==null || ot.getType()==Token.EOF ) break;
			List<Token> ows = original_tokens.getHiddenTokensToLeft(ot.getTokenIndex());
			original_ws += tokenText(ows).length();

			Token ft = formatted_tokens.LT(i);
			if ( ft==null || ft.getType()==Token.EOF ) break;
			List<Token> fws = formatted_tokens.getHiddenTokensToLeft(ft.getTokenIndex());
			formatted_ws += tokenText(fws).length();

			ws_distance += whitespaceEditDistance(tokenText(ows), tokenText(fws));
			i++;
		}
		// it's probably ok to ignore ws diffs after last real token

//		int non_ws = 0;
//		for (Token tok : original_tokens.getTokens()) {
//			if ( tok.getType()!=Token.EOF && tok.getChannel()==Lexer.DEFAULT_TOKEN_CHANNEL ) {
//				non_ws += tok.getText().length();
//			}
//		}
//		String original_text_with_ws = original_tokens.getText();
//		int original_ws = original_text_with_ws.length() - non_ws;
//		int formatted_ws = formatted.length() - non_ws;
//		int ws_distance = Tool.levenshteinDistance(original_text_with_ws, formatted);
		int max_ws = Math.max(original_ws, formatted_ws);
		double normalized_ws_distance = ((float) ws_distance)/max_ws;
		return normalized_ws_distance;
	}

	/** Compare an input document's original text with its formatted output
	 *  and return the ratio of the incorrectWhiteSpaceCount to total whitespace
	 *  count in the original document text. It is a measure of document
	 *  similarity.
	 */
	public static double compare(InputDocument doc,
	                             String formatted,
	                             Class<? extends Lexer> lexerClass)
		throws Exception
	{
		doc.allWhiteSpaceCount = 0;
		doc.incorrectWhiteSpaceCount = 0;

		String original = doc.content;

		// Grammar must strip all but real tokens and whitespace (and put that on hidden channel)
		CommonTokenStream original_tokens = tokenize(original, lexerClass);
		CommonTokenStream formatted_tokens = tokenize(formatted, lexerClass);

		// walk token streams and examine whitespace in between tokens
		int i = 1;

		while ( true ) {
			Token ot = original_tokens.LT(i);
			if ( ot==null || ot.getType()==Token.EOF ) break;
			List<Token> ows = original_tokens.getHiddenTokensToLeft(ot.getTokenIndex());
			String original_ws = tokenText(ows);

			Token ft = formatted_tokens.LT(i);
			if ( ft==null || ft.getType()==Token.EOF ) break;
			List<Token> fws = formatted_tokens.getHiddenTokensToLeft(ft.getTokenIndex());
			String formatted_ws = tokenText(fws);

			if (original_ws.length() == 0) {
				if (formatted_ws.length() != 0) {
					doc.incorrectWhiteSpaceCount++;

					if (doc.dumpIncorrectWS) {
						System.out.printf("\n*** Extra WS - line %d:\n", ot.getLine());
						Tool.printOriginalFilePiece(doc, (CommonToken)ot);
						System.out.println("actual: " + Tool.dumpWhiteSpace(formatted_ws));
					}
				}
			}
			else {
				doc.allWhiteSpaceCount++;

				if (formatted_ws.length() == 0) {
					doc.incorrectWhiteSpaceCount++;

					if (doc.dumpIncorrectWS) {
						System.out.printf("\n*** Miss a WS - line %d:\n", ot.getLine());
						Tool.printOriginalFilePiece(doc, (CommonToken) ot);
						System.out.println("should: " + Tool.dumpWhiteSpace(original_ws));
					}
				}
				else if (!TwoWSEqual(original_ws, formatted_ws)) {
					doc.incorrectWhiteSpaceCount++;

					if (doc.dumpIncorrectWS) {
						System.out.printf("\n*** Incorrect WS - line %d:\n", ot.getLine());
						Tool.printOriginalFilePiece(doc, (CommonToken)ot);
						System.out.println("should: " + Tool.dumpWhiteSpace(original_ws));
						System.out.println("actual: " + Tool.dumpWhiteSpace(formatted_ws));
					}
				}
			}

			i++;
		}
		return ((double)doc.incorrectWhiteSpaceCount) / doc.allWhiteSpaceCount;
	}


	// it's a compare function but only focus on NL
	// basically this function is copy and paste from compare function on above
	public static double compareNL(InputDocument doc,
								 String formatted,
								 Class<? extends Lexer> lexerClass)
		throws Exception
	{
		doc.allWhiteSpaceCount = 0;
		doc.incorrectWhiteSpaceCount = 0;

		String original = doc.content;

		// Grammar must strip all but real tokens and whitespace (and put that on hidden channel)
		CommonTokenStream original_tokens = tokenize(original, lexerClass);
		CommonTokenStream formatted_tokens = tokenize(formatted, lexerClass);

		// walk token streams and examine whitespace in between tokens
		int i = 1;

		while ( true ) {
			Token ot = original_tokens.LT(i);
			if ( ot==null || ot.getType()==Token.EOF ) break;
			List<Token> ows = original_tokens.getHiddenTokensToLeft(ot.getTokenIndex());
			String original_ws = tokenText(ows);

			Token ft = formatted_tokens.LT(i);
			if ( ft==null || ft.getType()==Token.EOF ) break;
			List<Token> fws = formatted_tokens.getHiddenTokensToLeft(ft.getTokenIndex());
			String formatted_ws = tokenText(fws);

			if (original_ws.length() == 0) {
				if (formatted_ws.length() != 0) {
					if (count(formatted_ws, '\n') > 0) {
						doc.incorrectWhiteSpaceCount++;

						if (doc.dumpIncorrectWS) {
							System.out.printf("\n*** Extra WS - line %d:\n", ot.getLine());
							Tool.printOriginalFilePiece(doc, (CommonToken)ot);
							System.out.println("actual: " + Tool.dumpWhiteSpace(formatted_ws));
						}
					}
				}
			}
			else {
				if (count(original_ws, '\n') > 0) {
					doc.allWhiteSpaceCount++;

					if (formatted_ws.length() == 0) {
						doc.incorrectWhiteSpaceCount++;

						if (doc.dumpIncorrectWS) {
							System.out.printf("\n*** Miss a WS - line %d:\n", ot.getLine());
							Tool.printOriginalFilePiece(doc, (CommonToken) ot);
							System.out.println("should: " + Tool.dumpWhiteSpace(original_ws));
						}
					}
					else if (count(original_ws, '\n') != count(formatted_ws, '\n')) {
						doc.incorrectWhiteSpaceCount++;

						if (doc.dumpIncorrectWS) {
							System.out.printf("\n*** Incorrect WS - line %d:\n", ot.getLine());
							Tool.printOriginalFilePiece(doc, (CommonToken)ot);
							System.out.println("should: " + Tool.dumpWhiteSpace(original_ws));
							System.out.println("actual: " + Tool.dumpWhiteSpace(formatted_ws));
						}
					}
				}
			}

			i++;
		}
		return ((double)doc.incorrectWhiteSpaceCount) / doc.allWhiteSpaceCount;
	}

	public static String tokenText(List<Token> tokens) {
		if ( tokens==null ) return "";
		StringBuilder buf = new StringBuilder();
		for (Token t : tokens) {
			buf.append(t.getText());
		}
		return buf.toString();
	}

	public static int getNumberRealTokens(CommonTokenStream tokens, int from, int to) {
		if ( tokens==null ) return 0;
		int n = 0;
		if ( from<0 ) from = 0;
		if ( to>tokens.size() ) to = tokens.size()-1;
		for (int i = from; i <= to; i++) {
			Token t = tokens.get(i);
			if ( t.getChannel()==Token.DEFAULT_CHANNEL ) {
				n++;
			}
		}
		return n;
	}

	public static String spaces(int n) {
		return sequence(n, " ");
//		StringBuilder buf = new StringBuilder();
//		for (int sp=1; sp<=n; sp++) buf.append(" ");
//		return buf.toString();
	}

	public static String newlines(int n) {
		return sequence(n, "\n");
//		StringBuilder buf = new StringBuilder();
//		for (int sp=1; sp<=n; sp++) buf.append("\n");
//		return buf.toString();
	}

	public static String sequence(int n, String s) {
		StringBuilder buf = new StringBuilder();
		for (int sp=1; sp<=n; sp++) buf.append(s);
		return buf.toString();
	}

	public static int count(String s, char x) {
		int n = 0;
		for (int i = 0; i<s.length(); i++) {
			if ( s.charAt(i)==x ) {
				n++;
			}
		}
		return n;
	}

	public static String expandTabs(String s, int tabSize) {
		if ( s==null ) return null;
		StringBuilder buf = new StringBuilder();
		int col = 0;
		for (int i = 0; i<s.length(); i++) {
			char c = s.charAt(i);
			switch ( c ) {
				case '\n' :
					col = 0;
					buf.append(c);
					break;
				case '\t' :
					buf.append(spaces(tabSize - col % tabSize));
					break;
				default :
					col++;
					buf.append(c);
					break;
			}
		}
		return buf.toString();
	}

	public static String dumpWhiteSpace(String s) {
		String[] whiteSpaces = new String[s.length()];
		for (int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			switch ( c ) {
				case '\n' :
					whiteSpaces[i] = "\\n";
					break;
				case '\t' :
					whiteSpaces[i] = "\\t";
					break;
				case '\r' :
					whiteSpaces[i] = "\\r";
					break;
				case '\u000C' :
					whiteSpaces[i] = "\\u000C";
					break;
				case ' ' :
					whiteSpaces[i] = "ws";
					break;
				default :
					whiteSpaces[i] = String.valueOf(c);
					break;
			}
		}
		return join(whiteSpaces, " | ");
	}

	// In some case, before a new line sign, there maybe some white space.
	// But those white spaces won't change the look of file.
	// To compare if two WS are the same, we should remove all the shite space before the first '\n'
	public static boolean TwoWSEqual(String a, String b) {
		String newA = a;
		String newB = b;

		int aStartNLIndex = a.indexOf('\n');
		int bStartNLIndex = b.indexOf('\n');

		if (aStartNLIndex > 0) newA = a.substring(aStartNLIndex);
		if (bStartNLIndex > 0) newB = b.substring(bStartNLIndex);

		return newA.equals(newB);
	}

	public static void printOriginalFilePiece(InputDocument doc, CommonToken originalCurToken) {
		System.out.println(doc.getLine(originalCurToken.getLine()-1));
		System.out.println(doc.getLine(originalCurToken.getLine()));
		System.out.print(Tool.spaces(originalCurToken.getCharPositionInLine()));
		System.out.println("^");
	}


	/** Given a corpus, format the given input documents and compute their document
	 *  similarities with {@link #compare}.
	 */
	public static ArrayList<Double> validateResults(Corpus corpus, List<InputDocument> testDocs,
	                                                Class<? extends Lexer> lexerClass,
	                                                Class<? extends Parser> parserClass,
	                                                String startRuleName,
	                                                int tabSize)
		throws Exception
	{
		ArrayList<Double> differenceRatios = new ArrayList<>();

		for (InputDocument testDoc: testDocs) {
			Pair<String, List<TokenPositionAnalysis>> results =
				format(corpus, testDoc, lexerClass, parserClass, startRuleName, tabSize, false);
			String formattedDoc = results.a;
			boolean dumpIncorrectWSOldValue = testDoc.dumpIncorrectWS;
			testDoc.dumpIncorrectWS = false;
			double differenceRatio = compareNL(testDoc, formattedDoc, lexerClass);
			testDoc.dumpIncorrectWS = dumpIncorrectWSOldValue;
			differenceRatios.add(differenceRatio);
		}
		return differenceRatios;
	}

	// return the median value of validate results array
	public static double validate(Corpus corpus, List<InputDocument> testDocs,
	                              Class<? extends Lexer> lexerClass,
	                              Class<? extends Parser> parserClass,
	                              String startRuleName,
	                              int tabSize)
		throws Exception
	{
		ArrayList<Double> differenceRatios =
			validateResults(corpus, testDocs, lexerClass, parserClass, startRuleName, tabSize);
		Collections.sort(differenceRatios);
		if (differenceRatios.size() % 2 == 1) return differenceRatios.get(differenceRatios.size() / 2);
		else if (differenceRatios.size() == 0) {
			System.err.println("Don't have enough results to get median value from validate results array!");
			return -1;
		}
		else return (differenceRatios.get(differenceRatios.size() / 2) + differenceRatios.get(differenceRatios.size() / 2 - 1))/2;
	}


	public static class Foo {
		public static void main(String[] args) throws Exception {
			ANTLRv4Lexer lexer = new ANTLRv4Lexer(new ANTLRFileStream("grammars/org/antlr/codebuff/ANTLRv4Lexer.g4"));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ANTLRv4Parser parser = new ANTLRv4Parser(tokens);
			ANTLRv4Parser.GrammarSpecContext tree = parser.grammarSpec();
			System.out.println(tree.toStringTree(parser));
		}
	}
}
