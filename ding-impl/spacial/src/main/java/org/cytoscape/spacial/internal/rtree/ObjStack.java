
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

package org.cytoscape.spacial.internal.rtree;


/**
 * A first-in, last-out container of objects; an object put onto the stack
 * may be null.  In the underlying implementation, the memory consumed by an
 * instance of this class may increase, but does never decrease.<p>
 * NOTE: Right now this class is package visible but it is written in such
 * a way so as to be suitable to become public at some point, once a proper
 * package location for it is found.
 */
final class ObjStack implements java.io.Serializable {
	private final static long serialVersionUID = 1213746741406103L;
	// This must be a non-negative integer.
	private static final int DEFAULT_CAPACITY = 11;
	private Object[] m_stack;
	private int m_currentSize;

	/**
	 * Creates a new stack of objects.
	 */
	public ObjStack() {
		m_stack = new Object[DEFAULT_CAPACITY];
		m_currentSize = 0;
	}

	/**
	 * Removes everything from this stack.  This operation has time complexity
	 * linear to the current size of this stack because we null out every entry
	 * so as to not hinder garbage collection.
	 */
	public final void empty() {
		for (int i = m_currentSize - 1; i >= 0; i--)
			m_stack[i] = null;

		m_currentSize = 0;
	}

	/**
	 * Returns the number of objects that are currently on this stack.
	 */
	public final int size() {
		return m_currentSize;
	}

	/**
	 * Pushes a new object onto this stack.  A successive peek() or pop()
	 * call will return the specified object.  An object pushed onto this
	 * stack may be null.
	 */
	public final void push(final Object obj) {
		try {
			m_stack[m_currentSize++] = obj;
		} catch (ArrayIndexOutOfBoundsException e) {
			m_currentSize--;
			checkSize();
			m_stack[m_currentSize++] = obj;
		}
	}

	/**
	 * A non-mutating operation that retrieves the next object on this stack.<p>
	 * It is considered an error to call this method if there are no objects
	 * currently on this stack.  If size() returns zero immediately before this
	 * method is called, the results of this operation are undefined.
	 */
	public final Object peek() {
		return m_stack[m_currentSize - 1];
	}

	/**
	 * Removes and returns the next object on this stack.<p>
	 * It is considered an error to call this method if there are no objects
	 * currently on this stack.  If size() returns zero immediately before this
	 * method is called, the results of this operation are undefined.
	 */
	public final Object pop() {
		try {
			final Object returnThis = m_stack[--m_currentSize];
			m_stack[m_currentSize] = null; // This line is essential.

			return returnThis;
		} catch (ArrayIndexOutOfBoundsException e) {
			m_currentSize++;
			throw e;
		}
	}

	private final void checkSize() {
		if (m_currentSize < m_stack.length)
			return;

		final int newStackSize = (int) Math.min((long) Integer.MAX_VALUE,
		                                        (((long) m_stack.length) * 2L) + 1L);

		if (newStackSize == m_stack.length)
			throw new IllegalStateException("cannot allocate large enough array");

		final Object[] newStack = new Object[newStackSize];
		System.arraycopy(m_stack, 0, newStack, 0, m_stack.length);
		m_stack = newStack;
	}
}
