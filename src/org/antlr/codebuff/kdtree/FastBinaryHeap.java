/*
 *  Copyright (C) 2010 Duy Nguyen <duyn.ng@gmail.com>.
 */

package org.antlr.codebuff.kdtree;

import java.util.Iterator;
/**
 * A fast implicit binary heap which uses less swaps and tries to be more
 * cache-efficient by using the heuristics described in:
 *
 *   Peter Sanders, 'Fast Priority Queues for Cached Memory'
 *     http://www.mpi-inf.mpg.de/~sanders/papers/spqjea.ps.gz
 *
 * Sanders did not invent it, but the original description is in a journal
 * not available online.
 *
 * @author duyn
 */
// Final for performance, may be removed if sub-classing necessary
public final class FastBinaryHeap<T> implements Iterable<PrioNode<T>> {
	private final int branches;
	// Direction = -1 or 1
	// root * direction > element * direction
	private final int direction;
	public static enum Direction {
		MIN(-1), MAX(1);

		public final int value;
		private Direction(int value) {
			this.value = value;
		}
	}
	public static final Direction MAX = Direction.MAX, MIN = Direction.MIN;

	private final PrioNode<T>[] data;
	private int size = 0;
	private static final int DEFAULT_N_CHILDREN = 4;

	public FastBinaryHeap(int capacity, Direction direction) {
		this(capacity, DEFAULT_N_CHILDREN, direction);
	}

	@SuppressWarnings("unchecked")
	public FastBinaryHeap(int capacity, int branches, Direction dir) {
		this.branches = branches;
		// This implicit conversion is safe because we only refer to
		// this array through our data member.
		this.data = new PrioNode[capacity];
		this.direction = dir.value;
	}

	public Iterator<PrioNode<T>>
	iterator() {
		return new Iterator<PrioNode<T>>() {

			int idx = 0;

			public boolean
			hasNext() { return idx < size; }

			public PrioNode<T>
			next() { return data[idx++]; }

			public void
			remove() {
				throw new UnsupportedOperationException("Not supported.");
			}

		};
	}

	public int
	size() { return size; }

	public boolean
	offer(double priority, T result) {
		if (size < data.length) {
			// Trivial case
			siftUp(size, new PrioNode<T>(priority, result));
			size++;
			return true;
		}

		if (priority*direction > data[0].priority*direction) {
			System.err.printf("Do not want %f when have %f\n",
				priority, data[0].priority);
			return false;
		}

		siftDown(0, new PrioNode<T>(priority, result));
		return true;
	}

	public PrioNode<T>
	peek() { return data[0]; }

	public PrioNode<T>
	poll() {
		// TODO: bottom-up heuristic
		if (size == 0) return null;

		final PrioNode<T> top = data[0];
		siftDown(0, data[--size]);
		return top;
	}

	private void
	siftUp(int hole, PrioNode<T> value) {
		if (size > 0) {
			final double vPD = value.priority*direction;
			while (hole > 0) {
				final int parent = (hole - 1)/branches;
				//final int parent = (hole - 1) >> branchesPowerOfTwo;
				if (vPD > data[parent].priority*direction) {
					data[hole] = data[parent];
					hole = parent;
				} else {
					break;
				}
			}
		}

		data[hole] = value;
	}

	private void
	siftDown(int hole, PrioNode<T> value) {
		if (size > 0) {
			// Push down along min path
			final double vPD = value.priority*direction;
			int firstChild;
			while ((firstChild = branches*hole + 1) < data.length) {

				// Find largest/smallest child
				double swapPD = data[firstChild].priority*direction;
				int swapChild = firstChild;
				for(int c = firstChild + 1;
					c < firstChild + branches && c < size; c++)
				{
					final double cPD = data[c].priority*direction;
					if (cPD > swapPD) {
						swapPD = cPD;
						swapChild = c;
					}
				}

				if (swapPD > vPD) {
					data[hole] = data[swapChild];
					hole = swapChild;
				} else {
					break;
				}
			}
		}

		data[hole] = value;
	}
}
