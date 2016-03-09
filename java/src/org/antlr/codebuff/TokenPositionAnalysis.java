package org.antlr.codebuff;

public class TokenPositionAnalysis {
	public String newline = "n/a";
	public String ws = "n/a";
	public String indent = "n/a";
	public String align = "n/a";

	public TokenPositionAnalysis() {
	}

	public TokenPositionAnalysis(String newline, String align, String indent, String ws) {
		this.align = align;
		this.indent = indent;
		this.newline = newline;
		this.ws = ws;
	}
}
