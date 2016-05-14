package org.antlr.codebuff.misc;

import org.antlr.v4.runtime.misc.MurmurHash;

public class RuleAltKey {
	public String ruleName;
	public int altNum;

	public RuleAltKey(String ruleName, int altNum) {
		this.altNum = altNum;
		this.ruleName = ruleName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if (!(obj instanceof RuleAltKey)) {
			return false;
		}

		RuleAltKey other = (RuleAltKey)obj;
		return
			ruleName.equals(other.ruleName) &&
			altNum==other.altNum;
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		hash = MurmurHash.update(hash, ruleName);
		hash = MurmurHash.update(hash, altNum);
		return MurmurHash.finish(hash, 2);
	}

	@Override
	public String toString() {
		return String.format("%s:%d", ruleName, altNum);
	}
}
