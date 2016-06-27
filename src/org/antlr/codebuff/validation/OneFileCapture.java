package org.antlr.codebuff.validation;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Utils;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.antlr.codebuff.Dbg.normalizedLevenshteinDistance;
import static org.antlr.codebuff.Tool.ANTLR4_DESCR;
import static org.antlr.codebuff.Tool.JAVA8_DESCR;
import static org.antlr.codebuff.Tool.JAVA_DESCR;
import static org.antlr.codebuff.Tool.QUORUM_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_NOISY_DESCR;
import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.TSQL_NOISY_DESCR;
import static org.antlr.codebuff.Tool.version;

public class OneFileCapture {
	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[] {
			QUORUM_DESCR,
			JAVA_DESCR,
			JAVA8_DESCR,
			ANTLR4_DESCR,
			SQLITE_NOISY_DESCR,
			SQLITE_CLEAN_DESCR,
			TSQL_NOISY_DESCR,
			TSQL_CLEAN_DESCR,
		};
		for (int i = 0; i<languages.length; i++) {
			LangDescriptor language = languages[i];
			runCaptureForOneLanguage(language);
		}
	}

	public static void runCaptureForOneLanguage(LangDescriptor language) throws Exception {
		List<String> filenames = Tool.getFilenames(new File(language.corpusDir), language.fileRegex);
		List<Float> selfEditDistances = new ArrayList<>();
		for (String fileName : filenames) {
			Corpus corpus = new Corpus(fileName, language);
			corpus.train();
			InputDocument testDoc = Tool.parse(fileName, corpus.language);
			Formatter formatter = new Formatter(corpus, language.indentSize);
			String output = formatter.format(testDoc, false);
			//		System.out.println(output);
			float editDistance = normalizedLevenshteinDistance(testDoc.content, output);
			System.out.println(fileName+" edit distance "+editDistance);
			selfEditDistances.add(editDistance);
		}

		Corpus corpus = new Corpus(language.corpusDir, language);
		corpus.train();

		List<Float> corpusEditDistances = new ArrayList<>();
		for (String fileName : filenames) {
			InputDocument testDoc = Tool.parse(fileName, corpus.language);
			Formatter formatter = new Formatter(corpus, language.indentSize);
			String output = formatter.format(testDoc, false);
			//		System.out.println(output);
			float editDistance = normalizedLevenshteinDistance(testDoc.content, output);
			System.out.println(fileName+"+corpus edit distance "+editDistance);
			corpusEditDistances.add(editDistance);
		}
		// heh this gives info on within-corpus variability. i.e., how good/consistent is my corpus?
		// those files with big difference are candidates for dropping from corpus or for cleanup.
		List<String> labels = BuffUtils.map(filenames, f -> '"'+new File(f).getName()+'"');

		String python =
			"#\n"+
				"# AUTO-GENERATED FILE. DO NOT EDIT\n" +
				"# CodeBuff <version> '<date>'\n" +
				"#\n"+
				"import numpy as np\n"+
				"import matplotlib.pyplot as plt\n\n" +
				"fig = plt.figure()\n"+
				"ax = plt.subplot(111)\n" +
				"labels = <labels>\n"+
				"N = len(labels)\n\n" +
				"featureIndexes = range(0,N)\n" +
				"<lang>_self = <selfEditDistances>\n" +
				"<lang>_corpus = <corpusEditDistances>\n" +
				"<lang>_diff = np.abs(np.subtract(<lang>_self, <lang>_corpus))\n\n" +
				"all = zip(<lang>_self, <lang>_corpus, <lang>_diff, labels)\n"+
				"all = sorted(all, key=lambda x : x[2], reverse=True)\n"+
				"<lang>_self, <lang>_corpus, <lang>_diff, labels = zip(*all)\n\n"+
				"ax.plot(featureIndexes, <lang>_self, label=\"<lang>_self\")\n"+
				"#ax.plot(featureIndexes, <lang>_corpus, label=\"<lang>_corpus\")\n"+
				"ax.plot(featureIndexes, <lang>_diff, label=\"<lang>_diff\")\n" +
				"ax.set_xticklabels(labels, rotation=60, fontsize=8)\n"+
				"plt.xticks(featureIndexes, labels, rotation=60)\n" +
				"ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)\n\n" +
				"ax.text(1, .25, 'median $f$ self distance = %5.3f, corpus+$f$ distance = %5.3f' %" +
				"    (np.median(<lang>_self),np.median(<lang>_corpus)))\n" +
				"ax.set_xlabel(\"File Name\")\n"+
				"ax.set_ylabel(\"Edit Distance\")\n"+
				"ax.set_title(\"Difference between Formatting File <lang> $f$\\nwith Training=$f$ and Training=$f$+Corpus\")\n"+
				"plt.legend()\n" +
				"plt.tight_layout()\n" +
				"fig.savefig(\"images/"+language.name+"_one_file_capture.pdf\", format='pdf')\n" +
				"plt.show()\n";
		ST pythonST = new ST(python);

		pythonST.add("lang", language.name);
		pythonST.add("version", version);
		pythonST.add("date", new Date());
		pythonST.add("labels", labels.toString());
		pythonST.add("selfEditDistances", selfEditDistances.toString());
		pythonST.add("corpusEditDistances", corpusEditDistances.toString());

		String code = pythonST.render();

		String fileName = "python/src/"+language.name+"_one_file_capture.py";
		Utils.writeFile(fileName, code);
		System.out.println("wrote python code to "+fileName);
	}
}
