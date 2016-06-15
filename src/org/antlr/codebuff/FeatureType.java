package org.antlr.codebuff;

public enum FeatureType {
	TOKEN(12), RULE(14), INT(12), BOOL(5), // bool can be -1 meaning don't know
	INFO_FILE(15), INFO_LINE(4), INFO_CHARPOS(4),
	UNUSED(0);
	public int displayWidth;

	FeatureType(int displayWidth) {
		this.displayWidth = displayWidth;
	}
}
