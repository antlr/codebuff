package org.antlr.codebuff;

import java.util.ArrayList;

/**
 * Created by morganzhang on 3/4/16.
 */
public class Tester {
	private static Corpus corpus;
	private static ArrayList<InputDocument> testDocs;
	private static int tabSize;

	public Tester(FeatureMetaData[] originalFeatures, Corpus c, ArrayList<InputDocument> docs, int tSize) {
		corpus = c;
		testDocs = docs;
		tabSize = tSize;

//		System.out.println("\n=== Brute Force Parameters ===");
//		System.out.printf("Each parameter try from %f to %f, step %f \n", minValueForParameter, maxValueForParameter, step);
//		System.out.printf("There are %d parameters, and each parameter need change %d times, it will finally test %d combinations.\n", allIndexOfFeatureNeedToTweak.size(), eachParameterStepTime, allDifferentCombination);
//		System.out.println("Testing parameters are: ");
//		for (int i: allIndexOfFeatureNeedToTweak) {
//			System.out.println(Tool.join(originalFeatures[i].abbrevHeaderRows, " "));
//		}
//		System.out.println();
	}

	public static double test(double[] parameters) {
		for (int i=0; i<parameters.length; i++) CollectFeatures.FEATURES_ALL[i].mismatchCost = parameters[i]; // should this ref originalFeatures
		double validateResult = 0;
		try {
			validateResult = Tool.validate(corpus, testDocs, tabSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return validateResult;
	}
}
