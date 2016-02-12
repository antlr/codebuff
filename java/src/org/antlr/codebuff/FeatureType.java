package org.antlr.codebuff;

public enum FeatureType {
	TOKEN(12), RULE(15), INT(7);
	public int displayWidth;

	FeatureType(int displayWidth) {
		this.displayWidth = displayWidth;
	}
}
