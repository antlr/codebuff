/*
 *  Copyright (C) 2010 Duy Nguyen <duyn.ng@gmail.com>.
 */

package org.antlr.codebuff.kdtree;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * A k-dimensional binary partitioning tree which splits space on the
 * mean of the dimension with the largest variance. Points are held in
 * buckets so we can pick a better split point than whatever comes first.
 *
 * Does not store tree depth. If you want balance, re-build the tree
 * periodically.
 *
 * Optimisations in this tree assume distance metric is euclidian distance.
 * May work if retrofitted with other metrics, but that is purely
 * accidental.
 *
 * Note: results can become unpredictable if values are different but so
 * close together that rounding errors in computing their mean result in
 * all data being on one side of the mean. Performance degrades when
 * this occurs. Nearest neighbour search tested to work up to range
 * [1, 1 + 5e-16).
 *
 * Ideas for path ordering and bounds-overlap-ball come from:
 *   NEAL SAMPLE, MATTHEW HAINES, MARK ARNOLD, TIMOTHY PURCELL,
 *     'Optimizing Search Strategies in k-d Trees'
 *     http://ilpubs.stanford.edu:8090/723/
 *
 * Actual path ordering is based on split value, which is cheaper than
 * full distance calculation.
 *
 * Computation of variance from:
 *   John Cook, 'Accurately computing running variance'
 *     http://www.johndcook.com/standard_deviation.html
 *
 * Terminology note: points are called Exemplars. They must all be
 * descended from Exemplar class. Position in k-d space is stored in each
 * exemplar's domain member. This is to avoid conflicting with already
 * existing classes referring to geometric points.
 *
 * Terminology comes from:
 *   Andrew Moore, 'An intoductory tutorial on kd-trees'
 *     http://www.autonlab.org/autonweb/14665
 *
 * @author duyn
 */
public final class FastKdTree<X extends Exemplar> {
	final Queue<X> data;
	FastKdTree<X> left = null, right = null;

	// These aren't initialised until add() is called.
	private double[] exMean = null, exSumSqDev = null;

	// Optimisation when sub-tree contains only duplicates
	private boolean singularity = true;

	// Number of exemplars to hold in a leaf before splitting
	private final int bucketSize;

	// Split properties. Not initialised until split occurs.
	private int splitDim = 0;
	private double split = Double.NaN;

	// Optimisation for searches. This lets us skip a node if its
	// scope intersects with a search hypersphere but it doesn't contain
	// any points that actually intersect.
	private double[] contentMax = null, contentMin = null;

	private static final int DEFAULT_BUCKET_SIZE = 10;

	public FastKdTree() {
		this(DEFAULT_BUCKET_SIZE);
	}

	public FastKdTree(int bucketSize) {
		this.bucketSize = bucketSize;
		this.data = new ArrayDeque<X>();
	}

	//
	// PUBLIC METHODS
	//

	public void
	add(X ex) {
		FastKdTree<X> tree = addNoSplit(this, ex);
		if (shouldSplit(tree)) {
			split(tree);
		}
	}

	public void
	addAll(Collection<X> exs) {
		// Some spurious function calls. Optimised for readability over
		// efficiency.
		final Set<FastKdTree<X>> modTrees =
			new HashSet<FastKdTree<X>>();
		for(X ex : exs) {
			modTrees.add(addNoSplit(this, ex));
		}

		for(FastKdTree<X> tree : modTrees) {
			if (shouldSplit(tree)) {
				split(tree);
			}
		}
	}

	public Iterable<PrioNode<X>>
	search(double[] query, int nResults) {
		// Forward to a static method to avoid accidental reference to
		// instance variables while descending the tree
		return search(this, query, nResults);
	}

	//
	// IMPLEMENTATION DETAILS
	//

	private final boolean
	isTree() { return left != null; }

	private int
	dimensions() { return contentMax.length; }

	// Addition

	// Adds an exemplar without splitting overflowing leaves.
	// Returns leaf to which exemplar was added.
	private static <X extends Exemplar> FastKdTree<X>
	addNoSplit(FastKdTree<X> tree, X ex) {
		// Some spurious function calls. Optimised for readability over
		// efficiency.
		FastKdTree<X> cursor = tree;
		while (cursor != null) {
			updateBounds(cursor, ex);
			if (cursor.isTree()) {
				// Sub-tree
				cursor = ex.domain[cursor.splitDim] <= cursor.split
					? cursor.left : cursor.right;
			} else {
				// Leaf

				// Add exemplar to leaf
				cursor.data.add(ex);

				// Calculate running mean and sum of squared deviations
				final int nExs = cursor.data.size();
				final int dims = cursor.dimensions();
				if (nExs == 1) {
					cursor.exMean = Arrays.copyOf(ex.domain, dims);
					cursor.exSumSqDev = new double[dims];
				} else {
					for(int d = 0; d < dims; d++) {
						final double coord = ex.domain[d];
						final double oldMean = cursor.exMean[d], newMean;
						cursor.exMean[d] = newMean =
							oldMean + (coord - oldMean)/nExs;
						cursor.exSumSqDev[d] = cursor.exSumSqDev[d]
							+ (coord - oldMean)*(coord - newMean);
					}
				}

				// Check that data are still uniform
				if (cursor.singularity) {
					final Queue<X> cExs = cursor.data;
					if (cExs.size() > 0 && !ex.collocated(cExs.peek()))
						cursor.singularity = false;
				}

				// Finished walking
				return cursor;
			}
		}
		return null;
	}

	private static <X extends Exemplar> void
	updateBounds(FastKdTree<X> tree, Exemplar ex) {
		final int dims = ex.domain.length;
		if (tree.contentMax == null) {
			tree.contentMax = Arrays.copyOf(ex.domain, dims);
			tree.contentMin = Arrays.copyOf(ex.domain, dims);
		} else {
			for(int d = 0; d < dims; d++) {
				final double dimVal = ex.domain[d];
				if (dimVal > tree.contentMax[d])
					tree.contentMax[d] = dimVal;
				else if (dimVal < tree.contentMin[d])
					tree.contentMin[d] = dimVal;
			}
		}
	}

	// Splitting (internal operation)

	private static <X extends Exemplar> boolean
	shouldSplit(FastKdTree<X> tree) {
		return tree.data.size() > tree.bucketSize
			&& !tree.singularity;
	}

	@SuppressWarnings("unchecked") private static <X extends Exemplar> void
	split(FastKdTree<X> tree) {
		assert !tree.singularity;
		// Find dimension with largest variance to split on
		double largestVar = -1;
		int splitDim = 0;
		for(int d = 0; d < tree.dimensions(); d++) {
			// Don't need to divide by number of data to find largest
			// variance
			final double var = tree.exSumSqDev[d];
			if (var > largestVar) {
				largestVar = var;
				splitDim = d;
			}
		}

		// Find mean as position for our split
		double splitValue = tree.exMean[splitDim];

		// Check that our split actually splits our data. This also lets
		// us bulk load data into sub-trees, which is more likely
		// to keep optimal balance.
		final Queue<X> leftExs = new ArrayDeque<X>();
		final Queue<X> rightExs = new ArrayDeque<X>();
		for(X s : tree.data) {
			if (s.domain[splitDim] <= splitValue)
				leftExs.add(s);
			else
				rightExs.add(s);
		}
		int leftSize = leftExs.size();
		final int treeSize = tree.data.size();
		if (leftSize == treeSize || leftSize == 0) {
			System.err.println(
				"WARNING: Randomly splitting non-uniform tree");
			// We know the data aren't all the same, so try picking
			// an exemplar and a dimension at random for our split point

			// This might take several tries, so we copy our data to
			// an array to speed up process of picking a random point
			Object[] exs = tree.data.toArray();
			while (leftSize == treeSize || leftSize == 0) {
				leftExs.clear();
				rightExs.clear();

				splitDim = (int)
					Math.floor(Math.random()*tree.dimensions());
				final int splitPtIdx = (int)
					Math.floor(Math.random()*exs.length);
				// Cast is inevitable consequence of java's inability to
				// create a generic array
				splitValue = ((X)exs[splitPtIdx]).domain[splitDim];
				for(X s : tree.data) {
					if (s.domain[splitDim] <= splitValue)
						leftExs.add(s);
					else
						rightExs.add(s);
				}
				leftSize = leftExs.size();
			}
		}

		// We have found a valid split. Start building our sub-trees
		final FastKdTree<X> left = new FastKdTree<X>(tree.bucketSize);
		final FastKdTree<X> right = new FastKdTree<X>(tree.bucketSize);
		left.addAll(leftExs);
		right.addAll(rightExs);

		// Finally, commit the split
		tree.splitDim = splitDim;
		tree.split = splitValue;
		tree.left = left;
		tree.right = right;

		// Let go of data (and their running stats) held in this leaf
		tree.data.clear();
		tree.exMean = tree.exSumSqDev = null;
	}

	// Searching

	// May return more results than requested if multiple data have
	// same distance from target.
	//
	// Note: this function works with squared distances to avoid sqrt()
	// operations
	private static <X extends Exemplar> Iterable<PrioNode<X>>
	search(FastKdTree<X> tree, double[] query, int nResults) {
		final SearchState<X> state = new SearchState<X>(nResults);
		final Deque<SearchStackEntry<X>> stack =
			new ArrayDeque<SearchStackEntry<X>>();
		if (tree.contentMin != null)
			stack.addLast(new SearchStackEntry<X>(false, tree));
TREE_WALK:
		while (!stack.isEmpty()) {
			final SearchStackEntry<X> entry = stack.removeLast();
			final FastKdTree<X> cur = entry.tree;

			if (entry.needBoundsCheck && state.results.size() >= nResults) {
				final double d = minDistanceSqFrom(query,
					cur.contentMin, cur.contentMax);
				if (d > state.results.peek().priority)
					continue TREE_WALK;
			}

			if (cur.isTree()) {
				searchTree(query, cur, stack);
			} else {
				searchLeaf(query, cur, state);
			}
		}

		return state.results;
	}

	private static <X extends Exemplar> void
	searchTree(double[] query, FastKdTree<X> tree,
		Deque<SearchStackEntry<X>> stack)
	{
		FastKdTree<X> nearTree = tree.left, farTree = tree.right;
		if (query[tree.splitDim] > tree.split) {
			nearTree = tree.right;
			farTree = tree.left;
		}

		// These variables let us skip empty sub-trees
		boolean nearEmpty = nearTree.contentMin == null;
		boolean farEmpty = farTree.contentMin == null;

		// Add nearest sub-tree to stack later so we descend it
		// first. This is likely to constrict our max distance
		// sooner, resulting in less visited nodes
		if (!farEmpty) {
			stack.addLast(new SearchStackEntry<X>(true, farTree));
		}

		if (!nearEmpty) {
			stack.addLast(new SearchStackEntry<X>(true, nearTree));
		}
	}

	private static <X extends Exemplar> void
	searchLeaf(double[] query, FastKdTree<X> leaf, SearchState<X> state) {
		double exD = Double.NaN;
		for(X ex : leaf.data) {
			if (!leaf.singularity || Double.isNaN(exD)) {
				exD = distanceSqFrom(query, ex.domain);
			}

			if (examine(exD, state)) {
				state.results.offer(exD, ex);
			}
		}
	}

	private static <X extends Exemplar> boolean
	examine(double distance, SearchState<X> state) {
		return state.results.size() < state.nResults
			|| distance < state.results.peek().priority;
	}

	// Distance calculations

	// Gets distance from target of nearest point on hyper-rect defined
	// by supplied min and max bounds
	private static double
	minDistanceSqFrom(double[] target, double[] min, double[] max) {
		// Note: profiling shows this is called lots of times, so it pays
		// to be well optimised
		double distanceSq = 0;
		for(int d = 0; d < target.length; d++) {
			if (target[d] < min[d]) {
				final double dst = min[d] - target[d];
				distanceSq += dst*dst;
			} else if (target[d] > max[d]) {
				final double dst = max[d] - target[d];
				distanceSq += dst*dst;
			}
		}
		return distanceSq;
	}

	private static double
	distanceSqFrom(double[] p1, double[] p2) {
		// Note: profiling shows this is called lots of times, so it pays
		// to be well optimised
		double dSq = 0;
		for(int d = 0; d < p1.length; d++) {
			final double dst = p1[d] - p2[d];
			if (dst != 0)
				dSq += dst*dst;
		}
		return dSq;
	}

	//
	// class SearchStackEntry
	//

	private static class SearchStackEntry<X extends Exemplar> {
		public final boolean needBoundsCheck;
		public final FastKdTree<X> tree;

		public SearchStackEntry(boolean needBoundsCheck,
			FastKdTree<X> tree)
		{
			this.needBoundsCheck = needBoundsCheck;
			this.tree = tree;
		}
	}

	//
	// class SearchState
	//
	// Holds data about current state of the search. Used for live updating
	// of pruning distance.

	private static class SearchState<X extends Exemplar> {
		final int nResults;
		final FastBinaryHeap<X> results;

		public SearchState(int nResults) {
			this.nResults = nResults;
			results = new FastBinaryHeap<X>(
				nResults, 4, FastBinaryHeap.MAX);
		}
	}
}
