package org.antlr.codebuff.rednaxela_kdtree;

import java.util.Arrays;
import java.util.Iterator;

/**
 *
 */
public class NearestNeighborIterator<T> implements Iterator<T>, Iterable<T> {
	private DistanceFunction distanceFunction;
	private double[] searchPoint;
	private MinHeap<KdNode<T>> pendingPaths;
	private IntervalHeap<T> evaluatedPoints;
	private int pointsRemaining;
	private double lastDistanceReturned;

	protected NearestNeighborIterator(KdNode<T> treeRoot, double[] searchPoint, int maxPointsReturned, DistanceFunction distanceFunction) {
		this.searchPoint = Arrays.copyOf(searchPoint, searchPoint.length);
		this.pointsRemaining = Math.min(maxPointsReturned, treeRoot.size());
		this.distanceFunction = distanceFunction;
		this.pendingPaths = new BinaryHeap.Min<KdNode<T>>();
		this.pendingPaths.offer(0, treeRoot);
		this.evaluatedPoints = new IntervalHeap<T>();
	}

    /* -------- INTERFACE IMPLEMENTATION -------- */

	@Override
	public boolean hasNext() {
		return pointsRemaining>0;
	}

	@Override
	public T next() {
		if ( !hasNext() ) {
			throw new IllegalStateException("NearestNeighborIterator has reached end!");
		}

		while ( pendingPaths.size()>0 && (evaluatedPoints.size()==0 || (pendingPaths.getMinKey()<evaluatedPoints.getMinKey())) ) {
			KdTree.nearestNeighborSearchStep(pendingPaths, evaluatedPoints, pointsRemaining, distanceFunction, searchPoint);
		}

		// Return the smallest distance point
		pointsRemaining--;
		lastDistanceReturned = evaluatedPoints.getMinKey();
		T value = evaluatedPoints.getMin();
		evaluatedPoints.removeMin();
		return value;
	}

	public double distance() {
		return lastDistanceReturned;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}
}
