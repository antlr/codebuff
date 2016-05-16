/*
 * Copyright 2001-2005 Daniel F. Savarese
 * Copyright 2006-2009 Savarese Software Research Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.savarese.com/software/ApacheLicense-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antlr.codebuff.kdtree;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

// All the view classes are inefficient for anything other than iteration.

/**
 * <p>A k-d tree divides a k-dimensional space relative to the points it
 * contains by storing them in a binary tree, discriminating by a
 * different dimension at each level of the tree.  It allows efficient
 * point data retrieval (<em>O(lg(n))</em>) and range searching.</p>
 * <p>
 * <p>KDTree conforms to the java.util.Map interface except that
 * Iterator.remove is not supported by the returned views.</p>
 */
public class KDTree<P extends Exemplar, V> implements RangeSearchTree<P, V> {
	final class KDNode implements Map.Entry<P, V> {
		int _discriminator;
		P _point;
		V _value;
		KDNode _low, _high;

		KDNode(int discriminator, P point, V value) {
			_point = point;
			_value = value;
			_low = _high = null;
			_discriminator = discriminator;
		}

		public boolean equals(Object o) {
			KDNode node = (KDNode) o;

			if ( node==this ) {
				return true;
			}

			return
				((getKey()==null ?
					node.getKey()==null : getKey().equals(node.getKey())) &&
					(getValue()==null ?
						node.getValue()==null : getValue().equals(node.getValue())));
		}

		public P getKey() {
			return _point;
		}

		public V getValue() {
			return _value;
		}

		// Only call if the node is in the tree.
		public V setValue(V value) {
			V old = _value;
			_hashCode -= hashCode();
			_value = value;
			_hashCode += hashCode();
			return old;
		}

		public int hashCode() {
			return
				((getKey()==null ? 0 : getKey().hashCode())^
					(getValue()==null ? 0 : getValue().hashCode()));
		}
	}

	final class MapEntryIterator implements Iterator<Map.Entry<P, V>> {
		LinkedList<KDNode> _stack;
		KDNode _next;
		P _lower, _upper;

		MapEntryIterator(P lower, P upper) {
			_stack = new LinkedList<KDNode>();
			_lower = lower;
			_upper = upper;
			_next = null;

			if ( _root!=null )
				_stack.addLast(_root);
			next();
		}

		MapEntryIterator() {
			this(null, null);
		}

		public boolean hasNext() {
			return (_next!=null);
		}

		public Map.Entry<P, V> next() {
			KDNode old = _next;

			while ( !_stack.isEmpty() ) {
				KDNode node = _stack.removeLast();
				int discriminator = node._discriminator;

				if ( (_upper==null ||
					 Integer.compare(node._point.getCoord(discriminator),
								     _upper.getCoord(discriminator))<=0) &&
					node._high!=null ) {
					_stack.addLast(node._high);
				}

				if ( (_lower==null ||
					 Integer.compare(node._point.getCoord(discriminator),
						     		 _lower.getCoord(discriminator))>0) &&
					node._low!=null ) {
					_stack.addLast(node._low);
				}

				if ( isInRange(node._point, _lower, _upper) ) {
					_next = node;
					return old;
				}
			}

			_next = null;

			return old;
		}

		// This violates the contract for entrySet, but we can't support
		// in a reasonable fashion the removal of mappings through the iterator.
		// Java iterators require a hasNext() function, which forces the stack
		// to reflect a future search state, making impossible to adjust the current
		// stack after a removal.  Implementation alternatives are all too
		// expensive.  Yet another reason to favor the C++ implementation...
		public void remove()
			throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	final class KeyIterator implements Iterator<P> {
		MapEntryIterator iterator;

		KeyIterator(MapEntryIterator it) {
			iterator = it;
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public P next() {
			Map.Entry<P, V> next = iterator.next();
			return (next==null ? null : next.getKey());
		}

		public void remove()
			throws UnsupportedOperationException {
			iterator.remove();
		}
	}

	final class ValueIterator implements Iterator<V> {
		MapEntryIterator iterator;

		ValueIterator(MapEntryIterator it) {
			iterator = it;
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public V next() {
			Map.Entry<P, V> next = iterator.next();
			return (next==null ? null : next.getValue());
		}

		public void remove()
			throws UnsupportedOperationException {
			iterator.remove();
		}
	}

	abstract class CollectionView<E> implements Collection<E> {

		public boolean add(E o)
			throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection<? extends E> c)
			throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		public void clear() {
			KDTree.this.clear();
		}

		public boolean containsAll(Collection<?> c) {
			for (Object o : c) {
				if ( !contains(o) )
					return false;
			}
			return true;
		}

		public int hashCode() {
			return KDTree.this.hashCode();
		}

		public boolean isEmpty() {
			return KDTree.this.isEmpty();
		}

		public int size() {
			return KDTree.this.size();
		}

		public Object[] toArray() {
			Object[] obja = new Object[size()];
			int i = 0;

			for (E e : this) {
				obja[i] = e;
				++i;
			}

			return obja;
		}

		public <T> T[] toArray(T[] a) {
			Object[] array = a;

			if ( array.length<size() )
				array = a =
					(T[]) Array.newInstance(a.getClass().getComponentType(), size());

			if ( array.length>size() )
				array[size()] = null;

			int i = 0;
			for (E e : this) {
				array[i] = e;
				++i;
			}

			return a;
		}
	}

	abstract class SetView<E> extends CollectionView<E> implements Set<E> {
		public boolean equals(Object o) {
			if ( !(o instanceof Set) )
				return false;

			if ( o==this )
				return true;

			Set<?> set = (Set<?>) o;

			if ( set.size()!=size() )
				return false;

			try {
				return containsAll(set);
			} catch (ClassCastException cce) {
				return false;
			}
		}
	}

	final class MapEntrySet extends SetView<Map.Entry<P, V>> {
		public boolean contains(Object o)
			throws ClassCastException, NullPointerException {
			Map.Entry<P, V> e = (Map.Entry<P, V>) o;
			KDNode node = getNode(e.getKey());

			if ( node==null )
				return false;

			return e.getValue().equals(node.getValue());
		}

		public Iterator<Map.Entry<P, V>> iterator() {
			return new MapEntryIterator();
		}

		public boolean remove(Object o)
			throws ClassCastException {
			int size = size();
			Map.Entry<P, V> e = (Map.Entry<P, V>) o;

			KDTree.this.remove(e.getKey());

			return (size!=size());
		}

		public boolean removeAll(Collection<?> c)
			throws ClassCastException {
			int size = size();

			for (Object o : c) {
				Map.Entry<P, V> e = (Map.Entry<P, V>) o;
				KDTree.this.remove(e.getKey());
			}

			return (size!=size());
		}

		public boolean retainAll(Collection<?> c)
			throws ClassCastException {
			for (Object o : c) {
				if ( contains(o) ) {
					Collection<Map.Entry<P, V>> col = (Collection<Map.Entry<P, V>>) c;
					clear();
					for (Map.Entry<P, V> e : col)
						put(e.getKey(), e.getValue());
					return true;
				}
			}
			return false;
		}
	}

	final class KeySet extends SetView<P> {

		public boolean contains(Object o)
			throws ClassCastException, NullPointerException {
			return KDTree.this.containsKey(o);
		}

		public Iterator<P> iterator() {
			return new KeyIterator(new MapEntryIterator());
		}

		public boolean remove(Object o)
			throws ClassCastException {
			int size = size();
			KDTree.this.remove(o);
			return (size!=size());
		}

		public boolean removeAll(Collection<?> c)
			throws ClassCastException {
			int size = size();

			for (Object o : c)
				KDTree.this.remove(o);

			return (size!=size());
		}

		public boolean retainAll(Collection<?> c)
			throws ClassCastException {
			HashMap<P, V> map = new HashMap<P, V>();
			int size = size();

			for (Object o : c) {
				V val = get(o);

				if ( val!=null || contains(o) )
					map.put((P) o, val);
			}

			clear();
			putAll(map);

			return (size!=size());
		}
	}

	final class ValueCollection extends CollectionView<V> {

		public boolean contains(Object o)
			throws ClassCastException, NullPointerException {
			return KDTree.this.containsValue(o);
		}

		public Iterator<V> iterator() {
			return new ValueIterator(new MapEntryIterator());
		}

		public boolean remove(Object o)
			throws ClassCastException {
			KDNode node = findValue(_root, o);

			if ( node!=null ) {
				KDTree.this.remove(node.getKey());
				return true;
			}

			return false;
		}

		public boolean removeAll(Collection<?> c)
			throws ClassCastException {
			int size = size();

			for (Object o : c) {
				KDNode node = findValue(_root, o);

				while ( node!=null ) {
					KDTree.this.remove(o);
					node = findValue(_root, o);
				}
			}

			return (size!=size());
		}

		public boolean retainAll(Collection<?> c)
			throws ClassCastException {
			HashMap<P, V> map = new HashMap<P, V>();
			int size = size();

			for (Object o : c) {
				KDNode node = findValue(_root, o);

				while ( node!=null ) {
					map.put(node.getKey(), node.getValue());
					node = findValue(_root, o);
				}
			}

			clear();
			putAll(map);

			return (size!=size());
		}
	}

	int _size, _hashCode, _dimensions;
	KDNode _root;

	KDNode getNode(P point, KDNode[] parent) {
		int discriminator;
		KDNode node = _root, current, last = null;
		int c1, c2;

		while ( node!=null ) {
			discriminator = node._discriminator;
			c1 = point.getCoord(discriminator);
			c2 = node._point.getCoord(discriminator);
			current = node;

			if ( Integer.compare(c1,c2)>0 ) {
				node = node._high;
			}
			else if ( Integer.compare(c1,c2)<0 ) {
				node = node._low;
			}
			else if ( node._point.equals(point) ) {
				if ( parent!=null ) {
					parent[0] = last;
				}
				return node;
			}
			else
				node = node._high;

			last = current;
		}

		if ( parent!=null )
			parent[0] = last;

		return null;
	}

	KDNode getNode(P point) {
		return getNode(point, null);
	}

	KDNode getMinimumNode(KDNode node, KDNode p, int discriminator,
	                      KDNode[] parent) {
		KDNode result;

		if ( discriminator==node._discriminator ) {
			if ( node._low!=null ) {
				return getMinimumNode(node._low, node, discriminator, parent);
			}
			else {
				result = node;
			}
		}
		else {
			KDNode nlow = null, nhigh = null;
			KDNode[] plow = new KDTree.KDNode[1], phigh = new KDTree.KDNode[1];

			if ( node._low!=null )
				nlow = getMinimumNode(node._low, node, discriminator, plow);

			if ( node._high!=null )
				nhigh = getMinimumNode(node._high, node, discriminator, phigh);

			if ( nlow!=null && nhigh!=null ) {
				if ( Integer.compare(nlow._point.getCoord(discriminator),nhigh._point.getCoord(discriminator))<0 ) {
					result = nlow;
					parent[0] = plow[0];
				}
				else {
					result = nhigh;
					parent[0] = phigh[0];
				}
			}
			else if ( nlow!=null ) {
				result = nlow;
				parent[0] = plow[0];
			}
			else if ( nhigh!=null ) {
				result = nhigh;
				parent[0] = phigh[0];
			}
			else {
				result = node;
			}
		}

		if ( result==node ) {
			parent[0] = p;
		}
		else if ( Integer.compare(node._point.getCoord(discriminator),result._point.getCoord(discriminator))<0 ) {
			result = node;
			parent[0] = p;
		}

		return result;
	}

	KDNode recursiveRemoveNode(KDNode node) {
		int discriminator;

		if ( node._low==null && node._high==null )
			return null;
		else
			discriminator = node._discriminator;

		if ( node._high==null ) {
			node._high = node._low;
			node._low = null;
		}

		KDNode[] parent = new KDTree.KDNode[1];
		KDNode newRoot =
			getMinimumNode(node._high, node, discriminator, parent);
		KDNode child = recursiveRemoveNode(newRoot);

		if ( parent[0]._low==newRoot )
			parent[0]._low = child;
		else
			parent[0]._high = child;

		newRoot._low = node._low;
		newRoot._high = node._high;
		newRoot._discriminator = node._discriminator;

		return newRoot;
	}

	KDNode findValue(KDNode node, Object value) {
		if ( node==null || (value==null ? node.getValue()==null :
			value.equals(node.getValue())) )
			return node;

		KDNode result;

		if ( (result = findValue(node._low, value))==null )
			result = findValue(node._high, value);

		return result;
	}

	boolean isInRange(P point, P lower, P upper) {
		Integer coordinate1, coordinate2 = null, coordinate3 = null;

		if ( lower!=null || upper!=null ) {
			int dimensions;
			dimensions = point.getDimensions();

			for (int i = 0; i<dimensions; ++i) {
				coordinate1 = point.getCoord(i);
				if ( lower!=null )
					coordinate2 = lower.getCoord(i);
				if ( upper!=null )
					coordinate3 = upper.getCoord(i);
				if ( (coordinate2!=null && coordinate1.compareTo(coordinate2)<0) ||
					(coordinate3!=null && coordinate1.compareTo(coordinate3)>0) )
					return false;
			}
		}

		return true;
	}

	/**
	 * Creates a two-dimensional KDTree.
	 */
	public KDTree() {
		this(2);
	}

	/**
	 * Creates a KDTree of the specified number of dimensions.
	 *
	 * @param dimensions The number of dimensions.  Must be greater than 0.
	 */
	public KDTree(int dimensions) {
		assert (dimensions>0);
		_dimensions = dimensions;
		clear();
	}

	// Begin Map interface methods

	/**
	 * Removes all elements from the container, leaving it empty.
	 */
	public void clear() {
		_root = null;
		_size = _hashCode = 0;
	}

	/**
	 * Returns true if the container contains a mapping for the specified key.
	 *
	 * @param key The point key to search for.
	 * @return true if the container contains a mapping for the specified key.
	 * @throws ClassCastException if the key is not an instance of P.
	 */
	public boolean containsKey(Object key)
		throws ClassCastException {
		return (getNode((P) key)!=null);
	}

	/**
	 * Returns true if the container contains a mapping with the specified value.
	 * Note: this is very inefficient for KDTrees because it requires searching
	 * the entire tree.
	 *
	 * @param value The value to search for.
	 * @return true If the container contains a mapping with the specified value.
	 */
	public boolean containsValue(Object value) {
		return (findValue(_root, value)!=null);
	}

	/**
	 * Returns a Set view of the point to value mappings in the KDTree.
	 * Modifications to the resulting set will be reflected in the KDTree
	 * and vice versa, except that {@code Iterator.remove} is not supported.
	 *
	 * @return A Set view of the point to value mappings in the KDTree.
	 */
	public Set<Map.Entry<P, V>> entrySet() {
		return new MapEntrySet();
	}

	/**
	 * Returns true if the object contains the same mappings, false if not.
	 *
	 * @param o The object to test for equality.
	 * @return true if the object contains the same mappings, false if not.
	 */
	public boolean equals(Object o)
		throws ClassCastException {
		if ( !(o instanceof Map) )
			return false;

		if ( o==this )
			return true;

		Map map = (Map) o;

		return (entrySet().equals(map.entrySet()));
	}

	/**
	 * Retrieves the value at the given location.
	 *
	 * @param point The location from which to retrieve the value.
	 * @return The value at the given location, or null if no value is present.
	 * @throws ClassCastException If the given point is not of the
	 *                            expected type.
	 */
	public V get(Object point) throws ClassCastException {
		KDNode node = getNode((P) point);

		return (node==null ? null : node.getValue());
	}

	/**
	 * Returns the hash code value for this map.
	 *
	 * @return The sum of the hash codes of all of the map entries.
	 */
	public int hashCode() {
		return _hashCode;
	}

	/**
	 * Returns true if the container has no elements, false if it
	 * contains one or more elements.
	 *
	 * @return true if the container has no elements, false if it
	 * contains one or more elements.
	 */
	public boolean isEmpty() {
		return (_root==null);
	}

	/**
	 * Returns a Set view of the point keys for the mappings in the
	 * KDTree.  Changes to the Set are reflected in the KDTree and vice
	 * versa, except that {@code Iterator.remove} is not supported.
	 *
	 * @return A Set view of the point keys for the mappings in the KDTree.
	 */
	public Set<P> keySet() {
		return new KeySet();
	}

	/**
	 * Inserts a point value pair into the tree, preserving the
	 * spatial ordering.
	 *
	 * @param point The point serving as a key.
	 * @param value The value to insert at the point.
	 * @return The old value if an existing value is replaced by the
	 * inserted value.
	 */
	public V put(P point, V value) {
		KDNode[] parent = new KDTree.KDNode[1];
		KDNode node = getNode(point, parent);
		V old = null;

		if ( node!=null ) {
			old = node.getValue();
			_hashCode -= node.hashCode();
			node._value = value;
		}
		else {
			if ( parent[0]==null )
				node = _root = new KDNode(0, point, value);
			else {
				int discriminator = parent[0]._discriminator;

				if ( Integer.compare(point.getCoord(discriminator),	parent[0]._point.getCoord(discriminator))>=0 ) {
					node = parent[0]._high =
						new KDNode((discriminator+1)%_dimensions, point, value);
				}
				else {
					node = parent[0]._low =
						new KDNode((discriminator+1)%_dimensions, point, value);
				}
			}

			++_size;
		}

		_hashCode += node.hashCode();

		return old;
	}

	/**
	 * Copies all of the point-value mappings from the given Map into the KDTree.
	 *
	 * @param map The Map from which to copy the mappings.
	 */
	public void putAll(Map<? extends P, ? extends V> map) {
		for (Map.Entry<? extends P, ? extends V> pair : map.entrySet())
			put(pair.getKey(), pair.getValue());
	}

	/**
	 * Removes the point-value mapping corresponding to the given point key.
	 *
	 * @param key The point key of the mapping to remove.
	 * @return The value part of the mapping, if a mapping existed and
	 * was removed.  Null if not.
	 * @throws ClassCastException If the key is not an instance of P.
	 */
	public V remove(Object key)
		throws ClassCastException {
		KDNode[] parent = new KDTree.KDNode[1];
		KDNode node = getNode((P) key, parent);
		V old = null;

		if ( node!=null ) {
			KDNode child = node;

			node = recursiveRemoveNode(child);

			if ( parent[0]==null )
				_root = node;
			else if ( child==parent[0]._low )
				parent[0]._low = node;
			else if ( child==parent[0]._high )
				parent[0]._high = node;

			--_size;
			_hashCode -= child.hashCode();
			old = child.getValue();
		}

		return old;
	}

	/**
	 * Returns the number of point-value mappings in the KDTree.
	 *
	 * @return The number of point-value mappings in the KDTree.
	 */
	public int size() {
		return _size;
	}

	/**
	 * Returns a Collection view of the values contained in the KDTree.
	 * Changes to the Collection are reflected in the KDTree and vice versa.
	 * Note: the resulting Collection is very inefficient.
	 *
	 * @return A Collection view of the values contained in the KDTree.
	 */
	public Collection<V> values() {
		return new ValueCollection();
	}

	// End Map interface methods

	public Iterator<Map.Entry<P, V>> iterator(P lower, P upper) {
		return new MapEntryIterator(lower, upper);
	}

	int fillArray(KDNode[] a, int index, KDNode node) {
		if ( node==null )
			return index;
		a[index] = node;
		index = fillArray(a, index+1, node._low);
		return fillArray(a, index, node._high);
	}

	final class NodeComparator implements Comparator<KDNode> {
		int _discriminator = 0;

		void setDiscriminator(int val) {
			_discriminator = val;
		}

		int getDiscriminator() {
			return _discriminator;
		}

		public int compare(KDNode n1, KDNode n2) {
			return
				Integer.compare(n1._point.getCoord(_discriminator),n2._point.getCoord(_discriminator));
		}
	}

	KDNode optimize(KDNode[] nodes, int begin, int end, NodeComparator comp) {
		KDNode midpoint = null;
		int size = end-begin;

		if ( size>1 ) {
			int nth = begin+(size >> 1);
			int nthprev = nth-1;
			int d = comp.getDiscriminator();

			Arrays.sort(nodes, begin, end, comp);

			while ( nth>begin &&
				Integer.compare(nodes[nth]._point.getCoord(d), nodes[nthprev]._point.getCoord(d))==0 ) {
				--nth;
				--nthprev;
			}

			midpoint = nodes[nth];
			midpoint._discriminator = d;

			if ( ++d>=_dimensions ) {
				d = 0;
			}

			comp.setDiscriminator(d);

			midpoint._low = optimize(nodes, begin, nth, comp);

			comp.setDiscriminator(d);

			midpoint._high = optimize(nodes, nth+1, end, comp);
		}
		else if ( size==1 ) {
			midpoint = nodes[begin];
			midpoint._discriminator = comp.getDiscriminator();
			midpoint._low = midpoint._high = null;
		}

		return midpoint;
	}

	/**
	 * Optimizes the performance of future search operations by balancing the
	 * KDTree.  The balancing operation is relatively expensive, but can
	 * significantly improve the performance of searches.  Usually, you
	 * don't have to optimize a tree which contains random key values
	 * inserted in a random order.
	 */
	public void optimize() {
		if ( isEmpty() )
			return;

		KDNode[] nodes =
			(KDNode[]) Array.newInstance(KDNode.class, size());
		fillArray(nodes, 0, _root);

		_root = optimize(nodes, 0, nodes.length, new NodeComparator());
	}
}
