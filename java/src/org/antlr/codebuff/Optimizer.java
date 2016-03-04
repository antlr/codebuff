package org.antlr.codebuff;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Optimizer {
	public static final int LEARNING_RATE = 1000000;
	public static final double h = 0.0001;
	public static final double PRECISION = 0.0000001; // can't be too small as f(x)-f(xprev) prec is low

	/** Use a simple gradient descent approach to find weights */
	/*
	LEARNING_RATE = 10
	h = 0.0001
	PRECISION = 0.0000001 # can't be too small as f(x)-f(xprev) prec is low
	def f(x): return np.cos(3*np.pi*x) / x
	x0s = [runif_(.1,1.2), runif_(.1,1.2)] # random starting positions
	tracex = minimize(f, x0s[0], LEARNING_RATE, h, PRECISION)
	 */
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

	public static double[] minimize2(Function<double[], Double> f, double[] x0, double eta, double h, double precision) {
		double[] x = Arrays.copyOf(x0, x0.length);
		double[] prev_x = null;
		while ( true ) {
			prev_x = x;

			double[] finite_diff = finiteDifferenceForAllParameter(f, x, h);

			for (int i=0; i<x.length; i++) {
				x[i] = x[i] - eta * finite_diff[i];
			}

			double oldValue = f.apply(prev_x);
			double newValue = f.apply(x);

			// print "f(%1.12f) = %1.12f" % (x, f(x)),
			double delta = newValue - oldValue;

			System.out.printf("New Result: " + newValue + " New Parameter: ");
			for (int i=0; i<x.length; i++) System.out.printf(x[i] + ", ");
			// print ", delta = %1.20f" % delta
			// stop when small change in vertical but not heading down
			if ( delta >= 0 && Math.abs(delta) < precision ) {
				break;
			}
		}
		return x;
	}

	// calculate finite difference for each parameter independently
	public static double[] finiteDifferenceForAllParameter(Function<double[], Double> f, double[] x, double h) {
		double[] finiteDifferences = new double[x.length];
		for (int i=0; i<x.length; i++) {
			double[] newX = x;
			newX[i] += h;
			System.out.printf(i + " ");
			finiteDifferences[i] = f.apply(newX) - f.apply(x);
		}
		return finiteDifferences;
	}

	public static double cost(double[] parameters) { return 0.0; }

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

	public static void main(String[] args) throws Exception {
//		minimize(Optimizer::cost, x0s[0], LEARNING_RATE, h, PRECISION);
		int tabSize = 4;
		String corpusDir;
		String testFileDir;
		if ( args.length==2 ) {
			corpusDir = args[0];
			testFileDir = args[1];
		}
		else {
			corpusDir = "../samples/stringtemplate4/org/stringtemplate/v4/compiler/";
			testFileDir = "../samples/stringtemplate4/org/stringtemplate/v4/compiler/";
		}
		Corpus corpus = Tool.train(corpusDir, JavaLexer.class, JavaParser.class, tabSize);

		List<String> allFiles = Tool.getFilenames(new File(testFileDir), ".*\\.java");
		ArrayList<InputDocument> documents = (ArrayList<InputDocument>) Tool.load(allFiles, JavaLexer.class, tabSize);

		double[] originalParameters = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		Tester t = new Tester(CollectFeatures.FEATURES, corpus, documents, tabSize);
		minimize2(Tester::test, originalParameters, LEARNING_RATE, h, PRECISION);
	}
}
