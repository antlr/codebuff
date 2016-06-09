package org.antlr.codebuff;

import org.antlr.codebuff.gui.GUIController;
import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.codebuff.validation.ClassificationAnalysis;
import org.antlr.codebuff.validation.LeaveOneOutValidator;
import org.antlr.codebuff.validation.TokenPositionAnalysis;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.misc.Triple;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Properties;

import static org.antlr.codebuff.misc.BuffUtils.filter;
import static org.antlr.v4.runtime.atn.PredictionMode.SLL;

/** Grammar must have WS/comments on hidden channel
 *
 * Testing:
 *
 * Tool  -dbg  -antlr     corpus/antlr4/training      grammars/org/antlr/codebuff/tsql.g4
 * Tool  -dbg  -leave-one-out -antlr     corpus/antlr4/training      corpus/antlr4/training/MASM.g4
 * Tool  -dbg  -leave-one-out -quorum     corpus/quorum/training      corpus/quorum/training/Containers/List.quorum
 * Tool  -dbg  -sqlite    corpus/sql/training      corpus/sql/training/dmart_bits.sql
 * Tool  -dbg  -leave-one-out -tsql      corpus/sql/training        corpus/sql/training/dmart_bits_PSQLRPT24.sql
 * Tool  -dbg  -java      corpus/java/training/stringtemplate4     src/org/antlr/codebuff/Tool.java
 * Tool  -dbg  -leave-one-out -java      corpus/java/training/stringtemplate4     corpus/java/training/stringtemplate4/org/stringtemplate/v4/StringRenderer.java
 * Tool  -dbg  -leave-one-out -java      corpus/java/training/guava     corpus/java/training/guava/base/Absent.java
 * Tool  -dbg  -java      corpus/java/training/antlr4-tool   corpus/java/training/stringtemplate4/org/stringtemplate/v4/AutoIndentWriter.java
 */
public class Tool {
	public static boolean showFileNames = false;
	public static boolean showTokens = false;

	public static final LangDescriptor QUORUM_DESCR =
		new LangDescriptor("quorum", "corpus/quorum/training", ".*\\.quorum", QuorumLexer.class, QuorumParser.class, "start", 4, QuorumLexer.COMMENTS);

	public static final LangDescriptor JAVA_DESCR =
		new LangDescriptor("java", "corpus/java/training/stringtemplate4", ".*\\.java", JavaLexer.class, JavaParser.class, "compilationUnit", 4, JavaLexer.LINE_COMMENT);
	public static final LangDescriptor JAVA8_DESCR =
		new LangDescriptor("java8", "corpus/java/training/stringtemplate4", ".*\\.java", Java8Lexer.class, Java8Parser.class, "compilationUnit", 4, Java8Lexer.LINE_COMMENT);

	public static final LangDescriptor JAVA_GUAVA_DESCR =
		new LangDescriptor("java_guava", "corpus/java/training/guava", ".*\\.java", JavaLexer.class, JavaParser.class, "compilationUnit",
		                   2, // wow. indent=2 not 4
						   JavaLexer.LINE_COMMENT);

	public static final LangDescriptor ANTLR4_DESCR =
		new LangDescriptor("antlr", "corpus/antlr4/training", ".*\\.g4", ANTLRv4Lexer.class, ANTLRv4Parser.class, "grammarSpec", 4, ANTLRv4Lexer.LINE_COMMENT);

	public static final LangDescriptor SQLITE_NOISY_DESCR =
		new LangDescriptor("sqlite_noisy", "corpus/sql/training", ".*\\.sql", SQLiteLexer.class, SQLiteParser.class, "parse", 4, SQLiteLexer.SINGLE_LINE_COMMENT);
	public static final LangDescriptor SQLITE_CLEAN_DESCR =
		new LangDescriptor("sqlite", "corpus/sql2/training", ".*\\.sql", SQLiteLexer.class, SQLiteParser.class, "parse", 4, SQLiteLexer.SINGLE_LINE_COMMENT);

	public static final LangDescriptor TSQL_NOISY_DESCR =
		new LangDescriptor("tsql_noisy", "corpus/sql/training", ".*\\.sql", tsqlLexer.class, tsqlParser.class, "tsql_file", 4, tsqlLexer.LINE_COMMENT);
	public static final LangDescriptor TSQL_CLEAN_DESCR =
		new LangDescriptor("tsql", "corpus/sql2/training", ".*\\.sql", tsqlLexer.class, tsqlParser.class, "tsql_file", 4, tsqlLexer.LINE_COMMENT);

	public static LangDescriptor[] languages = new LangDescriptor[] {
		QUORUM_DESCR,
		JAVA_DESCR,
		JAVA8_DESCR,
		JAVA_GUAVA_DESCR,
		ANTLR4_DESCR,
		SQLITE_NOISY_DESCR,
		SQLITE_CLEAN_DESCR,
		TSQL_NOISY_DESCR,
		TSQL_CLEAN_DESCR,
	};

	public static String version;
	static {
		try {
			setToolVersion();
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.err);
		}
	}

	public static void main(String[] args)
		throws Exception
	{
		if ( args.length<2 ) {
			System.err.println("ExtractFeatures [-dbg] [-leave-one-out] [-java|-java8|-antlr|-sqlite|-tsql] root-dir-of-samples test-file");
		}

		int arg = 0;
		boolean leaveOneOut = false;
		boolean collectAnalysis = false;
		if ( args[arg].equals("-dbg") ) {
			collectAnalysis = true;
			arg++;
		}
		if ( args[arg].equals("-leave-one-out") ) {
			leaveOneOut = true;
			arg++;
		}
		String language = args[arg++];
		language = language.substring(1);
		String corpusDir = args[arg++];
		String testFilename = args[arg];
		String output = "???";
		InputDocument testDoc = null;
		GUIController controller;
		List<TokenPositionAnalysis> analysisPerToken = null;
		Pair<String, List<TokenPositionAnalysis>> results;
		LangDescriptor lang = null;
		long start = 0, stop = 0;
		for (int i = 0; i<languages.length; i++) {
			if ( languages[i].name.equals(language) ) {
				lang = languages[i];
				break;
			}
		}
		if ( lang!=null && leaveOneOut ) {
			start = System.nanoTime();
			LeaveOneOutValidator validator = new LeaveOneOutValidator(corpusDir, lang);
			Triple<Formatter,Float,Float> val = validator.validateOneDocument(testFilename, null, collectAnalysis);
			testDoc = parse(testFilename, lang);
			stop = System.nanoTime();
			Formatter formatter = val.a;
			output = formatter.getOutput();
			System.out.println("output len = "+output.length());
			float editDistance = normalizedLevenshteinDistance(testDoc.content, output);
			System.out.println("normalized Levenshtein distance: "+editDistance);
			analysisPerToken = formatter.getAnalysisPerToken();

			CommonTokenStream original_tokens = tokenize(testDoc.content, lang.lexerClass);
			List<Token> wsTokens = filter(original_tokens.getTokens(),
			                              t -> t.getText().matches("\\s+"));
			String originalWS = tokenText(wsTokens);
			System.out.println("origin ws tokens len: "+originalWS.length());
			CommonTokenStream formatted_tokens = tokenize(output, lang.lexerClass);
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
		else if ( lang!=null ) {
			Corpus corpus = new Corpus(corpusDir, lang);
			corpus.train();
			testDoc = parse(testFilename, lang);
			start = System.nanoTime();
			Formatter formatter = new Formatter(corpus,lang.indentSize);
			output = formatter.format(testDoc, collectAnalysis);
			stop = System.nanoTime();
			analysisPerToken = formatter.getAnalysisPerToken();

			ClassificationAnalysis analysis = new ClassificationAnalysis(testDoc, analysisPerToken);
			System.out.println(analysis);

			CommonTokenStream original_tokens = tokenize(testDoc.content, corpus.language.lexerClass);
			List<Token> wsTokens = filter(original_tokens.getTokens(),
			                              t -> t.getText().matches("\\s+"));
			String originalWS = tokenText(wsTokens);
//			Utils.writeFile("/tmp/spaces1", originalWS);
//			Utils.writeFile("/tmp/input", testDoc.content);
//			Utils.writeFile("/tmp/output", output);

			CommonTokenStream formatted_tokens = tokenize(output, corpus.language.lexerClass);
			wsTokens = filter(formatted_tokens.getTokens(),
			                  t -> t.getText().matches("\\s+"));
			String formattedWS = tokenText(wsTokens);
//			Utils.writeFile("/tmp/spaces2", formattedWS);

			System.out.println("len orig, formatted="+testDoc.content.length()+", "+output.length());
			System.out.println("ws len orig, formatted="+originalWS.length()+", "+formattedWS.length());

			float editDistance = normalizedLevenshteinDistance(originalWS, formattedWS);
			System.out.println("Levenshtein distance of ws: "+editDistance);
			editDistance = normalizedLevenshteinDistance(testDoc.content, output);
			System.out.println("Levenshtein distance: "+editDistance);
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

	public static void setToolVersion() throws IOException {
		InputStream propsStream = Tool.class.getClassLoader().getResourceAsStream("codebuff.properties");
		Properties prop = new Properties();
		prop.load(propsStream);
		Tool.version = (String)prop.get("version");
		propsStream.close();
	}

	public static CodeBuffTokenStream tokenize(String doc, Class<? extends Lexer> lexerClass)
		throws Exception {
		ANTLRInputStream input = new ANTLRInputStream(doc);
		Lexer lexer = getLexer(lexerClass, input);

		CodeBuffTokenStream tokens = new CodeBuffTokenStream(lexer);
		tokens.fill();
		return tokens;
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

	/** Get all file contents into input doc list */
	public static List<InputDocument> load(List<String> fileNames, LangDescriptor language)
		throws Exception
	{
		List<InputDocument> documents = new ArrayList<>();
		for (String fileName : fileNames) {
			documents.add( parse(fileName, language) );
		}
		return documents;
	}

	public static String load(String fileName, int tabSize)
		throws Exception
	{
		Path path = FileSystems.getDefault().getPath(fileName);
		byte[] filearray = Files.readAllBytes(path);
		String content = new String(filearray);
		String notabs = expandTabs(content, tabSize);
		return notabs;
	}

	/**
	 * Parse doc and fill tree and tokens fields
	 */
	public static InputDocument parse(String fileName, LangDescriptor language)
		throws Exception
	{
		String content = load(fileName, language.indentSize);
		return parse(fileName, content, language);
	}

	public static InputDocument parse(String fileName, String content, LangDescriptor language)
		throws Exception
	{
		ANTLRInputStream input = new ANTLRInputStream(content);
		Lexer lexer = getLexer(language.lexerClass, input);
		input.name = fileName;

		InputDocument doc = new InputDocument(fileName, content, language);

		doc.tokens = new CodeBuffTokenStream(lexer);

		if ( showTokens ) {
			doc.tokens.fill();
			for (Object tok : doc.tokens.getTokens()) {
				System.out.println(tok);
			}
		}

		doc.parser = getParser(language.parserClass, doc.tokens);
		doc.parser.setBuildParseTree(true);

		// two-stage parsing. Try with SLL first
		doc.parser.getInterpreter().setPredictionMode(SLL);
		doc.parser.setErrorHandler(new BailErrorStrategy());
		doc.parser.removeErrorListeners();

		Method startRule = language.parserClass.getMethod(language.startRuleName);
		try {
			doc.setTree((ParserRuleContext) startRule.invoke(doc.parser, (Object[]) null));
		}
		catch (InvocationTargetException ex) {
			if ( ex.getCause() instanceof ParseCancellationException ) {
				doc.parser.reset();
				doc.tokens.reset(); // rewind input stream
				// back to standard listeners/handlers
				doc.parser.addErrorListener(
					new ANTLRErrorListener() {
						@Override
						public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
							System.err.println(recognizer.getInputStream().getSourceName()+" line " + line + ":" + charPositionInLine + " " + msg);
						}

						@Override
						public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
						}

						@Override
						public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
						}

						@Override
						public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
						}
					});
				doc.parser.setErrorHandler(new DefaultErrorStrategy());
				doc.parser.getInterpreter().setPredictionMode(PredictionMode.LL);
				doc.setTree((ParserRuleContext) startRule.invoke(doc.parser, (Object[]) null));
				if ( doc.parser.getNumberOfSyntaxErrors()>0 ) {
					doc.setTree(null);
				}
			}
		}

		return doc;
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
			FeatureType type = featureTypes[i].type;
			if ( type==FeatureType.TOKEN ||
				 type==FeatureType.RULE ||
				 type==FeatureType.INT ||
				 type==FeatureType.BOOL)
			{
				if ( A[i] != B[i] ) {
					count += featureTypes[i].mismatchCost;
				}
			}
			else if ( type==FeatureType.COLWIDTH ) {
				// threshold any len > RIGHT_MARGIN_ALARM
				int a = A[i];
				int b = B[i];
//				int a = Math.min(A[i], WIDE_LIST_THRESHOLD);
//				int b = Math.min(B[i], WIDE_LIST_THRESHOLD);
//				count += Math.abs(a-b) / (float) WIDE_LIST_THRESHOLD; // normalize to 0..1
//				count += sigmoid(a-b, 37);
				double delta = Math.abs(sigmoid(a, 43)-sigmoid(b, 43));
				count += delta;
			}
		}
		return count;
	}

	public static double sigmoid(int x, float center) {
		return 1.0 / (1.0 + Math.exp(-0.9*(x-center)));
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
		CodeBuffTokenStream original_tokens = tokenize(original, lexerClass);
//		String s = original_tokens.getText();
		CodeBuffTokenStream formatted_tokens = tokenize(formatted, lexerClass);
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
					int n = tabSize-col%tabSize;
					col+=n;
					buf.append(spaces(n));
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
