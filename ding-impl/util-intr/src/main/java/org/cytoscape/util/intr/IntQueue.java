
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
 * A first-in, first-out container of 32 bit integers.  In the underlying
 * implementation, the memory consumed by an instance of this class may
 * increase, but does never decrease.<p>
 * While other container classes in this package are able to hold up
 * to Integer.MAX_VALUE elements, this class is only able to hold
 * Integer.MAX_VALUE-1 elements.<p>
 * An instance of this class is serializable; however, serialized instances of
 * this class should not be stored in a persistent manner because the
 * serialization implemented in this class makes no attempt at handling
 * class versioning.
 */
public final class IntQueue implements java.io.Serializable {
	private final static long serialVersionUID = 121374594921419L;
	// This must be a non-negative integer.
	private static final int DEFAULT_CAPACITY = 12;
	private int[] m_queue;
	private int m_head;
	private int m_tail;

	/**
	 * Creates a new queue of integers.
	 */
	public IntQueue() {
		m_queue = new int[DEFAULT_CAPACITY];
		empty();
	}

	/**
	 * Removes all integers from this queue.  This operation has constant time
	 * complexity.
	 */
	public final void empty() {
		m_head = 0;
		m_tail = 0;
	}

	/**
	 * Returns the number of integers that are currently in this queue.
	 */
	public final int size() {
		int absHead = m_head;

		if (absHead < m_tail)
			absHead += m_queue.length;

		return absHead - m_tail;
	}

	/**
	 * Inserts a new integer into this queue.
	 */
	public final void enqueue(int value) {
		checkSize();
		m_queue[m_head++] = value;

		if (m_head == m_queue.length)
			m_head = 0;
	}

	/**
	 * A non-mutating operation that retrieves the next integer in this queue.<p>
	 * It is considered an error to call this method if there are no integers
	 * currently in this queue.  If size() returns zero immediately before
	 * this method is called, the results of this operation are undefined.
	 */
	public final int peek() {
		return m_queue[m_tail];
	}

	/**
	 * Removes and returns the next integer in this queue.<p>
	 * It is considered an error to call this method if there are no integers
	 * currently in this queue.  If size() returns zero immediately before
	 * this method is called, the results of this operation are undefined.
	 */
	public final int dequeue() {
		final int returnThis = m_queue[m_tail++];

		if (m_tail == m_queue.length)
			m_tail = 0;

		return returnThis;
	}

	/**
	 * Returns an enumeration of all elements currently in this queue.
	 * The order of elements in the returned enumeration is based on the
	 * elements' dequeue order; the first element returned is the element
	 * waiting to be dequeued.<p>
	 * No operation on this queue will have an effect on the returned
	 * enumeration except for enqueue().  If calls to enqueue() are mingled with
	 * calls to iterate over the returned enumeration, the contents of this
	 * enumeration become undefined.  Iterating over the returned enumeration
	 * never has an affect on this queue.
	 */
	public final IntEnumerator elements() {
		final int[] queue = m_queue;
		final int head = m_head;
		final int tail = m_tail;

		return new IntEnumerator() {
				private int inx = tail;

				public final int numRemaining() {
					int absHead = head;

					if (absHead < inx)
						absHead += queue.length;

					return absHead - inx;
				}

				public final int nextInt() {
					final int returnThis = queue[inx++];

					if (inx == queue.length)
						inx = 0;

					return returnThis;
				}
			};
	}

	private final void checkSize() {
		if ((size() + 2) > m_queue.length) {
			final int newQueueArrSize = (int) Math.min((long) Integer.MAX_VALUE,
			                                           (((long) m_queue.length) * 2L) + 1L);

			if (newQueueArrSize == m_queue.length)
				throw new IllegalStateException("cannot allocate large enough array");

			final int[] newQueueArr = new int[newQueueArrSize];

			if (m_tail <= m_head) {
				System.arraycopy(m_queue, m_tail, newQueueArr, 0, m_head - m_tail);
				m_head = m_head - m_tail;
			} else {
				System.arraycopy(m_queue, m_tail, newQueueArr, 0, m_queue.length - m_tail);
				System.arraycopy(m_queue, 0, newQueueArr, m_queue.length - m_tail, m_head);
				m_head = m_head + (m_queue.length - m_tail);
			}

			m_tail = 0;
			m_queue = newQueueArr;
		}
	}
}
