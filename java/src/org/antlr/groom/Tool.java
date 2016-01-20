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
		List<Integer> injectNewlines = new ArrayList<>();
		List<Integer> injectWS = new ArrayList<>();
		for (InputDocument doc : docs) {
			if ( showFileNames ) System.out.println(doc);
			process(doc, JavaLexer.class, JavaParser.class, "compilationUnit");
			for (int i=0; i<doc.features.size(); i++) {
				injectNewlines.add(doc.injectNewlines.get(i));
				injectWS.add(doc.injectWS.get(i));
				featureVectors.add(doc.features.get(i));
			}
		}
		System.out.printf("%d feature vectors\n", featureVectors.size());
		return new Corpus(featureVectors, injectNewlines, injectWS);
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
		doc.injectWS = collect.getInjectWS();
	}

	public static List<Token> tokenize(String doc, Class<? extends Lexer> lexerClass)
		throws Exception
	{
		ANTLRInputStream input = new ANTLRInputStream(doc);
		Constructor<? extends Lexer> lexerCtor =
			lexerClass.getConstructor(CharStream.class);
		Lexer lexer = lexerCtor.newInstance(input);

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();
		return tokens.getTokens();
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
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		if ( showTokens ) {
			tokens.fill();
			for (Object tok : tokens.getTokens()) {
				System.out.println(tok);
			}
		}

		Parser parser = parserCtor.newInstance(tokens);
		parser.setBuildParseTree(true);
		Method startRule = parserClass.getMethod(startRuleName);
		ParserRuleContext tree = (ParserRuleContext)startRule.invoke(parser, (Object[]) null);

		doc.tokens = tokens;
		doc.tree = tree;
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
//			System.err.println("read error; read="+numRead+"!="+f.length()());
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

	public static int L0_Distance(boolean[] categorical, int[] A, int[] B) {
		int count = 0; // count how many mismatched categories there are
		int num_categorical = 0;
		for (int i=0; i<A.length; i++) {
			if ( categorical[i] ) {
				num_categorical++;
				if ( A[i] != B[i] ) {
					count++;
				}
			}
		}
		return count;
	}

//	// From https://en.wikipedia.org/wiki/Levenshtein_distance
//	public static int LevenshteinDistance(String s, String t) {
//		return LevenshteinDistance(s, s.length(), t, t.length());
//	}
//
//	public static int LevenshteinDistance(String s, int slen, String t, int tlen) {
//		int cost;
//
//		// base case: empty strings
//		if (slen == 0) return tlen;
//		if (tlen == 0) return slen;
//
//		// test if last characters of the strings match
//		if ( s.charAt(slen-1) == t.charAt(tlen-1) ) {
//			cost = 0;
//		}
//		else {
//			cost = 1;
//		}
//
//		// return minimum of delete char from s, delete char from t, and delete char from both
//		return
//			Math.min(
//				Math.min(LevenshteinDistance(s, slen - 1, t, tlen    ) + 1,
//						 LevenshteinDistance(s, slen    , t, tlen - 1) + 1),
//				LevenshteinDistance(s, slen - 1, t, tlen - 1) + cost);
//	}

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

	/** Compute a document difference metric 0-1.0 between two documents that
	 *  are identical other than (likely) the whitespace. 1.0 means the docs
	 *  are maximally different and 0 means docs are identical.
	 *  The Levenshtein distance between the docs counts only
	 *  whitespace diffs as the non-WS content is identical.
	 *  Levenshtein distance is bounded by 0..max(len(doc1),len(doc2)) so
	 *  we normalize the distance by dividing by max WS count.
	 */
	public static double whitespaceDifference(String original,
	                                          String formatted,
	                                          Class<? extends Lexer> lexerClass)
		throws Exception
	{
		List<Token> tokens = tokenize(original, lexerClass);
		int non_ws = 0;
		for (Token tok : tokens) {
			non_ws += tok.getText().length();
		}
		int original_ws = original.length() - non_ws;
		int formatted_ws = formatted.length() - non_ws;
		int max_ws = Math.max(original_ws, formatted_ws);
		int ws_distance = Tool.levenshteinDistance(original, formatted);
		float normalized_ws_distance = ((float) ws_distance)/max_ws;
		return normalized_ws_distance;
	}
}
