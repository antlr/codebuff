package org.antlr.codebuff;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Optimizer {
	public static final long LEARNING_RATE = 10;
	public static final double h = 0.1;
	public static final double PRECISION = 0.0000001; // can't be too small as f(x)-f(xprev) prec is low
	public static double globalMin = Double.MAX_VALUE;
	public static double[] globalBestCombination;

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
		double oldResult = 0;
		while ( true ) {
			prev_x = Arrays.copyOf(x, x.length);

			double[] finite_diff = finiteDifferenceForAllParameter(f, x, h);

			for (int i=0; i<x.length; i++) {
				x[i] = x[i] - eta * finite_diff[i];
			}

			double newResult = f.apply(x);

			double delta = newResult - oldResult;

			System.out.printf("New Result: " + newResult + " New Parameter: ");
			for (int i=0; i<x.length; i++) {
				System.out.printf("%1.3f", x[i]);
				if (i < x.length-1) System.out.printf(", ");
				else System.out.println();
			}

			if ( delta >= 0 && Math.abs(delta) < precision ) {
				System.out.println("This round min result: " + oldResult);
				break;
			}
			oldResult = newResult;
		}
		if (oldResult < globalMin) {
			System.out.println("\n>>>> New global min result: " + oldResult);
			globalMin = oldResult;
			globalBestCombination = Arrays.copyOf(x, x.length);
			for (int i=0; i<x.length; i++) {
				System.out.printf(String.valueOf(x[i]));
				if (i < x.length-1) System.out.printf(", ");
				else System.out.println();
			}
		}
		return x;
	}

	public static void multiRoundMinimize(Function<double[], Double> f, double eta, double h, double precision, FeatureMetaData[] features, int maxRound) {
		int n = features.length;
		for (int i=0; i<features.length; i++) if (features[i] == FeatureMetaData.UNUSED) n--;

		double[] startCombination = new double[n];

		for (int i=0; i<maxRound; i++) {
			for (int j=0; j<n; j++) startCombination[j] = Math.random();
			System.out.println("\n\n>> Round " + (i+1) + " of " + maxRound);
			System.out.printf("Start combination: ");
			for (int j=0; j<startCombination.length; j++) {
				System.out.printf("%1.3f", startCombination[j]);
				if (j < startCombination.length-1) System.out.printf(", ");
				else System.out.println();
			}
			minimize2(f, startCombination, eta, h, precision);
		}
		System.out.println("\n\n>>>>>> All finished");
		System.out.println("Final result: " + globalMin);
		System.out.printf("Best combination: ");
		for (int i=0; i<globalBestCombination.length; i++) {
			System.out.printf(String.valueOf(globalBestCombination[i]));
			if (i < globalBestCombination.length-1) System.out.printf(", ");
			else System.out.println();
		}
	}

	// calculate finite difference for each parameter independently
	public static double[] finiteDifferenceForAllParameter(Function<double[], Double> f, double[] x0, double h) {
		double[] x = Arrays.copyOf(x0, x0.length);
		double oldValue = f.apply(x);
		double[] finiteDifferences = new double[x.length];
		System.out.printf("changed parameter: ");
		boolean changed = false;
		for (int i=0; i<x.length; i++) {
			double[] newX = Arrays.copyOf(x, x.length);
			newX[i] += h;
			double newValue = f.apply(newX);
			if (newValue != oldValue) {
				System.out.printf("%d : %1.3f | ",i, (newValue - oldValue));
				changed = true;
			}
			finiteDifferences[i] = newValue - oldValue;
		}
		if (!changed) System.out.printf(" N/A ");
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
			corpusDir = "../samples/stringtemplate4/org/stringtemplate/v4/debug/";
			testFileDir = "../samples/stringtemplate4/org/stringtemplate/v4/debug/";
		}
		Corpus corpus = Tool.train(corpusDir, ".*\\.java", JavaLexer.class, JavaParser.class, "compilationUnit", tabSize);

		List<String> allFiles = Tool.getFilenames(new File(testFileDir), ".*\\.java");
		ArrayList<InputDocument> documents = (ArrayList<InputDocument>) Tool.load(allFiles, JavaLexer.class, tabSize);

		Tester t = new Tester(CollectFeatures.FEATURES_INJECT_NL, corpus, documents, tabSize);
		// sorry, had to comment this out
//		multiRoundMinimize(Tester::test, LEARNING_RATE, h, PRECISION, CollectFeatures.FEATURES_INJECT_NL, 5);
	}
}
