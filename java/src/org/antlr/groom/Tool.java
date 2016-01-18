package org.antlr.groom;

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
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tool {
	public static boolean showFileNames = true;
	public static boolean showTokens = false;

	public static void main(String[] args)
		throws Exception
	{
		if ( args.length<2 ) {
			System.err.println("ExtractFeatures root-dir-of-samples test-file");
		}
		String corpusDir = args[0];
		String testFilename = args[1];
		Corpus corpus = train(corpusDir);
		InputDocument testDoc = load(testFilename);
		String output = format(corpus, testDoc);
		System.out.println(output);
	}

	/** Given a corpus, format the document by tokenizing and using the
	 *  corpus to locate newline and whitespace injection points.
	 */
	public static String format(Corpus corpus, InputDocument testDoc) throws Exception {
		parse(testDoc, JavaLexer.class, JavaParser.class, "compilationUnit");
		Formatter formatter = new Formatter(corpus, testDoc.tokens);
		ParseTreeWalker.DEFAULT.walk(formatter, testDoc.tree);
		return formatter.getOutput();
	}

	public static Corpus train(String rootDir) throws Exception {
		List<String> allFiles = getFilenames(new File(rootDir), ".*\\.java");
		List<InputDocument> documents = load(allFiles);
		return processSampleDocs(documents);
	}

	public static String toString(int[] features) {
		Vocabulary v = JavaParser.VOCABULARY;
		return String.format(
			"%s %s %d %s, %s %d %s",
			v.getDisplayName(features[0]),
			v.getDisplayName(features[1]), features[2],
			v.getDisplayName(features[3]), JavaParser.ruleNames[features[4]], features[5],
			v.getDisplayName(features[6])
		                    );
	}

	public void saveCSV(List<InputDocument> documents, String dir) throws IOException {
		FileWriter fw = new FileWriter(dir+"/style.csv");
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(Utils.join(CollectFeatures.FEATURE_NAMES, ", "));
		bw.write("\n");
		for (InputDocument doc : documents) {
			for (int[] record : doc.features) {
				String r = join(record, ", ");
				bw.write(r);
				bw.write('\n');
			}
		}
		bw.close();
	}

	public static Corpus processSampleDocs(List<InputDocument> docs)
		throws Exception
	{
		List<int[]> featureVectors = new ArrayList<>();
		List<Integer> categories = new ArrayList<>();
		for (InputDocument doc : docs) {
			if ( showFileNames ) System.out.println(doc);
			process(doc, JavaLexer.class, JavaParser.class, "compilationUnit");
			for (int i=0; i<doc.features.size(); i++) {
				categories.add(doc.injectNewlines.get(i));
				featureVectors.add(doc.features.get(i));
			}
		}
		System.out.printf("%d feature vectors\n", featureVectors.size());
		return new Corpus(featureVectors, categories);
	}

	/** Parse document, save feature vectors to the doc but return it also */
	public static void process(InputDocument doc,
	                           Class<? extends Lexer> lexerClass,
	                           Class<? extends Parser> parserClass,
	                           String startRuleName)
		throws Exception
	{
		parse(doc, lexerClass, parserClass, startRuleName);

		CollectFeatures collect = new CollectFeatures(doc.tokens);
		ParseTreeWalker.DEFAULT.walk(collect, doc.tree);
		doc.features = collect.getFeatures();
		doc.injectNewlines = collect.getInjectNewlines();
	}

	public static void tokenize(InputDocument doc, Class<? extends Lexer> lexerClass)
		throws Exception
	{
		ANTLRInputStream input = new ANTLRInputStream(doc.content, doc.content.length);
		Constructor<? extends Lexer> lexerCtor =
			lexerClass.getConstructor(CharStream.class);
		Lexer lexer = lexerCtor.newInstance(input);
		input.name = doc.fileName;

		doc.tokens = new CommonTokenStream(lexer);
		doc.tokens.fill();
	}

	/** Parse doc and fill tree and tokens fields */
	public static void parse(InputDocument doc,
	                         Class<? extends Lexer> lexerClass,
	                         Class<? extends Parser> parserClass,
	                         String startRuleName)
		throws Exception
	{
		ANTLRInputStream input = new ANTLRInputStream(doc.content, doc.content.length);
		Constructor<? extends Lexer> lexerCtor =
			lexerClass.getConstructor(CharStream.class);
		Lexer lexer = lexerCtor.newInstance(input);
		input.name = doc.fileName;

		Constructor<? extends Parser> parserCtor =
			parserClass.getConstructor(TokenStream.class);
		doc.tokens = new CommonTokenStream(lexer);

		if ( showTokens ) {
			doc.tokens.fill();
			for (Object tok : doc.tokens.getTokens()) {
				System.out.println(tok);
			}
		}

		Parser parser = parserCtor.newInstance(doc.tokens);
		parser.setBuildParseTree(true);
		Method startRule = parserClass.getMethod(startRuleName);
		doc.tree = (ParserRuleContext)startRule.invoke(parser, (Object[]) null);
	}

	/** Get all file contents into input array */
	public static List<InputDocument> load(List<String> fileNames) throws IOException {
		List<InputDocument> input = new ArrayList<InputDocument>(fileNames.size());
		int i = 0;
		for (String f : fileNames) {
			InputDocument doc = load(f);
			doc.index = i++;
			input.add(doc);
		}
		System.out.println(input.size()+" files");
		return input;
	}

	public static InputDocument load(String fileName) throws IOException {
		File f = new File(fileName);
		int size = (int)f.length();
		FileInputStream fis = new FileInputStream(fileName);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		char[] data = null;
		long numRead = 0;
		try {
			data = new char[size];
			numRead = isr.read(data);
		}
		finally {
			isr.close();
		}
		if ( numRead != size ) {
			data = Arrays.copyOf(data, (int) numRead);
//			System.err.println("read error; read="+numRead+"!="+f.length());
		}
		return new InputDocument(fileName, data);
	}

	public static List<String> getFilenames(File f, String inputFilePattern) throws Exception {
		List<String> files = new ArrayList<String>();
		getFilenames_(f, inputFilePattern, files);
		return files;
	}

	public static void getFilenames_(File f, String inputFilePattern, List<String> files) throws Exception {
		// If this is a directory, walk each file/dir in that directory
		if (f.isDirectory()) {
			String flist[] = f.list();
			for (int i=0; i < flist.length; i++) {
				getFilenames_(new File(f, flist[i]), inputFilePattern, files);
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

	public static void wipeLineAndPositionInfo(CommonTokenStream tokens) {
		tokens.fill();
		for (Token t : tokens.getTokens()) {
			CommonToken ct = (CommonToken)t;
			ct.setLine(0);
			ct.setCharPositionInLine(-1);
		}
	}

}
