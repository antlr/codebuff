package org.antlr.codebuff;

import org.junit.Test;
import weka.classifiers.trees.RandomForest;

public class TestWeka {
	@Test
	public void foo() {
//		Instances trainingSet = new Instances();
		RandomForest rf = new RandomForest();
		rf.setNumTrees(100);
		rf.setDebug(true);
//		rf.buildClassifier(trainingSet);
	}
}
