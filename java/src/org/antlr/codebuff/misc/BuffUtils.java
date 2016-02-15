package org.antlr.codebuff.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class BuffUtils {
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

	public static <T,R> List<R> map(Collection<T> data, Function<T,R> getter) {
		List<R> output = new ArrayList<>();
		if ( data!=null ) for (T x : data) {
			output.add(getter.apply(x));
		}
		return output;
	}

	public static <T,R> List<R> map(T[] data, Function<T,R> getter) {
		List<R> output = new ArrayList<>();
		if ( data!=null ) for (T x : data) {
			output.add(getter.apply(x));
		}
		return output;
	}
}
