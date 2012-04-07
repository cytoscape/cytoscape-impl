
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
 * An insert-only hashtable that has non-negative 32 bit integer keys;
 * no satellite data is stored in this hashtable.  An instance of this class
 * is well-suited for efficiently detecting collisions between integers,
 * removing duplicates from a list of integers, or determining the presence of
 * an integer in a list of integers.<p>
 * In the underlying implementation, this hashtable increases in size to adapt
 * to elements being added (the underlying size of the hashtable is invisible
 * to the programmer).  In the underlying implementation, this hashtable never
 * decreases in size.  As a hashtable increases in size,
 * it takes at most four times as much memory as it would take
 * to store the hashtable's elements in a perfectly-sized array.
 * Underlying size expansions are implemented such that the operation of
 * expanding in size is amortized over the contstant time complexity of
 * inserting new elements.<p>
 * An instance of this class is serializable; however, serialized instances of
 * this class should not be stored in a persistent manner because the
 * serialization implemented in this class makes no attempt at handling
 * class versioning.
 */
public final class IntHash implements java.io.Serializable {
	private final static long serialVersionUID = 1213745949547L;
	private static final int[] PRIMES = {
	                                        11, 23, 53, 113, 251, 509, 1019, 2039, 4079, 8179, 16369,
	                                        32749, 65521, 131063, 262133, 524269, 1048571, 2097143,
	                                        4194287, 8388587, 16777183, 33554393, 67108837,
	                                        134217689, 268435399, 536870879, 1073741789, 2147483647
	                                    };
	private static final int INITIAL_SIZE = PRIMES[0];
	private static final double THRESHOLD_FACTOR = 0.77;
	private int[] m_arr;
	private int m_elements;
	private int m_size;
	private int m_thresholdSize;

	/**
	 * Creates a new hashtable.
	 */
	public IntHash() {
		m_arr = new int[INITIAL_SIZE];
		empty();
	}

	/**
	 * Removes all elements from this hashtable.  This operation has
	 * O(1) time complexity.
	 */
	public final void empty() {
		m_elements = 0;
		m_size = INITIAL_SIZE;
		m_thresholdSize = (int) (THRESHOLD_FACTOR * (double) m_size);

		for (int i = 0; i < m_size; i++)
			m_arr[i] = -1;
	}

	/**
	 * Returns the number of elements currently in this hashtable.
	 */
	public final int size() {
		return m_elements;
	}

	/**
	 * Puts a new value into this hashtable if that value is not already in
	 * this hashtable; otherwise does nothing.  Returns the input value if this
	 * value was already in this hashtable; returns -1 if the input value was
	 * not in this hashtable prior to this call.<p>
	 * Insertions into the hashtable are performed in [amortized] time
	 * complexity O(1).
	 *
	 * @exception IllegalArgumentException if value is negative.
	 */
	public final int put(final int value) {
		if (m_elements == m_thresholdSize)
			incrSize();

		int incr = 0;
		int index;

		try {
			for (index = value % (((~value) >>> 31) * m_size);
			     (m_arr[index] >= 0) && (m_arr[index] != value); index = (index + incr) % m_size) {
				// Caching increment, which is an expensive operation, at the expense
				// of having an if statement.  I don't want to compute the increment
				// before this 'for' loop in case we get an immediate hit.
				if (incr == 0) {
					incr = 1 + (value % (m_size - 1));
				}
			}
		} catch (ArithmeticException exc) {
			throw new IllegalArgumentException("value is negative");
		}

		final int returnVal = m_arr[index];
		m_arr[index] = value;
		m_elements += (returnVal >>> 31);

		return returnVal;
	}

	/**
	 * Determines whether or not the value specified is in this hashtable.
	 * Returns the value specified if this value is in the hashtable, otherwise
	 * returns -1.<p>
	 * Searches in this hashtable are performed in [amortized] time
	 * complexity O(1).
	 *
	 * @exception IllegalArgumentException if value is negative.
	 */
	public final int get(final int value) {
		int incr = 0;
		int index;

		try {
			for (index = value % (((~value) >>> 31) * m_size);
			     (m_arr[index] >= 0) && (m_arr[index] != value); index = (index + incr) % m_size) {
				// Caching increment, which is an expensive operation, at the expense
				// of having an if statement.  I don't want to compute the increment
				// before this 'for' loop in case we get an immediate hit.
				if (incr == 0) {
					incr = 1 + (value % (m_size - 1));
				}
			}
		} catch (ArithmeticException exc) {
			throw new IllegalArgumentException("value is negative");
		}

		return m_arr[index];
	}

	/**
	 * Returns an enumeration of elements in this hashtable, ordered
	 * arbitrarily.<p>
	 * The returned enumeration becomes invalid as soon as put(int) or empty()
	 * is called on this hashtable; calling methods on an invalid
	 * enumeration will cause undefined behavior in the enumerator.
	 * The returned enumerator has absolutely no effect on the underlying
	 * hashtable.<p>
	 * This method returns in constant time.  The returned enumerator
	 * returns successive elements in [amortized] time complexity O(1).
	 */
	public final IntEnumerator elements() {
		final int[] array = m_arr;
		final int numElements = m_elements;

		return new IntEnumerator() {
				int elements = numElements;
				int index = -1;

				public int numRemaining() {
					return elements;
				}

				public int nextInt() {
					while (array[++index] < 0) {
					}

					elements--;

					return array[index];
				}
			};
	}

	private int[] m_dump = null;

	private final void incrSize() {
		final int newSize;

		try {
			int primesInx = 0;

			while (m_size != PRIMES[primesInx++]) {
			}

			newSize = PRIMES[primesInx];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalStateException("too many elements in this hashtable");
		}

		if (m_arr.length < newSize) {
			m_dump = m_arr; // We want the old m_dump to potentially get garbage
			                // collected before we instantiate the new array.

			m_arr = new int[newSize];
		} else {
			System.arraycopy(m_arr, 0, m_dump, 0, m_size);
		}

		for (int i = 0; i < newSize; i++)
			m_arr[i] = -1;

		m_size = newSize;
		m_thresholdSize = (int) (THRESHOLD_FACTOR * (double) m_size);

		int incr;
		int newIndex;
		int oldIndex = -1;

		for (int i = 0; i < m_elements; i++) {
			while (m_dump[++oldIndex] < 0) {
			}

			incr = 0;

			for (newIndex = m_dump[oldIndex] % m_size; m_arr[newIndex] >= 0;
			     newIndex = (newIndex + incr) % m_size) {
				// Caching increment, which is an expensive operation, at the expense
				// of having an if statement.  I don't want to compute the increment
				// before this 'for' loop in case we get an immediate hit.
				if (incr == 0) {
					incr = 1 + (m_dump[oldIndex] % (m_size - 1));
				}
			}

			m_arr[newIndex] = m_dump[oldIndex];
		}
	}
}
