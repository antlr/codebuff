package org.antlr.codebuff.misc;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.MurmurHash;

/** A key that identifies a parent/child/separator relationship where the child
 *  is a sibling list. The separator must be part of key so that expressions
 *  can distinguish between different operators.
 */
public class ParentSiblingListKey {
	public int parentRuleIndex;
	public int parentRuleAlt;
	public int childRuleIndex;
	public int childRuleAlt;
	public int separatorTokenType;

	public ParentSiblingListKey(ParserRuleContext parent, ParserRuleContext child, int separatorTokenType) {
		parentRuleIndex = parent.getRuleIndex();
		parentRuleAlt = parent.getAltNumber();
		childRuleIndex = child.getRuleIndex();
		childRuleAlt = child.getAltNumber();
		this.separatorTokenType = separatorTokenType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if (!(obj instanceof ParentSiblingListKey)) {
			return false;
		}

		ParentSiblingListKey other = (ParentSiblingListKey)obj;
		return
			parentRuleIndex==other.parentRuleIndex &&
			parentRuleAlt==other.parentRuleAlt &&
			childRuleIndex==other.childRuleIndex &&
			childRuleAlt==other.childRuleAlt &&
			separatorTokenType==other.separatorTokenType;
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		hash = MurmurHash.update(hash, parentRuleIndex);
		hash = MurmurHash.update(hash, parentRuleAlt);
		hash = MurmurHash.update(hash, childRuleIndex);
		hash = MurmurHash.update(hash, childRuleAlt);
		hash = MurmurHash.update(hash, separatorTokenType);
		return MurmurHash.finish(hash, 5);
	}

	@Override
	public String toString() {
		return String.format("(%d, %d, %d, %d, %d)",
		                     parentRuleIndex, parentRuleAlt,
	                         childRuleIndex, childRuleAlt,
		                     separatorTokenType);
	}
}
