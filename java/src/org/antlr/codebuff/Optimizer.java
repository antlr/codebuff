package org.antlr.codebuff;

import java.util.Arrays;
import java.util.function.Function;

public class Optimizer {
	/** Use a simple gradient descent approach to find weights */
	public static double[] minimize(Function<double[], Double> f, double[] x0, double eta, double h, double precision) {
		double[] x = Arrays.copyOf(x0, x0.length);
		double[] prev_x = null;
		while ( true ) {
			prev_x = x;
			double finite_diff = f.apply(vector_scalar_add(x,h)) - f.apply(x);	// division by h rolls into learning rate
			x = vector_scalar_sub(x, eta * finite_diff);                 		// decelerates x jump as it flattens out
			// print "f(%1.12f) = %1.12f" % (x, f(x)),
			double delta = f.apply(x) - f.apply(prev_x);
			// print ", delta = %1.20f" % delta
			// stop when small change in vertical but not heading down
			if ( delta >= 0 && Math.abs(delta) < precision ) {
				break;
			}
		}
		return x;
	}

	public static double[] vector_scalar_add(double[] x, double v) {
		double[] y = new double[x.length];
		for (int i = 0; i<x.length; i++) {
			y[i] = x[i] + v;
		}
		return y;
	}

	public static double[] vector_scalar_sub(double[] x, double v) {
		double[] y = new double[x.length];
		for (int i = 0; i<x.length; i++) {
			y[i] = x[i] - v;
		}
		return y;
	}
}
