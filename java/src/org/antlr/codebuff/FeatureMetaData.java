package org.antlr.codebuff;

public class FeatureMetaData {
	public String[] abbrevHeaderRows;
	public FeatureType type;
	public int mismatchCost;

	public FeatureMetaData(FeatureType type, String[] abbrevHeaderRows, int mismatchCost) {
		this.abbrevHeaderRows = abbrevHeaderRows;
		this.mismatchCost = mismatchCost;
		this.type = type;
	}
}
