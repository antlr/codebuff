package org.antlr.codebuff;

public class FeatureMetaData {
	public String[] abbrevHeaderRows;
	public FeatureType type;
	public double mismatchCost;

	public FeatureMetaData(FeatureType type, String[] abbrevHeaderRows, int mismatchCost) {
		this.abbrevHeaderRows = abbrevHeaderRows;
		this.mismatchCost = mismatchCost;
		this.type = type;
	}
}
