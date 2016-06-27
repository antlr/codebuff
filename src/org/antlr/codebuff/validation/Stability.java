package org.antlr.codebuff.validation;

import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.misc.Utils;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.antlr.codebuff.Dbg.normalizedLevenshteinDistance;
import static org.antlr.codebuff.Tool.QUORUM_DESCR;
import static org.antlr.codebuff.Tool.version;

public class Stability {
	public static final int STAGES = 5;

	public static void main(String[] args) throws Exception {
		LeaveOneOutValidator.FORCE_SINGLE_THREADED = true; // need this when we compare results file by file
		LangDescriptor[] languages = new LangDescriptor[]{
			QUORUM_DESCR,
//			JAVA_DESCR,
//			JAVA8_DESCR,
//			ANTLR4_DESCR,
//			SQLITE_CLEAN_DESCR,
//			TSQL_CLEAN_DESCR,
		};

		Map<String, List<Float>> results = new HashMap<>();
		for (LangDescriptor language : languages) {
			List<Float> errorRates = checkStability(language);
			System.out.println(language.name+" "+errorRates);
			results.put(language.name, errorRates);
		}
		for (String name : results.keySet()) {
			System.out.println(name+" = "+results.get(name));
		}

		String python =
			"#\n"+
			"# AUTO-GENERATED FILE. DO NOT EDIT\n" +
			"# CodeBuff <version> '<date>'\n" +
			"#\n"+
			"import numpy as np\n"+
			"import matplotlib.pyplot as plt\n\n" +
			"import matplotlib\n" +
			"fig = plt.figure()\n"+
			"ax = plt.subplot(111)\n"+
			"N = <N>\n" +
			"sizes = range(0,N)\n" +
			"<results:{r |\n" +
			"<r> = [<results.(r); separator={,}>]\n"+
			"ax.plot(sizes, <r>, label=\"<r>\", marker='o')\n" +
			"}>\n" +
			"ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)\n" +
			"xa = ax.get_xaxis()\n"+
			"xa.set_major_locator(matplotlib.ticker.MaxNLocator(integer=True))\n" +
			"ax.set_xlabel(\"Formatting Stage; stage 0 is first formatting pass\")\n"+
			"ax.set_ylabel(\"Median Leave-one-out Validation Error Rate\")\n" +
			"ax.set_title(\"<N>-Stage Formatting Stability\\nStage $n$ is formatted output of stage $n-1$\")\n"+
			"plt.legend()\n" +
			"plt.tight_layout()\n" +
			"fig.savefig('images/stability.pdf', format='pdf')\n"+
			"plt.show()\n";
		ST pythonST = new ST(python);
		pythonST.add("results", results);
		pythonST.add("version", version);
		pythonST.add("date", new Date());
		pythonST.add("N", STAGES+1);
		String fileName = "python/src/stability.py";
		Utils.writeFile(fileName, pythonST.render());
		System.out.println("wrote python code to "+fileName);
	}

	public static List<Float> checkStability(LangDescriptor language) throws Exception {
		List<Float> errorRates = new ArrayList<>();

		// format the corpus into tmp dir
		LeaveOneOutValidator validator0 = new LeaveOneOutValidator(language.corpusDir, language);
		Triple<List<Formatter>, List<Float>, List<Float>> results0 = validator0.validateDocuments(false, "/tmp/stability/1");
		errorRates.add( BuffUtils.median(results0.c) );

		List<Formatter> formatters0 = results0.a;
		// now try formatting it over and over
		for (int i = 1; i<=STAGES; i++) {
			String inputDir  = "/tmp/stability/"+i;
			String outputDir = "/tmp/stability/"+(i+1);
			LeaveOneOutValidator validator = new LeaveOneOutValidator(inputDir, language);
			Triple<List<Formatter>, List<Float>, List<Float>> results =
				validator.validateDocuments(false, outputDir);
			List<Formatter> formatters = results.a;
			List<Float> distances = new ArrayList<>();
			for (int j = 0; j<formatters.size(); j++) {
				Formatter f0 = formatters0.get(j);
				Formatter f = formatters.get(j);
				float editDistance = normalizedLevenshteinDistance(f.getOutput(), f0.getOutput());
				distances.add(editDistance);
			}
			errorRates.add( BuffUtils.median(distances) );
		}

		return errorRates;
	}
}
