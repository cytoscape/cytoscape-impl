
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.util.intr;


/**
 * An insert-only hashtable that has non-negative 32 bit integer keys and
 * non-negative 32 bit integer values.<p>
 * In the underlying implementation, this hashtable increases in size to adapt
 * to key/value pairs being added (the underlying size of the hashtable is
 * invisible to the programmer).  In the underlying implementation, this
 * hashtable never decreases in size.  As a hashtable increases in size,
 * it takes at most four times as much memory as it would take
 * to store the hashtable's keys and values in a perfectly-sized array.
 * Underlying size expansions are implemented such that the operation of
 * expanding in size is amortized over the contstant time complexity needed to
 * insert new elements.<p>
 * An instance of this class is serializable; however, serialized instances of
 * this class should not be stored in a persistent manner because the
 * serialization implemented in this class makes no attempt at handling
 * class versioning.
 */
public final class IntIntHash implements java.io.Serializable {
	private final static long serialVersionUID = 1213745949129420L;
	private static final int[] PRIMES = {
	                                        11, 23, 53, 113, 251, 509, 1019, 2039, 4079, 8179, 16369,
	                                        32749, 65521, 131063, 262133, 524269, 1048571, 2097143,
	                                        4194287, 8388587, 16777183, 33554393, 67108837,
	                                        134217689, 268435399, 536870879, 1073741789, 2147483647
	                                    };
	private static final int INITIAL_SIZE = PRIMES[0];
	private static final double THRESHOLD_FACTOR = 0.77;
	private int[] m_keys;
	private int[] m_vals;
	private int m_elements;
	private int m_size;
	private int m_thresholdSize;

	// These are caching variables.  The idea is that programmers will
	// frequently first do a get(), and based on that result, will perform
	// some other operations and then maybe do a put() operation with the same
	// key as the previous get() operation.
	private int m_prevKey;
	private int m_prevInx;

	/**
	 * Creates a new hashtable.
	 */
	public IntIntHash() {
		m_keys = new int[INITIAL_SIZE];
		m_vals = new int[INITIAL_SIZE];
		empty();
	}

	/**
	 * Removes all key/value pairs from this hashtable.  This operation has
	 * O(1) time complexity.
	 */
	public final void empty() {
		m_elements = 0;
		m_size = INITIAL_SIZE;
		m_thresholdSize = (int) (THRESHOLD_FACTOR * (double) m_size);

		for (int i = 0; i < m_size; i++) {
			m_keys[i] = -1;
			m_vals[i] = -1;
		}

		m_prevKey = -1;
		m_prevInx = -1;
	}

	/**
	 * Returns the number of key/value pairs currently in this hashtable.
	 */
	public final int size() {
		return m_elements;
	}

	/**
	 * Puts a new key/value pair into this hashtable, potentially overwriting
	 * an existing value whose key is the same as the one specified.
	 * Returns the old value associated with the specified key or -1 if no value
	 * is associated with specified key at the time of this call.<p>
	 * Insertions into the hashtable are performed in [amortized] time
	 * complexity O(1).
	 *
	 * @exception IllegalArgumentException if either key or value is negative.
	 */
	public final int put(final int key, final int value) {
		if (key < 0)
			throw new IllegalArgumentException("key is negative");

		if (value < 0)
			throw new IllegalArgumentException("value is negative");

		if (m_elements == m_thresholdSize)
			incrSize();

		if (key != m_prevKey) {
			int incr = 0;

			for (m_prevInx = key % m_size; (m_keys[m_prevInx] >= 0) && (m_keys[m_prevInx] != key);
			     m_prevInx = (m_prevInx + incr) % m_size)
				if (incr == 0)
					incr = 1 + (key % (m_size - 1));

			m_prevKey = key;
		}

		final int returnVal = m_vals[m_prevInx];
		m_vals[m_prevInx] = value;
		m_keys[m_prevInx] = key;
		m_elements += (returnVal >>> 31);

		return returnVal;
	}

	/**
	 * Returns the value bound to the specified key or -1 if no value is
	 * currently bound to the specified key.<p>
	 * Searches in this hashtable are performed in [amortized] time
	 * complexity O(1).
	 *
	 * @exception IllegalArgumentException if key is negative.
	 */
	public final int get(final int key) {
		if (key < 0)
			throw new IllegalArgumentException("key is negative");

		if (key != m_prevKey) {
			int incr = 0;

			for (m_prevInx = key % m_size; (m_keys[m_prevInx] >= 0) && (m_keys[m_prevInx] != key);
			     m_prevInx = (m_prevInx + incr) % m_size)
				if (incr == 0)
					incr = 1 + (key % (m_size - 1));

			m_prevKey = key;
		}

		return m_vals[m_prevInx];
	}

	/**
	 * Returns an enumeration of keys in this hashtable, ordered
	 * arbitrarily.<p>
	 * The returned enumeration becomes invalid as soon as put(int, int) or
	 * empty() is called on this hashtable; calling methods on an invalid
	 * enumeration will cause undefined behavior in the enumerator.
	 * The returned enumerator has absolutely no effect on the underlying
	 * hashtable.<p>
	 * This method returns in constant time.  The returned enumerator
	 * returns successive keys in [amortized] time complexity O(1).<p>
	 * It is possible to get the keys() and values() enumerations at the same
	 * time and iterate over them simultaneously; then, the
	 * i<sup>th</sup> element of the keys() enumeration is the key into the
	 * i<sup>th</sup> element of the values() enumeration.
	 */
	public final IntEnumerator keys() {
		return enumeration(m_keys);
	}

	/**
	 * Returns an enumeration of values in this hashtable, ordered
	 * arbitrarily.<p>
	 * The returned enumeration becomes invalid as soon as put(int, int) or
	 * empty() is called on this hashtable; calling methods on an invalid
	 * enumeration will cause undefined behavior in the enumerator.
	 * The returned enumerator has absolutely no effect on the underlying
	 * hashtable.<p>
	 * This method returns in constant time.  The returned enumerator
	 * returns successive values in [amortized] time complexity O(1).<p>
	 * It is possible to get the keys() and values() enumerations at the same
	 * time and iterate over them simultaneously; then, the
	 * i<sup>th</sup> element of the keys() enumeration is the key into the
	 * i<sup>th</sup> element of the values() enumeration.
	 */
	public final IntEnumerator values() {
		return enumeration(m_vals);
	}

	private final IntEnumerator enumeration(final int[] arr) {
		final int numElements = m_elements;

		return new IntEnumerator() {
				int elements = numElements;
				int index = -1;

				public int numRemaining() {
					return elements;
				}

				public int nextInt() {
					while (arr[++index] < 0)
						;

					elements--;

					return arr[index];
				}
			};
	}

	private int[] m_keyDump = null;
	private int[] m_valDump = null;

	private final void incrSize() {
		final int newSize;

		try {
			int primesInx = 0;

			while (m_size != PRIMES[primesInx++])
				;

			newSize = PRIMES[primesInx];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalStateException("too many elements in this hashtable");
		}

		if (m_keys.length < newSize) {
			m_keyDump = m_keys;
			m_valDump = m_vals;
			m_keys = new int[newSize];
			m_vals = new int[newSize];
		} else {
			System.arraycopy(m_keys, 0, m_keyDump, 0, m_size);
			System.arraycopy(m_vals, 0, m_valDump, 0, m_size);
		}

		for (int i = 0; i < newSize; i++) {
			m_keys[i] = -1;
			m_vals[i] = -1;
		}

		m_size = newSize;
		m_thresholdSize = (int) (THRESHOLD_FACTOR * (double) m_size);

		int incr;
		int newIndex;
		int oldIndex = -1;

		for (int i = 0; i < m_elements; i++) {
			while (m_keyDump[++oldIndex] < 0)
				;

			incr = 0;

			for (newIndex = m_keyDump[oldIndex] % m_size; m_keys[newIndex] >= 0;
			     newIndex = (newIndex + incr) % m_size)
				if (incr == 0)
					incr = 1 + (m_keyDump[oldIndex] % (m_size - 1));

			m_keys[newIndex] = m_keyDump[oldIndex];
			m_vals[newIndex] = m_valDump[oldIndex];
		}

		m_prevKey = -1;
		m_prevInx = -1;
	}
}
