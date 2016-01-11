package org.antlr.groom;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTree;
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

public class ExtractFeatures {
	public static boolean showFileNames = true;
	public static boolean showTokens = false;

	List<InputDocument> documents;

//	public static Class[] parsers = {
//		JavaParser.class
//	};
//
//	public static Class[] lexers = {
//		JavaLexer.class
//	};
//
	public static void main(String[] args)
		throws Exception
	{
		if ( args.length==0 ) {
			System.err.println("ExtractFeatures root-dir-of-samples");
		}
		new ExtractFeatures().go(args[0]);
	}

	public void go(String rootDir) throws Exception {
		List<String> allFiles = getFilenames(new File(rootDir), ".*\\.java");
		documents = load(allFiles);
		processSampleDocs(documents);
		FileWriter fw = new FileWriter(rootDir+"/style.csv");
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("inject newline, token type, column, length, enclosing rule, earliest ancestor rule, "+
		          "earliest ancestor length, prev token type, prev token column, prev token last char index");
		for (InputDocument doc : documents) {
			for (int[] record : doc.data) {
				String r = join(record, ", ");
				bw.write(r);
				bw.write('\n');
			}
		}
		bw.close();
	}

	public void saveCSV(InputDocument doc, String dir) {

	}

	public void processSampleDocs(List<InputDocument> docs)
		throws Exception
	{
		for (InputDocument doc : docs) {
			if ( showFileNames ) System.out.println(doc);
			process(doc, JavaLexer.class, JavaParser.class, "compilationUnit");
		}
	}

	public List<int[]> process(InputDocument doc,
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
		ParseTree tree = (ParseTree)startRule.invoke(parser, (Object[]) null);

		CollectFeatures collect = new CollectFeatures(tokens);
		ParseTreeWalker.DEFAULT.walk(collect, tree);
		doc.data = collect.getData();
		return doc.data;
	}

	/** Get all file contents into input array */
	public List<InputDocument> load(List<String> fileNames) throws IOException {
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

	public InputDocument load(String fileName) throws IOException {
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

	public List<String> getFilenames(File f, String inputFilePattern) throws Exception {
		List<String> files = new ArrayList<String>();
		getFilenames_(f, inputFilePattern, files);
		return files;
	}

	public void getFilenames_(File f, String inputFilePattern, List<String> files) throws Exception {
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
}
