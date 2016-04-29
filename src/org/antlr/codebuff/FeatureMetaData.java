package org.antlr.codebuff;

public class FeatureMetaData {
	public static final FeatureMetaData UNUSED = new FeatureMetaData(FeatureType.UNUSED, null, 0);
	public String[] abbrevHeaderRows;
	public FeatureType type;
	public double mismatchCost;

	public FeatureMetaData(FeatureMetaData old) {
		this.abbrevHeaderRows = old.abbrevHeaderRows;
		this.type = old.type;
		this.mismatchCost = old.mismatchCost;
	}

	public FeatureMetaData(FeatureType type, String[] abbrevHeaderRows, int mismatchCost) {
		this.abbrevHeaderRows = abbrevHeaderRows;
		this.mismatchCost = mismatchCost;
		this.type = type;
	}

}
