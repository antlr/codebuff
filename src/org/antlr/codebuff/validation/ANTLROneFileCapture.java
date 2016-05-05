package org.antlr.codebuff.validation;

import org.antlr.codebuff.Tool;

public class ANTLROneFileCapture extends OneFileCapture {
	public static void main(String[] args) throws Exception {
		runCaptureForOneLanguage(Tool.ANTLR4_DESCR);
	}
}
