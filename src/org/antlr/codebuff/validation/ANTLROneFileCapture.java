package org.antlr.codebuff.validation;

import org.antlr.codebuff.Dbg;

public class ANTLROneFileCapture extends OneFileCapture {
	public static void main(String[] args) throws Exception {
		runCaptureForOneLanguage(Dbg.ANTLR4_DESCR);
	}
}
