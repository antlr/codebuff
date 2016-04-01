package org.antlr.codebuff;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by morganzhang on 2/23/16.
 */
public class FeatureMetaDataTweaker {
	private static ArrayList<Integer> allIndexOfFeatureNeedToTweak;  // stores the index of FeatureMetaData that need to be change
	private static FeatureMetaData[] ORIGINAL_FEATURES;

	// resultMap stores all results, the key is validated value, the value is a list of parameter combinations with the same validate value
	// Each hashMap in the arraylist is a parameter combination, key is the index of FeatureMetaData, the value is misMatchCost
	private static HashMap<Double, ArrayList<HashMap<Integer, Double>>> resultMap;
	private static Corpus corpus;
	private static ArrayList<InputDocument> testDocs;
	private static int tabSize;
	private static int eachParameterStepTime;  // how many times a parameter would change
	private static int allDifferentCombination;  // how many times in total it would test

	private static double minValueForParameter = 1.5;
	private static double maxValueForParameter = 2.0;
	private static double step = 0.2;
	private static int showResultMaxAmount = 10;  // top n results we want to see

	Class<? extends Lexer> lexerClass;
	Class<? extends Parser> parserClass;
	String startRuleName;

	public FeatureMetaDataTweaker(FeatureMetaData[] originalFeatures,
	                              Corpus c,
	                              ArrayList<InputDocument> docs,
	                              Class<? extends Lexer> lexerClass,
	                              Class<? extends Parser> parserClass,
	                              String startRuleName,
	                              int tSize)
	{
		ORIGINAL_FEATURES = originalFeatures;
		resultMap = new HashMap<>();
		allIndexOfFeatureNeedToTweak = new ArrayList<>();

		this.lexerClass = lexerClass;
		this.parserClass = parserClass;
		this.startRuleName = startRuleName;

		corpus = c;
		testDocs = docs;
		tabSize = tSize;

		for (int i=0; i<originalFeatures.length; i++) {
			if (originalFeatures[i].mismatchCost > 0) {
				allIndexOfFeatureNeedToTweak.add(i);
			}
		}

		eachParameterStepTime = 0;
		for (double i=minValueForParameter; i<=maxValueForParameter; i+=step) eachParameterStepTime++;

		allDifferentCombination = (int)Math.pow(eachParameterStepTime, allIndexOfFeatureNeedToTweak.size());

		System.out.println("\n=== Brute Force Parameters ===");
		System.out.printf("Each parameter try from %f to %f, step %f \n", minValueForParameter, maxValueForParameter, step);
		System.out.printf("There are %d parameters, and each parameter need change %d times, it will finally test %d combinations.\n", allIndexOfFeatureNeedToTweak.size(), eachParameterStepTime, allDifferentCombination);
		System.out.println("Testing parameters are: ");
		for (int i: allIndexOfFeatureNeedToTweak) {
			System.out.println(Tool.join(originalFeatures[i].abbrevHeaderRows, " "));
		}
		System.out.println();
	}

	public void tweakParameterAndTest() throws Exception {
		ArrayList<ArrayList<Double>> testParametersArray = bruteForceParameterGenerator();
		for (ArrayList<Double> parameters: testParametersArray) {
			HashMap<Integer, Double> testParameters = new HashMap<>();
			for (int i=0; i<parameters.size(); i++) {
				testParameters.put(allIndexOfFeatureNeedToTweak.get(i), parameters.get(i));
			}
			test(testParameters, lexerClass, parserClass, startRuleName);
		}
		System.out.println("=== Brute Force End ===");
		System.out.println("=== Best Parameter Combination Found ===");
		ArrayList<Double> resultValueArray = new ArrayList<>(resultMap.keySet());
		Collections.sort(resultValueArray);
		int showedResultCount = 0;
		for (double resultValue: resultValueArray) {
			ArrayList<HashMap<Integer, Double>> parametersArray = resultMap.get(resultValue);
			for (HashMap<Integer, Double> parameters: parametersArray) printValidateResult(parameters, resultValue);
			showedResultCount += parametersArray.size();
			if (showedResultCount > showResultMaxAmount) break;
		}
		System.out.println("=== Best Parameter Combination End ===");

		CollectFeatures.FEATURES_ALL = ORIGINAL_FEATURES;
	}

	// Generate all possible parameter combinations and return it as a list
	// The inside list is a combination of parameter.
	// The i parameter of the inside list's FeatureMetaDada index == allIndexOfFeatureNeedToTweak.get(i)
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

	public static void test(HashMap<Integer, Double> testParameters,
	                        Class<? extends Lexer> lexerClass,
	                        Class<? extends Parser> parserClass,
	                        String startRuleName)
		throws Exception
	{
		FeatureMetaData[] currentTestParameters = ORIGINAL_FEATURES;
		testParameters.forEach((k, v) -> currentTestParameters[k].mismatchCost = v);
		test(currentTestParameters, testParameters, lexerClass, parserClass, startRuleName);
	}

	public static void test(FeatureMetaData[] testFeatures,
	                        HashMap<Integer, Double> testParametersMap,
	                        Class<? extends Lexer> lexerClass,
	                        Class<? extends Parser> parserClass,
	                        String startRuleName)
		throws Exception
	{
		CollectFeatures.FEATURES_ALL = testFeatures;

		double validateResult = Tool.validate(corpus, testDocs, lexerClass, parserClass, startRuleName, tabSize);

		ArrayList<HashMap<Integer, Double>> newValue;
		if (resultMap.containsKey(validateResult)) {
			newValue = resultMap.get(validateResult);
		}
		else {
			newValue = new ArrayList<>();
		}
		newValue.add(testParametersMap);
		resultMap.put(validateResult, newValue);

		printValidateResult(testParametersMap, validateResult);
	}

	private static void printValidateResult(HashMap<Integer, Double> testParametersMap, double validateResult) {
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
		Corpus corpus = Tool.train(corpusDir, ".*\\.java", JavaLexer.class, JavaParser.class, "compilationUnit", tabSize);

		List<String> allFiles = Tool.getFilenames(new File(testFileDir), ".*\\.java");
		ArrayList<InputDocument> documents = (ArrayList<InputDocument>) Tool.load(allFiles, JavaLexer.class, tabSize);
		FeatureMetaDataTweaker f = new FeatureMetaDataTweaker(CollectFeatures.FEATURES_ALL, corpus, documents, JavaLexer.class, JavaParser.class, "compilationUnit", tabSize);
		f.tweakParameterAndTest();
	}
}
