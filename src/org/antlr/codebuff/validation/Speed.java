package org.antlr.codebuff.validation;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.antlr.codebuff.Tool.getFilenames;
import static org.antlr.codebuff.Tool.languages;
import static org.antlr.codebuff.Trainer.FEATURES_HPOS;
import static org.antlr.codebuff.Trainer.FEATURES_INJECT_WS;
import static org.antlr.codebuff.misc.BuffUtils.filter;

/** Test the speed of loading (parsing), training on corpus - doc, and formatting one doc.
 *
 *  Sample runs:
 *
 *      -antlr corpus/antlr4/training/Java8.g4
 *      -java_guava corpus/java/training/guava/cache/LocalCache.java
 *      -java8_guava corpus/java/training/guava/cache/LocalCache.java
 */

public class Speed {
	public static final int TRIALS = 20;
	public static void main(String[] args) throws Exception {
		String langname = args[0].substring(1);
		String testFilename = args[1];
		LangDescriptor language = null;
		for (int i = 0; i<languages.length; i++) {
			if ( languages[i].name.equals(langname) ) {
				language = languages[i];
				break;
			}
		}
		if ( language==null ) {
			System.err.println("Language "+langname+" unknown");
			return;
		}

		// load all files up front
		long load_start = System.nanoTime();
		List<String> allFiles = getFilenames(new File(language.corpusDir), language.fileRegex);
		List<InputDocument> documents = Tool.load(allFiles, language);
		long load_stop = System.nanoTime();
		long load_time = (load_stop-load_start)/1_000_000;
		System.out.printf("Loaded %d files in %dms\n", documents.size(), load_time);

		final String path = new File(testFilename).getAbsolutePath();
		List<InputDocument> others = filter(documents, d -> !d.fileName.equals(path));
		List<InputDocument> excluded = filter(documents, d -> d.fileName.equals(path));
		assert others.size() == documents.size() - 1;
		if ( excluded.size()==0 ) {
			System.err.println("Doc not in corpus: "+path);
			return;
		}
		InputDocument testDoc = excluded.get(0);

		List<Integer> training = new ArrayList<>();
		List<Integer> formatting = new ArrayList<>();
		for (int i = 1; i<=TRIALS; i++) {
			Pair<Integer, Integer> timing = test(language, others, testDoc);
			training.add(timing.a);
			formatting.add(timing.b);
		}
		// drop first four
		training = training.subList(5,training.size());
		formatting = formatting.subList(5,formatting.size());
		System.out.printf("median of [5:%d] training %dms\n", TRIALS-1, BuffUtils.median(training));
		System.out.printf("median of [5:%d] formatting %dms\n", TRIALS-1, BuffUtils.median(formatting));
	}

	public static Pair<Integer,Integer> test(LangDescriptor language,
	                                         List<InputDocument> others,
	                                         InputDocument testDoc)
		throws Exception
	{
		long train_start = System.nanoTime();
		Corpus corpus = new Corpus(others, language);
		corpus.train();
		long train_stop = System.nanoTime();

		long format_start = System.nanoTime();
		Formatter formatter = new Formatter(corpus, language.indentSize, Formatter.DEFAULT_K,
		                                    FEATURES_INJECT_WS, FEATURES_HPOS);
		formatter.format(testDoc, false);
		long format_stop = System.nanoTime();

		long train_time = (train_stop-train_start)/1_000_000;
		long format_time = (format_stop-format_start)/1_000_000;

		System.out.printf("%s training of %s = %dms formatting = %dms\n",
		                  language.name,
		                  testDoc.fileName,
		                  train_time,
		                  format_time);

		return new Pair<>((int)train_time, (int)format_time);
	}
}
