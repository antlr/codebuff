package org.antlr.codebuff;

import org.antlr.v4.runtime.Token;

public class TokenPositionAnalysis {
	public Token t;            // token from the input stream; it's position will usually differ from charIndexStart etc...
	public int charIndexStart; // where in *output* buffer the associated token starts; used to respond to clicks in formatted text
	public int charIndexStop;  // stop index (inclusive)
	public int ws;             // predicted '\n' or ' '
	public int align;          // predicted align/indent if ws indicates newline
	public String wsAnalysis = "n/a";
	public String alignAnalysis = "n/a";

	public TokenPositionAnalysis(Token t, int ws, String wsAnalysis, int align, String alignAnalysis) {
		this.t = t;
		this.ws = ws;
		this.wsAnalysis = wsAnalysis;
		this.align = align;
		this.alignAnalysis = alignAnalysis;
	}
}
