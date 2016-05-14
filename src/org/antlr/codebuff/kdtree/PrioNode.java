/*
 *  Copyright (C) 2010 Duy Nguyen <duyn.ng@gmail.com>.
 */

package org.antlr.codebuff.kdtree;


/**
 * Associates a number with arbitrary data.
 *
 * @author duyn
 */
public final class PrioNode<T> {
	public final double priority;
	public final T data;

	public PrioNode(double priority, T data) {
		this.priority = priority;
		this.data = data;
	}
}
