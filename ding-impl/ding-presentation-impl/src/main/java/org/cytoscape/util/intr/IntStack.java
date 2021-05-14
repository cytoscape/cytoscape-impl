package org.cytoscape.util.intr;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


/**
 * A first-in, last-out container of 32 bit integers.  In the underlying
 * implementation, the memory consumed by an instance of this class may
 * increase, but does never decrease.<p>
 * An instance of this class is serializable; however, serialized instances of
 * this class should not be stored in a persistent manner because the
 * serialization implemented in this class makes no attempt at handling
 * class versioning.
 */
public final class IntStack implements java.io.Serializable {
	private final static long serialVersionUID = 1213745949153047L;
	// This must be a non-negative integer.
	private static final int DEFAULT_CAPACITY = 11;
	private int[] m_stack;
	private int m_currentSize;

	/**
	 * Creates a new stack of integers.
	 */
	public IntStack() {
		m_stack = new int[DEFAULT_CAPACITY];
		empty();
	}

	/**
	 * Removes all integers from this stack.  This operation has constant time
	 * complexity.
	 */
	public final void empty() {
		m_currentSize = 0;
	}

	/**
	 * Returns the number of integers that are currently on this stack.
	 */
	public final int size() {
		return m_currentSize;
	}

	/**
	 * Pushes a new integer onto this stack.  A successive peek() or pop()
	 * call will return the specified value.
	 */
	public final void push(int value) {
		try {
			m_stack[m_currentSize++] = value;
		} catch (ArrayIndexOutOfBoundsException e) {
			m_currentSize--;
			checkSize();
			m_stack[m_currentSize++] = value;
		}
	}

	/**
	 * A non-mutating operation that retrieves the next integer on this stack.<p>
	 * It is considered an error to call this method if there are no integers
	 * currently on this stack.  If size() returns zero immediately before this
	 * method is called, the results of this operation are undefined.
	 */
	public final int peek() {
		return m_stack[m_currentSize - 1];
	}

	/**
	 * Removes and returns the next integer on this stack.<p>
	 * It is considered an error to call this method if there are no integers
	 * currently on this stack.  If size() returns zero immediately before this
	 * method is called, the results of this operation are undefined.
	 */
	public final int pop() {
		try {
			return m_stack[--m_currentSize];
		} catch (ArrayIndexOutOfBoundsException e) {
			m_currentSize++;
			throw e;
		}
	}

	/**
	 * Returns an enumeration of all elements currently on this stack.
	 * The order of elements in the returned enumeration is based on the
	 * elements' popping order on the stack; the first element returned is
	 * the element waiting to be popped off the stack.<p>
	 * No operation on this stack will have an effect on the returned
	 * enumeration except for push().  If calls to push() are mingled with
	 * calls to iterate over the returned enumeration, the contents of this
	 * enumeration become undefined.  Iterating over the returned enumeration
	 * never has an affect on this stack.
	 */
	public final IntEnumerator elements() {
		final int[] stack = m_stack;
		final int size = m_currentSize;

		return new IntEnumerator() {
				private int inx = size;

				public final int numRemaining() {
					return inx;
				}

				public final int nextInt() {
					return stack[--inx];
				}
			};
	}

	private final void checkSize() {
		if (m_currentSize < m_stack.length)
			return;

		final int newStackSize = (int) Math.min((long) Integer.MAX_VALUE,
		                                        (((long) m_stack.length) * 2L) + 1L);

		if (newStackSize == m_stack.length)
			throw new IllegalStateException("cannot allocate large enough array");

		final int[] newStack = new int[newStackSize];
		System.arraycopy(m_stack, 0, newStack, 0, m_stack.length);
		m_stack = newStack;
	}
}
