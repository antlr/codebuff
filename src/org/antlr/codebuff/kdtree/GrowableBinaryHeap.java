/*
 *  Copyright (C) 2010 Duy Nguyen <duyn.ng@gmail.com>.
 */

package org.antlr.codebuff.kdtree;

import java.util.ArrayList;
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
public final class GrowableBinaryHeap<T> implements Iterable<PrioNode<T>> {
	private final int branches;

	// Direction = -1 or 1
	// root * direction > element * direction
	private final int direction;
	public static final Direction MAX = Direction.MAX, MIN = Direction.MIN;
	public static enum Direction {
		MIN(-1), MAX(1);

		public final int value;
		private Direction(int value) {
			this.value = value;
		}
	}

	private final ArrayList<PrioNode<T>> data;

	public GrowableBinaryHeap(int branches, Direction dir) {
		this.branches = branches;
		// This implicit conversion is safe because we only refer to
		// this array through our data member.
		this.data = new ArrayList<PrioNode<T>>();
		this.direction = dir.value;
	}

	public Iterator<PrioNode<T>>
	iterator() {
		return new Iterator<PrioNode<T>>() {

			int idx = 0;

			public boolean
			hasNext() { return idx < data.size(); }

			public PrioNode<T>
			next() { return data.get(idx++); }

			public void
			remove() {
				throw new UnsupportedOperationException("Not supported.");
			}

		};
	}

	public int
	size() { return data.size(); }

	public boolean
	offer(double priority, T result) {
		data.add(null);
		siftUp(data.size() - 1, new PrioNode<T>(priority, result));
		return true;
	}

	public PrioNode<T>
	peek() { return data.get(0); }

	public PrioNode<T>
	poll() {
		if (data.size() == 0) return null;

		final PrioNode<T> top = data.get(0);
		siftDown(0, data.remove(data.size() - 1));
		return top;
	}

	public Object[]
	toArray() {
		return data.toArray();
	}

	private void
	siftUp(int hole, PrioNode<T> value) {
		if (data.size() > 1) {
			final double vPD = value.priority*direction;
			while (hole > 0) {
				final int parent = (hole - 1)/branches;
				//final int parent = (hole - 1) >> branchesPowerOfTwo;
				if (vPD > data.get(parent).priority*direction) {
					data.set(hole, data.get(parent));
					hole = parent;
				} else {
					break;
				}
			}
		}

		data.set(hole, value);
	}

	private void
	siftDown(int hole, PrioNode<T> value) {
		if (data.size() > 1) {
			// Push down along min path
			final double vPD = value.priority*direction;
			int firstChild;
			while ((firstChild = branches*hole + 1) < data.size()) {

				// Find largest/smallest child
				double swapPD = data.get(firstChild).priority*direction;
				int swapChild = firstChild;
				for(int c = firstChild + 1;
					c < firstChild + branches && c < data.size(); c++)
				{
					final double cPD = data.get(c).priority*direction;
					if (cPD > swapPD) {
						swapPD = cPD;
						swapChild = c;
					}
				}

				if (swapPD > vPD) {
					data.set(hole, data.get(swapChild));
					hole = swapChild;
				} else {
					break;
				}
			}
		}

		data.set(hole, value);
	}
}
