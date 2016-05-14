/*
 *  Copyright (C) 2010 Duy Nguyen <duyn.ng@gmail.com>.
 */

package org.antlr.codebuff.kdtree;

import java.util.Arrays;

/**
 * A sample point in multi-dimensional space. Needed because each sample
 * may contain an arbitrary payload.
 *
 * Note: this class does not make allowance for a payload. Sub-class if
 * you want to store something more than just data points.
 *
 * @author duyn
 */
public class Exemplar {
	public final int[] domain;

	public Exemplar(int[] domain) {
		this.domain = domain;
	}

	public final boolean
	collocated(final Exemplar other) {
		return Arrays.equals(domain, other.domain);
	}
}
