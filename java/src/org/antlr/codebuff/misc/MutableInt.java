package org.antlr.codebuff.misc;

public class MutableInt {
	public int i;

	public MutableInt(int i) {
		this.i = i;
	}

	public void inc() { i++; }

	public int asInt() { return i; }

	@Override
	public String toString() {
		return String.valueOf(i);
	}
}
