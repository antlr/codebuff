package org.antlr.codebuff.validation;

import org.antlr.v4.runtime.Token;

public class TokenPositionAnalysis {
	public Token t;            // token from the input stream; it's position will usually differ from charIndexStart etc...
	public int charIndexStart; // where in *output* buffer the associated token starts; used to respond to clicks in formatted text
	public int charIndexStop;  // stop index (inclusive)
	public int wsPrediction;   // predicted category, '\n' or ' '
	public int alignPrediction;// predicted category, align/indent if ws indicates newline
	public int actualWS;       // actual category, '\n' or ' '
	public int actualAlign;    // actual category
	public String wsAnalysis = "n/a";
	public String alignAnalysis = "n/a";

	public TokenPositionAnalysis(Token t, int wsPrediction, String wsAnalysis, int alignPrediction, String alignAnalysis) {
		this.t = t;
		this.wsPrediction = wsPrediction;
		this.wsAnalysis = wsAnalysis;
		this.alignPrediction = alignPrediction;
		this.alignAnalysis = alignAnalysis;
	}
}
