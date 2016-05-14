package org.antlr.codebuff.kdtree;

/**
 *
 */
public interface MaxHeap<T> {
	public int size();

	public void offer(double key, T value);

	public void replaceMax(double key, T value);

	public void removeMax();

	public T getMax();

	public double getMaxKey();
}
