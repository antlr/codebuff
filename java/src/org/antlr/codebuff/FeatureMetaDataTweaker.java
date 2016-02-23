package org.antlr.codebuff;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by morganzhang on 2/23/16.
 */
public class FeatureMetaDataTweaker {
	private static ArrayList<Integer> allFeaturesNeedToTweak;
	private static HashMap<Integer, Double> testParameters;
	private static FeatureMetaData[] ORIGINAL_FEATURES;

	private static double minValueForParameter = 0;
	private static double maxValueForParameter = 3.0;
	private static double step = 0.1;

	public FeatureMetaDataTweaker(FeatureMetaData[] originalFeatures) {
		ORIGINAL_FEATURES = originalFeatures;
		allFeaturesNeedToTweak = new ArrayList<>();
		testParameters = new HashMap<>();

		for (int i=0; i<originalFeatures.length; i++) {
			if (originalFeatures[i].mismatchCost > 0) allFeaturesNeedToTweak.add(i);
			testParameters.put(i, 0.0);
		}
	}

	public static void tweakParameter() {
		for (int i=0; i<allFeaturesNeedToTweak.size(); i++) {
			for (double j=minValueForParameter; j<maxValueForParameter; j+=step) {
				testParameters.put(i, j);
			}
		}
	}

	public static void test(HashMap<Integer, Double> testParameters) {
		FeatureMetaData[] currentTestParameters = ORIGINAL_FEATURES;
		testParameters.forEach((k, v) -> currentTestParameters[k].mismatchCost = v);
		test(currentTestParameters);
	}

	public static void test(FeatureMetaData[] testParameters) {

	}


}
