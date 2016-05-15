package org.antlr.codebuff.rednaxela_kdtree;

public interface DistanceFunction {
	public double distance(double[] p1, double[] p2);

	public double distanceToRect(double[] point, double[] min, double[] max);
}

