package org.antlr.codebuff;

public enum FeatureType {
	TOKEN(12), RULE(14), INT(7), BOOL(5), COL(7),
	INFO_FILE(15), INFO_LINE(4), INFO_CHARPOS(4),
	UNUSED(0);
	public int displayWidth;

	FeatureType(int displayWidth) {
		this.displayWidth = displayWidth;
	}
}
