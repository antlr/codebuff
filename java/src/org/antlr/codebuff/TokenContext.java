package org.antlr.codebuff;

import java.util.Arrays;

public class TokenContext {
	public final int[] tokens;

	public TokenContext(int[] tokens) {
		this.tokens = tokens;
	}

	public TokenContext(int t1, int t2, int t3, int t4) {
		this(new int[]{t1,t2,t3,t4});
	}

	@Override
	public int hashCode() {
		int h = tokens[0];
		h = h << 7 + tokens[1];
		h = h << 7 + tokens[2];
		h = h << 7 + tokens[3];
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj==this ) return true;
		if ( obj.hashCode()!=this.hashCode() ) return false;
		if ( obj.getClass()!=TokenContext.class ) return false;
		TokenContext other = (TokenContext)obj;
		return Arrays.equals(this.tokens, other.tokens);
	}

}
