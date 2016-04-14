package org.antlr.codebuff.misc;

import org.antlr.v4.runtime.misc.MurmurHash;
import org.antlr.v4.runtime.misc.ObjectEqualityComparator;

public class Quad<A,B,C,D> {
	public final A a;
	public final B b;
	public final C c;
	public final D d;

	public Quad(A a, B b, C c, D d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if (!(obj instanceof Quad<?, ?, ?, ?>)) {
			return false;
		}

		Quad<?, ?, ?, ?> other = (Quad<?, ?, ?, ?>)obj;
		return ObjectEqualityComparator.INSTANCE.equals(a, other.a)
			&& ObjectEqualityComparator.INSTANCE.equals(b, other.b)
			&& ObjectEqualityComparator.INSTANCE.equals(c, other.c)
			&& ObjectEqualityComparator.INSTANCE.equals(d, other.d);
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		hash = MurmurHash.update(hash, a);
		hash = MurmurHash.update(hash, b);
		hash = MurmurHash.update(hash, c);
		hash = MurmurHash.update(hash, d);
		return MurmurHash.finish(hash, 4);
	}

	@Override
	public String toString() {
		return String.format("(%s, %s, %s, %s)", a, b, c, d);
	}
}
