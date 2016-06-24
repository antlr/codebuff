package org.antlr.codebuff.validation;

import static org.antlr.codebuff.Dbg.languages;

public class DropWSFeaturesFromAll extends DropWSFeatures {
	public static void main(String[] args) throws Exception {
		testFeatures(languages, true);
	}
}
