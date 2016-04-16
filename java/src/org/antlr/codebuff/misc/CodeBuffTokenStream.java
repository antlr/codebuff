package org.antlr.codebuff.misc;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

/** Override to fix bug in LB() */
public class CodeBuffTokenStream extends CommonTokenStream {
	public CodeBuffTokenStream(TokenSource tokenSource) {
		super(tokenSource);
	}

	@Override
	protected Token LB(int k) {
		if ( k==0 || (p-k)<0 ) return null;

		int i = p;
		int n = 1;
		// find k good tokens looking backwards
		while ( i>=1 && n<=k ) {
			// skip off-channel tokens
			i = previousTokenOnChannel(i - 1, channel);
			n++;
		}
		if ( i<0 ) return null;
		return tokens.get(i);
	}

}
