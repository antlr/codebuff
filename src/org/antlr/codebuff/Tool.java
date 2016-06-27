package org.antlr.codebuff;

import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.ANTLRErrorListener;
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
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.misc.Utils;

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
import java.util.Map;
import java.util.Properties;

import static org.antlr.codebuff.Trainer.FEATURES_HPOS;
import static org.antlr.codebuff.Trainer.FEATURES_INJECT_WS;
import static org.antlr.codebuff.misc.BuffUtils.filter;
import static org.antlr.v4.runtime.atn.PredictionMode.SLL;

/** The main CodeBuff tool used to format files. Examples:
 *
   $ java -jar target/codebuff-1.4.19.jar  \
        -g org.antlr.codebuff.ANTLRv4 -rule grammarSpec -corpus corpus/antlr4/training \
        -files g4 -indent 4 -comment LINE_COMMENT T.g4

   $ java -jar codebuff-1.4.19 \
        -g org.antlr.codebuff.Java -rule compilationUnit \
        -corpus corpus/java/training/stringtemplate4  -files java \
        -comment LINE_COMMENT T.java
 *
 * You have to have some libs in your CLASSPATH. See pom.xml, but it's
 * ANTLR 4, Apache commons-lang3, Google guava, and StringTemplate 4.
 *
 * The grammar must be run through ANTLR and be compiled (and in the CLASSPATH).
 * For Java8.g4, use "-g Java8", not the filename. For separated
 * grammar files, like ANTLRv4Parser.g4 and ANTLRv4Lexer.g4, use "-g ANTLRv4".
 * If the grammar is in a package, use fully-qualified like
 * "-g org.antlr.codebuff.ANTLRv4"
 *
 * Output goes to stdout if no -o option used.
 */
public class Tool {
	public static boolean showFileNames = false;
	public static boolean showTokens = false;

	public static final LangDescriptor QUORUM_DESCR =
		new LangDescriptor("quorum", "corpus/quorum/training", ".*\\.quorum", QuorumLexer.class, QuorumParser.class, "start", 4, QuorumLexer.COMMENTS);

	public static final LangDescriptor JAVA_DESCR =
		new LangDescriptor("java_st", "corpus/java/training/stringtemplate4", ".*\\.java", JavaLexer.class, JavaParser.class, "compilationUnit", 4, JavaLexer.LINE_COMMENT);
	public static final LangDescriptor JAVA8_DESCR =
		new LangDescriptor("java8_st", "corpus/java/training/stringtemplate4", ".*\\.java", Java8Lexer.class, Java8Parser.class, "compilationUnit", 4, Java8Lexer.LINE_COMMENT);

	public static final LangDescriptor JAVA_GUAVA_DESCR =
		new LangDescriptor("java_guava", "corpus/java/training/guava", ".*\\.java", JavaLexer.class, JavaParser.class, "compilationUnit",
		                   2, // wow. indent=2 not 4
						   JavaLexer.LINE_COMMENT);
	public static final LangDescriptor JAVA8_GUAVA_DESCR =
		new LangDescriptor("java8_guava", "corpus/java/training/guava", ".*\\.java", Java8Lexer.class, Java8Parser.class, "compilationUnit",
		                   2, // wow. indent=2 not 4
						   Java8Lexer.LINE_COMMENT);

	public static final LangDescriptor ANTLR4_DESCR =
		new LangDescriptor("antlr", "corpus/antlr4/training", ".*\\.g4", ANTLRv4Lexer.class, ANTLRv4Parser.class, "grammarSpec", 4, ANTLRv4Lexer.LINE_COMMENT);

	public static final LangDescriptor SQLITE_NOISY_DESCR =
		new LangDescriptor("sqlite_noisy", "corpus/sql/training", ".*\\.sql", SQLiteLexer.class, SQLiteParser.class, "parse", 4, SQLiteLexer.SINGLE_LINE_COMMENT);
	public static final LangDescriptor SQLITE_CLEAN_DESCR =
		new LangDescriptor("sqlite", "corpus/sqlclean/training", ".*\\.sql", SQLiteLexer.class, SQLiteParser.class, "parse", 4, SQLiteLexer.SINGLE_LINE_COMMENT);

	public static final LangDescriptor TSQL_NOISY_DESCR =
		new LangDescriptor("tsql_noisy", "corpus/sql/training", ".*\\.sql", tsqlLexer.class, tsqlParser.class, "tsql_file", 4, tsqlLexer.LINE_COMMENT);

	public static final LangDescriptor TSQL_CLEAN_DESCR =
		new LangDescriptor("tsql", "corpus/sqlclean/training", ".*\\.sql", tsqlLexer.class, tsqlParser.class, "tsql_file", 4, tsqlLexer.LINE_COMMENT);

	public static LangDescriptor[] languages = new LangDescriptor[] {
		QUORUM_DESCR,
		JAVA_DESCR,
		JAVA8_DESCR,
		JAVA_GUAVA_DESCR,
		JAVA8_GUAVA_DESCR,
		ANTLR4_DESCR,
		SQLITE_NOISY_DESCR,
		SQLITE_CLEAN_DESCR,
		TSQL_NOISY_DESCR,
		TSQL_CLEAN_DESCR,
	};

	public static String version;

	static {
		try {
			Tool.setToolVersion();
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.err);
		}
	}

	public static void main(String[] args) throws Exception {
		if ( args.length<7 ) {
			System.err.println("org.antlr.codebuff.Tool -g grammar-name -rule start-rule -corpus root-dir-of-samples \\\n" +
			                   "   [-files file-extension] [-indent num-spaces] \\" +
			                   "   [-comment line-comment-name] [-o output-file] file-to-format");
			return;
		}

		String grammarName = null;
		String startRule = null;
		String corpusDir = null;
		String indentS = "4";
		String commentS = null;
		String testFileName = null;
		String outputFileName = null;
		String fileExtension = null;
		int i = 0;
		while ( i<args.length && args[i].startsWith("-") ) {
			switch ( args[i] ) {
				case "-g":
					i++;
					grammarName = args[i++];
					break;
				case "-rule" :
					i++;
					startRule = args[i++];
					break;
				case "-corpus" :
					i++;
					corpusDir = args[i++];
					break;
				case "-files" :
					i++;
					fileExtension = args[i++];
					break;
				case "-indent" :
					i++;
					indentS = args[i++];
					break;
				case "-comment" :
					i++;
					commentS = args[i++];
					break;
				case "-o" :
					i++;
					outputFileName = args[i++];
					break;
			}
		}
		testFileName = args[i]; // must be last

		System.out.println("gramm: "+grammarName);
		String parserClassName = grammarName+"Parser";
		String lexerClassName = grammarName+"Lexer";
		Class<? extends Parser> parserClass = null;
		Class<? extends Lexer> lexerClass = null;
		Lexer lexer = null;
		try {
			parserClass = (Class<? extends Parser>)Class.forName(parserClassName);
			lexerClass = (Class<? extends Lexer>)Class.forName(lexerClassName);
		}
		catch (Exception e) {
			System.err.println("Can't load "+parserClassName+" or maybe "+lexerClassName);
			System.err.println("Make sure they are generated by ANTLR, compiled, and in CLASSPATH");
			e.printStackTrace(System.err);
		}
		if ( parserClass==null | lexerClass==null ) {
			return; // don't return from catch!
		}
		int indentSize = Integer.parseInt(indentS);
		int singleLineCommentType = -1;
		if ( commentS!=null ) {
			try {
				lexer = getLexer(lexerClass, null);
			}
			catch (Exception e) {
				System.err.println("Can't instantiate lexer "+lexerClassName);
				e.printStackTrace(System.err);
			}
			if ( lexer==null ) return;
			Map<String, Integer> tokenTypeMap = lexer.getTokenTypeMap();
			if ( tokenTypeMap.containsKey(commentS) ) {
				singleLineCommentType = tokenTypeMap.get(commentS);
			}
		}
		String fileRegex = null;
		if ( fileExtension!=null ) {
			fileRegex = ".*\\."+fileExtension;
		}
		LangDescriptor language = new LangDescriptor(grammarName, corpusDir, fileRegex,
		                                             lexerClass, parserClass, startRule,
		                                             indentSize, singleLineCommentType);
		format(language, testFileName, outputFileName);
	}

	public static void format(LangDescriptor language,
	                          String testFileName,
	                          String outputFileName)
		throws Exception
	{
		// load all files up front
		List<String> allFiles = getFilenames(new File(language.corpusDir), language.fileRegex);
		List<InputDocument> documents = load(allFiles, language);
		// if in corpus, don't include in corpus
		final String path = new File(testFileName).getAbsolutePath();
		List<InputDocument> others = filter(documents, d -> !d.fileName.equals(path));
		InputDocument testDoc = parse(testFileName, language);
		Corpus corpus = new Corpus(others, language);
		corpus.train();

		Formatter formatter = new Formatter(corpus, language.indentSize, Formatter.DEFAULT_K,
		                                    FEATURES_INJECT_WS, FEATURES_HPOS);
		String output = formatter.format(testDoc, false);

		if ( outputFileName!=null ) {
			Utils.writeFile(outputFileName, output);
		}
		else {
			System.out.print(output);
		}
	}

	public static void setToolVersion() throws IOException {
		InputStream propsStream = Tool.class.getClassLoader().getResourceAsStream("codebuff.properties");
		Properties prop = new Properties();
		prop.load(propsStream);
		version = (String)prop.get("version");
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
		if ( documents.size()>0 ) {
			documents.get(0).parser.getInterpreter().clearDFA(); // free up memory
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
}
