package org.antlr.codebuff;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by morganzhang on 2/23/16.
 */
public class FeatureMetaDataTweaker {
	private static HashMap<Integer, Double> testParameters;
	private static ArrayList<Integer> allIndexOfFeatureNeedToTweak;
	private static FeatureMetaData[] ORIGINAL_FEATURES;
	private static HashMap<Double, ArrayList<HashMap<Integer, Double>>> resultMap;
	private static Corpus corpus;
	private static ArrayList<InputDocument> testDocs;
	private static int tabSize;
	private static int eachParameterStepTime;
	private static int allDifferentCombination;

	private static double minValueForParameter = 1.5;
	private static double maxValueForParameter = 2.0;
	private static double step = 0.2;
	private static int showResultMaxAmount = 10;

	public FeatureMetaDataTweaker(FeatureMetaData[] originalFeatures, Corpus c, ArrayList<InputDocument> docs, int tSize) {
		ORIGINAL_FEATURES = originalFeatures;
		testParameters = new HashMap<>();
		resultMap = new HashMap<>();
		allIndexOfFeatureNeedToTweak = new ArrayList<>();

		corpus = c;
		testDocs = docs;
		tabSize = tSize;

		for (int i=0; i<originalFeatures.length; i++) {
			if (originalFeatures[i].mismatchCost > 0 && i<5) {
				allIndexOfFeatureNeedToTweak.add(i);
				testParameters.put(i, 0.0);
			}
		}

		eachParameterStepTime = 0;
		for (double i=minValueForParameter; i<=maxValueForParameter; i+=step) eachParameterStepTime++;

		allDifferentCombination = (int)Math.pow(eachParameterStepTime, allIndexOfFeatureNeedToTweak.size());

		CollectFeatures.FEATURES = ORIGINAL_FEATURES;

		System.out.println("\n=== Brute Force Parameters ===");
		System.out.printf("Each parameter try from %f to %f, step %f \n", minValueForParameter, maxValueForParameter, step);
		System.out.printf("There are %d parameters, and each parameter need change %d times, it will finally test %d combinations.\n", allIndexOfFeatureNeedToTweak.size(), eachParameterStepTime, allDifferentCombination);
		System.out.println("Testing parameters are: ");
		for (int i: testParameters.keySet()) {
			System.out.println(Tool.join(originalFeatures[i].abbrevHeaderRows, " "));
		}
		System.out.println();
	}

	public static void tweakParameterAndTest() throws Exception {
		ArrayList<ArrayList<Double>> testParametersArray = bruteForceParameterGenerator();
		for (ArrayList<Double> parameters: testParametersArray) {
			HashMap<Integer, Double> testParameters = new HashMap<>();
			for (int i=0; i<parameters.size(); i++) {
				testParameters.put(allIndexOfFeatureNeedToTweak.get(i), parameters.get(i));
			}
			test(testParameters);
		}
		System.out.println("=== Brute Force End ===");
		System.out.println("=== Best Parameter Combination Found ===");
		ArrayList<Double> resultValueArray = new ArrayList<>(resultMap.keySet());
		Collections.sort(resultValueArray);
		int showedResultCount = 0;
		for (double resultValue: resultValueArray) {
			ArrayList<HashMap<Integer, Double>> parametersArray = resultMap.get(resultValue);
			for (HashMap<Integer, Double> parameters: parametersArray) showValidateResult(parameters, resultValue);
			showedResultCount += parametersArray.size();
			if (showedResultCount > showResultMaxAmount) break;
		}
	}

	private static ArrayList<ArrayList<Double>> bruteForceParameterGenerator() {
		ArrayList<ArrayList<Double>> testParametersArray = new ArrayList<>();

		for (int i=0; i<allIndexOfFeatureNeedToTweak.size(); i++) {
			if (i==0) {
				for (double j=minValueForParameter; j<=maxValueForParameter; j+=step) {
					ArrayList<Double> newValue = new ArrayList<>();
					newValue.add(j);
					testParametersArray.add(newValue);
				}
			}
			else {
				int loopTime = (int) Math.pow(eachParameterStepTime, i);
				for (int k=0; k<loopTime; k++) {
					ArrayList<Double> oldValue = testParametersArray.remove(0);
					for (double l=minValueForParameter; l<=maxValueForParameter; l+=step) {
						ArrayList<Double> newValue = new ArrayList<>(oldValue);
						newValue.add(l);
						testParametersArray.add(newValue);
					}
				}
			}
		}
		return testParametersArray;
	}

	public static void test(HashMap<Integer, Double> testParameters) throws Exception {
		FeatureMetaData[] currentTestParameters = ORIGINAL_FEATURES;
		testParameters.forEach((k, v) -> currentTestParameters[k].mismatchCost = v);
		test(currentTestParameters, testParameters);
	}

	public static void test(FeatureMetaData[] testFeatures, HashMap<Integer, Double> testParametersMap) throws Exception {
		CollectFeatures.FEATURES = testFeatures;

		double validateResult = Tool.validate(corpus, testDocs, tabSize);

		ArrayList<HashMap<Integer, Double>> newValue;
		if (resultMap.containsKey(validateResult)) {
			newValue = resultMap.get(validateResult);
		}
		else {
			newValue = new ArrayList<>();
		}
		newValue.add(testParametersMap);
		resultMap.put(validateResult, newValue);

		showValidateResult(testParametersMap, validateResult);
	}

	private static void showValidateResult(HashMap<Integer, Double> testParametersMap, double validateResult) {
		System.out.printf("Validate Result: %1.3f", validateResult);
		for (int i: allIndexOfFeatureNeedToTweak) {
			System.out.printf(" %s : %1.1f |", Tool.join(ORIGINAL_FEATURES[i].abbrevHeaderRows, " "), testParametersMap.get(i));
		}
		System.out.println();
	}

	public static void main(String[] args) throws Exception {
		int tabSize = 4;
		String corpusDir;
		String testFileDir;
		if ( args.length==2 ) {
			corpusDir = args[0];
			testFileDir = args[1];
		}
		else {
			corpusDir = "../samples/stringtemplate4/org/stringtemplate/v4/compiler/";
			testFileDir = "../samples/stringtemplate4/org/stringtemplate/v4/compiler/";
		}
		Corpus corpus = Tool.train(corpusDir, JavaLexer.class, JavaParser.class, tabSize);

		List<String> allFiles = Tool.getFilenames(new File(testFileDir), ".*\\.java");
		ArrayList<InputDocument> documents = (ArrayList<InputDocument>) Tool.load(allFiles, JavaLexer.class, tabSize);
		FeatureMetaDataTweaker f = new FeatureMetaDataTweaker(CollectFeatures.FEATURES, corpus, documents, tabSize);
		FeatureMetaDataTweaker.tweakParameterAndTest();
	}
}
