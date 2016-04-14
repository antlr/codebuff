package org.antlr.codebuff.misc;

public class MutableDouble {
	public double d;

	public MutableDouble(double d) {
		this.d = d;
	}

	public double add(double value) {
		d += value;
		return d;
	}

	public double div(double value) {
		d /= value;
		return d;
	}

	@Override
	public String toString() {
		return Double.toString(d);
	}
}
