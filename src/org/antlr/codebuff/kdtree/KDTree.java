/*
 *  Copyright (C) 2010 Duy Nguyen <duyn.ng@gmail.com>.
 */

package org.antlr.codebuff.kdtree;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A basic kd-tree for tutorial purposes.
 *
 * @author duyn
 */
public class KDTree {
	// Basic tree structure
	Exemplar data = null;
	KDTree left = null;
	KDTree right = null;

	// Split point. Dimension is defined when tree is created, value
	// not set until splitting happens.
	int splitDim = 0;
	double split = Double.NaN;

	private int	dimensions() {
		return data.features.length;
	}

	private boolean isTree() {
		return left!=null;
	}

	public void add(Exemplar ex) {
		KDTree tree = this;
		while ( tree!=null ) {
			if ( tree.isTree() ) {
				// Traverse in search of a leaf
				tree = ex.features[tree.splitDim]<=tree.split
					? tree.left : tree.right;
			}
			else {
				if ( tree.data==null ) {
					tree.data = ex;
				}
				else {
					// Split tree and add

					// Find smallest exemplar to be our split point
					final int d = tree.splitDim;
					Exemplar leftX = ex, rightX = tree.data;
					if ( rightX.features[d]<leftX.features[d] ) {
						leftX = tree.data;
						rightX = ex;
					}
					tree.split = 0.5*(leftX.features[d]+rightX.features[d]);

					final int nextSplitDim =
						(tree.splitDim+1)%tree.dimensions();

					tree.left = new KDTree();
					tree.left.splitDim = nextSplitDim;
					tree.left.data = leftX;

					tree.right = new KDTree();
					tree.right.splitDim = nextSplitDim;
					tree.right.data = rightX;
				}

				// Done.
				tree = null;
			}
		}
	}

	Iterable<? extends PrioNode<Exemplar>> search(KDTreeDistanceFunc distanceFunc, int[] query, int k) {
		KDTree tree = this;
		final Queue<PrioNode<Exemplar>> results =
			new PriorityQueue<PrioNode<Exemplar>>(k,
				new Comparator<PrioNode<Exemplar>>() {

				   // min-heap
				   public int
				   compare(PrioNode<Exemplar> o1, PrioNode<Exemplar> o2) {
				       return o1.priority==o2.priority ? 0
				           : o1.priority>o2.priority ? -1
				           : 1;
				   }

				}
			);
		final Deque<KDTree> stack = new LinkedList<>();
		stack.addLast(tree);
		while ( !stack.isEmpty() ) {
			tree = stack.removeLast();

			if ( tree.isTree() ) {
				// Guess nearest tree to query point
				KDTree nearTree = tree.left, farTree = tree.right;
				if ( query[tree.splitDim]>tree.split ) {
					nearTree = tree.right;
					farTree = tree.left;
				}

				// Only search far tree if our search sphere might
				// overlap with splitting plane
				if ( results.size()<k || sq(query[tree.splitDim]-tree.split)
					<=results.peek().priority ) {
					stack.addLast(farTree);
				}

				// Always search the nearest branch
				stack.addLast(nearTree);
			}
			else {
				final double dSq = distanceFunc.distance(query, tree.data.features);
				if ( results.size()<k || dSq<results.peek().priority ) {
					while ( results.size()>=k ) {
						results.poll();
					}

					results.offer(new PrioNode<>(dSq, tree.data));
				}
			}
		}
		return results;
	}

//	double distance(int[] p1, int[] p2) {
//		// Note: profiling shows this is called lots of times, so it pays
//		// to be well optimised
//		double dSq = 0;
//		for (int d = 0; d<p1.length; d++) {
//			final double dst = p1[d]-p2[d];
//			if ( dst!=0 )
//				dSq += dst*dst;
//		}
//		return dSq;
//	}

//	double distance(int[] A, int[] B) {
//		double d = Tool.weightedL0_Distance(FEATURES, A, B);
//		return d/maxDistanceCount;
//	}


	private static double sq(double n) {
		return n*n;
	}
}
