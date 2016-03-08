package org.antlr.codebuff.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HashBag<T> implements Map<T, Integer> {
	protected Map<T, MutableInt> data = new HashMap<>();

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	@Override
	public Integer get(Object key) {
		MutableInt I = data.get(key);
		if ( I!=null ) return I.asInt();
		return null;
	}

	@Override
	public Integer put(T key, Integer value) {
		data.put(key, new MutableInt(value));
		return null; // violates Map<> contract
	}

	public Integer add(T key) {
		MutableInt I = data.get(key);
		if ( I==null ) {
			data.put(key, new MutableInt(1));
		}
		else {
			I.inc();
		}
		return get(key);
	}

	@Override
	public Integer remove(Object key) {
		Integer I = get(key);
		data.remove(key);
		return I;
	}

	@Override
	public void putAll(Map<? extends T, ? extends Integer> m) {
		for (T key : m.keySet()) {
			put(key, m.get(key));
		}
	}

	@Override
	public Set<T> keySet() {
		return data.keySet();
	}

	@Override
	public Collection<Integer> values() {
		List<Integer> v = new ArrayList<>();
		for (MutableInt I : data.values()) {
			v.add(I.asInt());
		}
		return v;
	}

	@Override
	public Set<Entry<T, Integer>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return data.toString();
	}

	public T getMostFrequent() {
		T t = null;
		int max = 0;
		for (T key : data.keySet()) {
			MutableInt count = data.get(key);
			if ( count.asInt()>max ) {
				max = count.asInt();
				t = key;
			}
		}
		return t;
	}
}
