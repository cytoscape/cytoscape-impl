
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

package org.cytoscape.model.internal;

import java.util.NoSuchElementException;


/**
 * An insert-only hashtable that has non-negative 32 bit integer keys and
 * non-null object values.<p>
 * In the underlying implementation, this hashtable increases in size to adapt
 * to key/value pairs being added (the underlying size of the hashtable is
 * invisible to the programmer).  In the underlying implementation, this
 * hashtable never decreases in size.  As a hashtable increases in size,
 * it takes at most four times as much memory as it would take
 * to store the hashtable's keys and values in perfectly-sized arrays.
 * Underlying size expansions are implemented such that the operation of
 * expanding in size is amortized over the contstant time complexity needed to
 * insert new elements.<p>
 * An instance of this class is serializable; however, serialized instances of
 * this class should not be stored in a persistent manner because the
 * serialization implemented in this class makes no attempt at handling
 * class versioning.  For an instance of this class to be properly
 * serialized, all object values stored in that instance must be
 * serializable.
 */
public final class LongTHash<T> {
	private final static long serialVersionUID = 121374594955439L;
	private static final int[] PRIMES = {
	                                        11, 23, 53, 113, 251, 509, 1019, 2039, 4079, 8179, 
	                                        16369, 32749, 65521, 131063, 262133, 524269, 1048571, 
	                                        2097143, 4194287, 8388587, 16777183, 33554393, 67108837,
	                                        134217689, 268435399, 536870879, 1073741789, 2147483647
	                                    };
	private static final int INITIAL_SIZE = PRIMES[0];
	private static final double THRESHOLD_FACTOR = 0.77;
	private long[] m_keys;
	private Object[] m_vals;
	private int m_elements;
	private int m_thresholdSize;

	// These are caching variables.  The idea is that programmers will
	// frequently first do a get(), and based on that result, will perform
	// some other operations and then maybe do a put() operation with the same
	// key as the previous get() operation.
	private long m_prevKey;
	private int m_prevInx;

	private final Class<T> clazz;

	/**
	 * Creates a new hashtable.
	 */
	public LongTHash(Class<T> clazz) {
		this.clazz = clazz;
		m_keys = new long[INITIAL_SIZE];
		m_vals = new Object[INITIAL_SIZE];
		m_elements = 0;
		m_thresholdSize = (int) (THRESHOLD_FACTOR * (double) m_keys.length);

		for (int i = 0; i < m_keys.length; i++)
			m_keys[i] = -1;

		m_prevKey = -1;
		m_prevInx = -1;
	}

	/**
	 * Puts a new key/value pair into this hashtable, potentially overwriting
	 * an existing value whose key is the same as the one specified.
	 * Returns the old value associated with the specified key or null if no
	 * value is associated with specified key at the time of this call.<p>
	 * Insertions into the hashtable are performed in [amortized] time
	 * complexity O(1).
	 * @exception IllegalArgumentException if key is negative.
	 * @exception NullPointerException if value is null.
	 */
	public final T put(final long key, final T value) {
		if (key < 0)
			throw new IllegalArgumentException("key is negative");

		if (key != m_prevKey) {
			calcPrevInx(key);
			m_prevKey = key;
		}

		final Object returnVal = m_vals[m_prevInx];

		if (returnVal == null) {
			if (m_elements == m_thresholdSize) {
				incrSize();

				return put(key, value);
			}

			m_elements++;
		}

		m_vals[m_prevInx] = value;
		if ( value == null )
			m_keys[m_prevInx] = -1;
		else
			m_keys[m_prevInx] = key;

		return clazz.cast(returnVal);
	}

	/**
	 * Returns the value bound to the specified key or null if no value is
	 * currently bound to the specified key.<p>
	 * Searches in this hashtable are performed in [amortized] time
	 * complexity O(1).
	 * @exception IllegalArgumentException if key is negative.
	 */
	public final T get(final long key) {
		if (key < 0)
			return null;

		if (key != m_prevKey) {
			calcPrevInx(key);
			m_prevKey = key;
		}

		return clazz.cast(m_vals[m_prevInx]);
	}

	public final T remove(final long key) {
		Object ret = get(key);
		put(key,null);
		m_elements--;
		return clazz.cast(ret);
	}

	// for unit testing
	int size() {
		return m_elements;
	}

	private void calcPrevInx(long key) {
		int incr = 0;

		for (m_prevInx = (int)(key % (long)m_keys.length);
		     (m_keys[m_prevInx] >= 0) && (m_keys[m_prevInx] != key);
		     m_prevInx = (m_prevInx + incr) % m_keys.length)
			if (incr == 0)
				incr = 1 + (int)(key % ((long)m_keys.length - 1));
	}

	private final void incrSize() {
		final int newSize;

		try {
			int primesInx = 0;

			while (m_keys.length != PRIMES[primesInx++])
				;

			newSize = PRIMES[primesInx];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalStateException("too many elements in this hashtable");
		}

		final long[] newKeys = new long[newSize];
		final Object[] newVals = new Object[newSize];

		for (int i = 0; i < newKeys.length; i++)
			newKeys[i] = -1L;

		m_thresholdSize = (int) (THRESHOLD_FACTOR * (double) newKeys.length);

		int incr;
		int newIndex;
		int oldIndex = -1;

		for (int i = 0; i < m_elements; i++) {
			while (m_keys[++oldIndex] < 0);

			incr = 0;

			for (newIndex = (int)(m_keys[oldIndex] % (long)newKeys.length); newKeys[newIndex] >= 0;
			     newIndex = (newIndex + incr) % newKeys.length)
				if (incr == 0)
					incr = 1 + ((int)(m_keys[oldIndex] % (long)(newKeys.length - 1)));

			newKeys[newIndex] = m_keys[oldIndex];
			newVals[newIndex] = m_vals[oldIndex];
		}

		m_keys = newKeys;
		m_vals = newVals;
		m_prevKey = -1;
		m_prevInx = -1;
	}
}
