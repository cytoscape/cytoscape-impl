
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
 * A dynamically growing array of integers.
 * Valid indices of this array are in the range [0, Integer.MAX_VALUE - 1].<p>
 * In the underlying implementation, this dynamically growing array
 * increases in size to adapt to elements being added (the size of the
 * underlying data structure supporting this dynamically growing array
 * is invisible to the programmer).  In the underlying implementation, this
 * dynamic array never decreases in size.  Underlying size expansions are
 * implemented such that the operation of expanding in size is amortized over
 * the constant time complexity of inserting new elements into this
 * dynamic array.<p>
 * An instance of this class is serializable; however, serialized instances of
 * this class should not be stored in a persistent manner because the
 * serialization implemented in this class makes no attempt at handling
 * class versioning.
 */
public final class IntArray implements java.io.Serializable {
	private final static long serialVersionUID = 121374594932593L;
	private final static int INITIAL_CAPACITY = 0; // Must be non-negative.
	private int[] m_arr;

	/**
	 * Creates a new IntArray object.
	 */
	public IntArray() {
		m_arr = new int[INITIAL_CAPACITY];
	}

	/**
	 * Returns the value at specified index.
	 * This method will not increase the size of the underlying array, no
	 * matter what.  The value returned by this method will be 0 unless a
	 * value at given index has been previously specified with
	 * setIntAtIndex(int, int).
	 *
	 * @exception ArrayIndexOutOfBoundsException if index is negative or
	 *   Integer.MAX_VALUE.
	 */
	public final int getIntAtIndex(int index) {
		// Do pre-checking because try/catch with thrown exception causes huge
		// performance hit.
		if ((index >= m_arr.length) && (index != Integer.MAX_VALUE))
			return 0;

		return m_arr[index]; // Exception if Integer.MAX_VALUE or negative.
	}

	/**
	 * Sets the specified value at specified index.
	 * This method will potentially increase the size of the underlying array,
	 * but only if two conditions hold:
	 *   1. value is not zero and
	 *   2. index is greater than or equal to the length of the underlying
	 *      array.
	 * <p>
	 * NOTE: Setting very large indices to non-zero values implies instantiating
	 * a very large underlying data structure.
	 *
	 * @exception ArrayIndexOutOfBoundsException if index is negative or
	 *   Integer.MAX_VALUE.
	 */
	public final void setIntAtIndex(int value, int index) {
		// Do pre-checking because try/catch with thrown exception causes huge
		// performance hit.
		if ((index >= m_arr.length) && (value == 0) && (index != Integer.MAX_VALUE))
			return;

		try {
			m_arr[index] = value;
		} catch (ArrayIndexOutOfBoundsException e) {
			if ((index < 0) || (index == Integer.MAX_VALUE)) {
				throw e;
			}

			// We need to ensure amortized constant time hits.
			final int newArrSize = (int) Math.min((long) Integer.MAX_VALUE,
			                                      Math.max((((long) m_arr.length) * 2L) + 1L,
			                                               ((long) index) + 1L
			                                               + (long) INITIAL_CAPACITY));
			int[] newArr = new int[newArrSize];
			System.arraycopy(m_arr, 0, newArr, 0, m_arr.length);
			m_arr = newArr;
			m_arr[index] = value;
		}
	}
}
