package org.antlr.codebuff.misc;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class BuffUtils {

	public static int indexOf(ParserRuleContext parent, ParseTree child) {
		for (int i = 0; i<parent.getChildCount(); i++) {
			if ( parent.getChild(i)==child ) {
				return i;
			}
		}
		return -1;
	}

	//  Generic filtering, mapping, joining that should be in the standard library but aren't
	public static <T> T findFirst(List<T> data, Predicate<T> pred) {
		if ( data!=null ) for (T x : data) {
			if ( pred.test(x) ) {
				return x;
			}
		}
		return null;
	}

	public static <T> List<T> filter(List<T> data, Predicate<T> pred) {
		List<T> output = new ArrayList<>();
		if ( data!=null ) for (T x : data) {
			if ( pred.test(x) ) {
				output.add(x);
			}
		}
		return output;
	}

	public static <T> List<T> filter(Collection<T> data, Predicate<T> pred) {
		List<T> output = new ArrayList<>();
		for (T x : data) {
			if ( pred.test(x) ) {
				output.add(x);
			}
		}
		return output;
	}

	public static <T, R> List<R> map(Collection<T> data, Function<T, R> getter) {
		List<R> output = new ArrayList<>();
		if ( data!=null ) for (T x : data) {
			output.add(getter.apply(x));
		}
		return output;
	}

	public static <T, R> List<R> map(T[] data, Function<T, R> getter) {
		List<R> output = new ArrayList<>();
		if ( data!=null ) for (T x : data) {
			output.add(getter.apply(x));
		}
		return output;
	}

	public static double variance(List<Integer> data) {
		int n = data.size();
		double sum = 0;
		double avg = sum(data)/((double) n);
		for (int d : data) {
			sum += (d-avg)*(d-avg);
		}
		return sum/n;
	}

	public static double varianceFloats(List<Float> data) {
		int n = data.size();
		double sum = 0;
		double avg = sumFloats(data)/((double) n);
		for (float d : data) {
			sum += (d-avg)*(d-avg);
		}
		return sum/n;
	}

	public static int sum(Collection<Integer> data) {
		int sum = 0;
		for (int d : data) {
			sum += d;
		}
		return sum;
	}

	public static float sumFloats(Collection<Float> data) {
		float sum = 0;
		for (float d : data) {
			sum += d;
		}
		return sum;
	}

	public static float sumDoubles(Collection<Double> data) {
		float sum = 0;
		for (double d : data) {
			sum += d;
		}
		return sum;
	}

	public static List<Double> diffFloats(List<Float> a, List<Float> b) {
		List<Double> diffs = new ArrayList<>();
		for (int i = 0; i<a.size(); i++) {
			diffs.add((double) a.get(i)-b.get(i));
		}
		return diffs;
	}

	public static <T> T median(List<T> data) {
		Collections.sort((List)data);
		int n = data.size();
		return data.get(n/2);
	}

	public static double max(List<Double> data) {
		Collections.sort(data);
		int n = data.size();
		return data.get(n-1);
	}

	public static double min(List<Double> data) {
		Collections.sort(data);
		int n = data.size();
		return data.get(0);
	}

	public static double mean(Collection<Double> data) {
		double sum = 0.0;
		for (Double d : data) {
			sum += d;
		}
		return sum / data.size();
	}
}
