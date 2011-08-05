
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
 * A utility class which conveniently converts an array of integers into
 * an IntEnumerator (an enumeration of integers).
 */
public final class ArrayIntEnumerator implements IntEnumerator {
	private final int[] m_elements;
	private int m_index;
	private final int m_end;

	/**
	 * No copy of the elements array is made.  The contents of the array
	 * are never modified by this object.
	 */
	public ArrayIntEnumerator(int[] elements, int beginIndex, int length) {
		if (beginIndex < 0)
			throw new IllegalArgumentException("beginIndex is less than zero");

		if (length < 0)
			throw new IllegalArgumentException("length is less than zero");

		if ((((long) beginIndex) + (long) length) > (long) elements.length)
			throw new IllegalArgumentException("combination of beginIndex and length exceed length of array");

		m_elements = elements;
		m_index = beginIndex;
		m_end = beginIndex + length;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public final int numRemaining() {
		return m_end - m_index;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public final int nextInt() {
		return m_elements[m_index++];
	}
}
