package org.antlr.codebuff.validation;

import org.antlr.codebuff.FeatureMetaData;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.misc.Utils;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.antlr.codebuff.Tool.ANTLR4_DESCR;
import static org.antlr.codebuff.Tool.JAVA8_DESCR;
import static org.antlr.codebuff.Tool.JAVA_DESCR;
import static org.antlr.codebuff.Tool.QUORUM_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.version;
import static org.antlr.codebuff.Trainer.FEATURES_ALL;
import static org.antlr.codebuff.Trainer.FEATURES_HPOS;
import static org.antlr.codebuff.Trainer.FEATURES_INJECT_WS;

// hideous cut/paste from WS version
public class DropAlignFeatures {
	public static void main(String[] args) throws Exception {
		LangDescriptor[] languages = new LangDescriptor[] {
			QUORUM_DESCR,
			JAVA_DESCR,
			JAVA8_DESCR,
			ANTLR4_DESCR,
//			SQLITE_NOISY_DESCR,
			SQLITE_CLEAN_DESCR,
//			TSQL_NOISY_DESCR,
			TSQL_CLEAN_DESCR,
		};
		testFeatures(languages, false);
	}

	public static void testFeatures(LangDescriptor[] languages, boolean includeAllFeatures) throws Exception {
		Map<String,Map<String, Float>> langToFeatureMedians = new HashMap<>();

		FeatureMetaData[] whichFeatures = FEATURES_HPOS;
		if ( includeAllFeatures ) {
			whichFeatures = FEATURES_ALL;
		}

		List<String> labels = new ArrayList<>();
		labels.add("curated");
		if ( includeAllFeatures ) {
			labels.add("all-in");
		}
		for (FeatureMetaData f : whichFeatures) {
			if ( f==FeatureMetaData.UNUSED || f.type.toString().startsWith("INFO_") ) continue;
			labels.add(Utils.join(f.abbrevHeaderRows, " "));
		}

		for (LangDescriptor language : languages) {
			System.out.println("###### "+language.name);
			Map<String, Float> featureToErrors = new LinkedHashMap<>();

			FeatureMetaData[] alignFeatures = deepCopy(whichFeatures);

			// do it first to get answer with curated features
			List<Float> errors = getAlignmentErrorRates(language, FEATURES_INJECT_WS, alignFeatures);
			Collections.sort(errors);
			int n = errors.size();
			float quart = errors.get((int)(0.27*n));
			float median = errors.get(n/2);
			float quart3 = errors.get((int)(0.75*n));
			System.out.println("curated error median "+median);
			featureToErrors.put("curated", median);

			// do it again to get answer with all features if they want
			if ( includeAllFeatures ) {
				errors = getAlignmentErrorRates(language, FEATURES_INJECT_WS, FEATURES_ALL);
				Collections.sort(errors);
				n = errors.size();
				median = errors.get(n/2);
				System.out.println("all-in error median "+median);
				featureToErrors.put("all-in", median);
			}

			for (FeatureMetaData feature : alignFeatures) {
				if ( feature==FeatureMetaData.UNUSED || feature.type.toString().startsWith("INFO_") )
					continue;
				String name = Utils.join(feature.abbrevHeaderRows, " ");
				labels.add(name.trim());
				System.out.println("wack "+name);
				double saveCost = feature.mismatchCost;
				feature.mismatchCost = 0; // wack this feature

				errors = getAlignmentErrorRates(language, FEATURES_INJECT_WS, alignFeatures);
				Collections.sort(errors);
				n = errors.size();
				median = errors.get(n/2);
				featureToErrors.put(name, median);
				System.out.println("median error rates "+median);

				// reset feature
				feature.mismatchCost = saveCost;
			}
			System.out.println(featureToErrors);
			langToFeatureMedians.put(language.name, featureToErrors);
		}

		String python =
			"#\n"+
			"# AUTO-GENERATED FILE. DO NOT EDIT\n" +
			"# CodeBuff <version> '<date>'\n" +
			"#\n"+
			"import numpy as np\n"+
			"import matplotlib.pyplot as plt\n\n" +
			"fig = plt.figure()\n"+
			"ax = plt.subplot(111)\n"+
			"N = <numFeatures>\n" +
			"featureIndexes = range(0,N)\n" +
			"<langToMedians:{r |\n" +
			"<r> = [<langToMedians.(r); separator={, }>]\n"+
			"ax.plot(featureIndexes, <r>, label=\"<r>\")\n" +
			"}>\n" +
			"labels = [<labels:{l | '<l>'}; separator={, }>]\n" +
			"ax.set_xticklabels(labels, rotation=60, fontsize=8)\n"+
			"plt.xticks(featureIndexes, labels, rotation=60)\n" +
			"ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)\n\n" +
			"ax.set_xlabel(\"Alignment Feature\")\n"+
			"ax.set_ylabel(\"Median Error rate\")\n" +
			"ax.set_title(\"Effect of Dropping One Feature on Alignment Decision\\nMedian Leave-one-out Validation Error Rate\")\n"+
			"plt.legend()\n" +
			"plt.tight_layout()\n" +
			"fig.savefig(\"images/drop_one_align_feature"+(includeAllFeatures?"_from_all":"")+".pdf\", format='pdf')\n" +
			"plt.show()\n";
		String fileName = "python/src/drop_one_align_feature.py";
		if ( includeAllFeatures ) {
			fileName = "python/src/drop_one_align_feature_from_all.py";
		}
		ST pythonST = new ST(python);
		Map<String,Collection<Float>> langToMedians = new HashMap<>();
		int numFeatures = 0;
		for (String s : langToFeatureMedians.keySet()) {
			Map<String, Float> featureToErrors = langToFeatureMedians.get(s);
			langToMedians.put(s, featureToErrors.values());
			numFeatures = featureToErrors.values().size();
		}
		pythonST.add("langToMedians", langToMedians);
		pythonST.add("version", version);
		pythonST.add("date", new Date());
		pythonST.add("numFeatures", numFeatures);
		pythonST.add("labels", labels);

		String code = pythonST.render();

		Utils.writeFile(fileName, code);
		System.out.println("wrote python code to "+fileName);
	}

	public static List<Float> getAlignmentErrorRates(LangDescriptor language,
	                                                 FeatureMetaData[] injectWSFeatures,
	                                                 FeatureMetaData[] alignmentFeatures)
		throws Exception
	{
		LeaveOneOutValidator validator = new LeaveOneOutValidator(language.corpusDir, language);
		Triple<List<Formatter>,List<Float>,List<Float>> results =
			validator.validateDocuments(injectWSFeatures, alignmentFeatures, false, null);
		List<Formatter> formatters = results.a;
		List<Float> alignErrorRates = new ArrayList<>(); // don't include align errors
		for (Formatter formatter : formatters) {
			ClassificationAnalysis analysis =
				new ClassificationAnalysis(formatter.testDoc, formatter.getAnalysisPerToken());
			alignErrorRates.add(analysis.getAlignmentErrorRate());
		}
//		System.out.println(results.c);
//		System.out.println("vs");
//		System.out.println(alignErrorRates);
		return alignErrorRates;
	}

	public static FeatureMetaData[] deepCopy(FeatureMetaData[] features) {
		FeatureMetaData[] dup = new FeatureMetaData[features.length];
		for (int i = 0; i<dup.length; i++) {
			if ( features[i]==FeatureMetaData.UNUSED ) {
				dup[i] = FeatureMetaData.UNUSED;
			}
			else {
				dup[i] = new FeatureMetaData(features[i]);
			}
		}
		return dup;
	}
}
