package org.antlr.codebuff;

public class TokenPositionAnalysis {
	public int charIndexStart; // where in output buffer the associated token starts; used to respond to clicks in formatted text
	public int charIndexStop; // stop index (inclusive)
	public String newline = "n/a";
	public String ws = "n/a";
	public String align = "n/a";

	public TokenPositionAnalysis(String newline, String align, String ws) {
		this.align = align;
		this.newline = newline;
		this.ws = ws;
	}
}
