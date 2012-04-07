
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
 * An in-memory B<sup>+</sup>-tree that stores integers.<p>
 * The motivation behind the implementation of this tree is not usefulness.
 * The motivation is to get to know the algorithms associated with this tree
 * structure, so that implementing variants of this structure would become
 * simpler (an example of a variant of this structure is the R-tree).  The
 * implementation of this tree uses recursion (as opposed to the iterative
 * approach).  While there is a performance penalty paid by using recursion,
 * recursion does make the code much more understandable.<p>
 * An instance of this class is serializable; however, serialized instances of
 * this class should not be stored in a persistent manner because the
 * serialization implemented in this class makes no attempt at handling
 * class versioning.
 */
public final class IntBTree implements java.io.Serializable {
	private final static long serialVersionUID = 1213745949101434L;
	// This quantity must be at least 3.
	/**
	 * 
	 */
	public final static int DEFAULT_MAX_BRANCHES = 27;
	private final int m_maxBranches;
	private final int m_minBranches;
	private Node m_root;

	/**
	 * Creates a new tree structure with an optimal branching factor.
	 */
	public IntBTree() {
		this(DEFAULT_MAX_BRANCHES);
	}

	/**
	 * Creates a new tree structure with the specified maximum branching
	 * factor.  Overriding the default maximum branching factor is only
	 * useful for testing purposes; there are no performance gains to be had.
	 * @param maxBranches the maximum branching factor of this tree.
	 * @exception IllegalArgumentException if maxBranches is less than three.
	 */
	public IntBTree(final int maxBranches) {
		m_maxBranches = maxBranches;

		// Letting minimum fill fall below half actually decreases performance by
		// a hair, my tests show.  However, I like the idea of letting nodes fall
		// below being half full, so I'm keeping it at 0.4 (as opposed to 0.5).
		// Note that every piece of literature in the entire universe that
		// mentions B-trees defines them is such a way which is equivalent to
		// to setting this to 0.5, and not below (setting it above 0.5 would
		// destroy the algorithms).  With 0.4, the following table shows
		// the max/min relationships:
		//
		//  m_maxBranches | m_minBranches
		// ---------------+---------------
		//   3               2
		//   4               2
		//   5               2
		//   6               2
		//   7               3
		//   8               3
		//   9               4
		//   27              11
		m_minBranches = Math.max(2, (int) (((double) (m_maxBranches + 1)) * 0.4d));

		m_root = new Node(m_maxBranches, true);
	}

	/**
	 * Empties this structure of all elements.  This method returns in constant
	 * time (note however that garbage collection will take place in the
	 * background).
	 */
	public final void empty() {
		m_root = new Node(m_maxBranches, true);
	}

	/**
	 * Returns the number of elements currently in this structure.  Duplicate
	 * entries are counted however many times they are present.  This method
	 * returns in constant time.<p>
	 * NOTE: To retrieve an enumeration of all entries in this tree, use
	 * searchRange() with Integer.MIN_VALUE as the lower bound and
	 * Integer.MAX_VALUE as the upper bound.
	 */
	public final int size() {
		return isLeafNode(m_root) ? m_root.sliceCount : m_root.data.deepCount;
	}

	/*
	 * This simple functionality is in the form of a helper method in order
	 * to make code more readable.
	 */
	private final static boolean isLeafNode(final Node n) {
		return n.data == null;
	}

	/**
	 * Inserts a new entry into this tree structure; duplicate entries may be
	 * inserted.  This method has a time complexity of O(log(N)) where N is the
	 * number of entries currently stored in this tree structure.
	 * @param x the new entry to insert.
	 */
	public final void insert(final int x) {
		final Node newSibling = insert(m_root, x, m_maxBranches);

		if (newSibling != null) { // The root has been split into two.

			final int newSplitVal;
			final int newDeepCount;

			if (isLeafNode(newSibling)) {
				newSplitVal = newSibling.values[0];
				newDeepCount = m_root.sliceCount + newSibling.sliceCount;
			} else {
				newSplitVal = m_root.data.splitVals[m_root.sliceCount - 1];
				newDeepCount = m_root.data.deepCount + newSibling.data.deepCount;
			}

			final Node newRoot = new Node(m_maxBranches, false);
			newRoot.sliceCount = 2;
			newRoot.data.deepCount = newDeepCount;
			newRoot.data.splitVals[0] = newSplitVal;
			newRoot.data.children[0] = m_root;
			newRoot.data.children[1] = newSibling;
			m_root = newRoot;
		}
	}

	/*
	 * Returns a node being the newly created node if a split was performed;
	 * the node returned is the right sibling of node n.  If the returned node
	 * is a leaf node then the first value of the node is to be the new split
	 * index; if return value is internal node, then the split index to be used
	 * is n.data.splitVals[n.sliceCount - 1].  (This is something that this
	 * method sets; it's this method saying "use this index in the higher
	 * levels".)
	 */
	private final static Node insert(final Node n, final int x, final int maxBranches) {
		if (isLeafNode(n)) {
			if (n.sliceCount < maxBranches) { // There's room for a value.

				int i = -1;

				while (++i < n.sliceCount)

					if (x <= n.values[i])
						break;

				for (int j = n.sliceCount; j > i;)
					n.values[j] = n.values[--j];

				n.values[i] = x;
				n.sliceCount++;

				return null;
			} else { // No room for another value in this leaf node; perform split.

				final Node newLeafSibling = new Node(maxBranches, true);
				final int combinedCount = maxBranches + 1;
				n.sliceCount = combinedCount >> 1; // Divide by two.
				newLeafSibling.sliceCount = combinedCount - n.sliceCount;
				split(x, n.values, newLeafSibling.values, newLeafSibling.sliceCount);

				return newLeafSibling;
			}
		} else { // Internal node.

			int foundPath = 0;

			for (int i = n.sliceCount - 2; i >= 0; i--)
				if (x >= n.data.splitVals[i]) {
					foundPath = i + 1;

					break;
				}

			final Node oldChild = n.data.children[foundPath];
			final Node newChild = insert(oldChild, x, maxBranches);

			if (newChild == null) {
				n.data.deepCount++;

				return null;
			} else { // A split was performed at one level deeper.

				final int newSplit;

				if (isLeafNode(newChild))
					newSplit = newChild.values[0];
				else
					newSplit = oldChild.data.splitVals[oldChild.sliceCount - 1];

				if (n.sliceCount < maxBranches) { // There's room here.

					for (int j = n.sliceCount - 1; j > foundPath;) {
						n.data.children[j + 1] = n.data.children[j];
						n.data.splitVals[j] = n.data.splitVals[--j];
					}

					n.sliceCount++;
					n.data.deepCount++;
					n.data.children[foundPath + 1] = newChild;
					n.data.splitVals[foundPath] = newSplit;

					return null;
				} else { // No room in this internal node; perform split.

					final Node newInternalSibling = new Node(maxBranches, false);
					final int combinedCount = maxBranches + 1;
					n.sliceCount = combinedCount >> 1; // Divide by two.
					newInternalSibling.sliceCount = combinedCount - n.sliceCount;
					split(newChild, foundPath, n.data.children, newInternalSibling.data.children,
					      newInternalSibling.sliceCount);
					split(newSplit, n.data.splitVals, newInternalSibling.data.splitVals,
					      newInternalSibling.sliceCount - 1);
					n.data.deepCount = 0; // Update the deep count in both nodes.

					if (isLeafNode(newChild)) {
						for (int i = 0; i < n.sliceCount; i++)
							n.data.deepCount += n.data.children[i].sliceCount;

						for (int i = 0; i < newInternalSibling.sliceCount; i++)
							newInternalSibling.data.deepCount += newInternalSibling.data.children[i].sliceCount;
					} else {
						for (int i = 0; i < n.sliceCount; i++)
							n.data.deepCount += n.data.children[i].data.deepCount;

						for (int i = 0; i < newInternalSibling.sliceCount; i++)
							newInternalSibling.data.deepCount += newInternalSibling.data.children[i].data.deepCount;
					}

					return newInternalSibling;
				}
			}
		}
	}

	/*
	 * It's tedious to rigorously define what this method does.  I give an
	 * example:
	 *
	 *
	 *   INPUTS
	 *   ======
	 *
	 *   newVal: 5
	 *
	 *             +---+---+---+---+---+---+---+
	 *   origBuff: | 0 | 2 | 3 | 6 | 6 | 8 | 9 |
	 *             +---+---+---+---+---+---+---+
	 *
	 *                 +---+---+---+---+---+---+---+
	 *   overflowBuff: |   |   |   |   |   |   |   |
	 *                 +---+---+---+---+---+---+---+
	 *
	 *   overflowCount: 4
	 *
	 *
	 *   OUTPUTS
	 *   =======
	 *
	 *             +---+---+---+---+---+---+---+
	 *   origBuff: | 0 | 2 | 3 | 5 |   |   |   |
	 *             +---+---+---+---+---+---+---+
	 *
	 *                 +---+---+---+---+---+---+---+
	 *   overflowBuff: | 6 | 6 | 8 | 9 |   |   |   |
	 *                 +---+---+---+---+---+---+---+
	 */
	private final static void split(final int newVal, final int[] origBuff,
	                                final int[] overflowBuff, final int overflowCount) {
		int[] currentArr = overflowBuff;
		int currentInx = overflowCount;
		boolean found = false;

		for (int i = origBuff.length - 1; i >= 0; i--) {
			if ((!found) && (newVal >= origBuff[i])) {
				currentArr[--currentInx] = newVal;
				found = true;

				if (currentArr == origBuff)
					break;

				i++;
			} else {
				currentArr[--currentInx] = origBuff[i];
			}

			if (currentInx == 0) {
				if (found)
					break;

				currentArr = origBuff;
				currentInx = origBuff.length - overflowCount + 1;
			}
		}

		if (!found)
			currentArr[0] = newVal;
	}

	/*
	 * It's tedious to rigorously define what this method does.  I give an
	 * example:
	 *
	 *
	 *   INPUTS
	 *   ======
	 *
	 *   newNode: Z
	 *
	 *   newInx: 5
	 *
	 *              +---+---+---+---+---+---+---+
	 *   origNodes: | Q | I | E | A | Y | N | W |
	 *              +---+---+---+---+---+---+---+
	 *
	 *                  +---+---+---+---+---+---+---+
	 *   overflowNodes: |   |   |   |   |   |   |   |
	 *                  +---+---+---+---+---+---+---+
	 *
	 *   overflowCount: 4
	 *
	 *
	 *   OUTPUTS
	 *   =======
	 *
	 *             +---+---+---+---+---+---+---+
	 *   origBuff: | Q | I | E | A | / | / | / |
	 *             +---+---+---+---+---+---+---+
	 *
	 *                 +---+---+---+---+---+---+---+
	 *   overflowBuff: | Y | N | Z | W |   |   |   |
	 *                 +---+---+---+---+---+---+---+
	 *
	 *   In addition, the "unused" entries in origBuff are nulled out (remove
	 *   pointers to enable garbage collection).
	 *
	 *   Note that newInx means to put the new node after the existing node
	 *   at index newInx in the original array.  Placing the new node before
	 *   every other node would entail specifying newInx as -1, which is not
	 *   allowed.
	 */
	private final static void split(Node newNode, final int newInx, final Node[] origNodes,
	                                final Node[] overflowNodes, final int overflowCount) {
		Node[] currentNodes = overflowNodes;
		int currentInx = overflowCount;

		for (int i = origNodes.length - 1; i >= 0; i--) {
			if ((newNode != null) && (i == newInx)) {
				currentNodes[--currentInx] = newNode;
				newNode = null;

				if (currentNodes == origNodes)
					break;

				i++;
			} else {
				currentNodes[--currentInx] = origNodes[i];
			}

			if (currentInx == 0) {
				if (newNode == null)
					break;

				currentNodes = origNodes;
				currentInx = origNodes.length - overflowCount + 1;
			}
		}

		for (int i = origNodes.length - overflowCount + 1; i < origNodes.length; i++)
			origNodes[i] = null; // Remove dangling pointers for garbage collection.
	}

	// NOTE TO MYSELF: If we had a double-linked node structure (every
	// node points to its parent in addition to what we have now) and if
	// we could access, in constant time, the leaf node to which an entry to
	// be deleted belongs to, and if we did not store deep counts in nodes,
	// then we could implement a single deletion in constant average time
	// because the likelihood of necessary percolations (shifting and merging)
	// decreases as we head from a leaf node to a root node after a deletion.
	/**
	 * Deletes at most one entry of the integer x.  This method has a time
	 * complexity of O(log(N)) where N is the number of entries currently
	 * stored in this tree structure.<p>
	 * Unfortunately there does not seem to exists an elegant algorithm for
	 * efficiently deleting a range of integers while guaranteeing a balanced
	 * tree structure.
	 * @param x the integer to try to delete (just one entry).
	 * @return true if and only if an entry was deleted (at most one entry is
	 *   deleted by this method).
	 */
	public final boolean delete(final int x) {
		final boolean returnThis = delete(m_root, x, m_minBranches);

		if ((!isLeafNode(m_root)) && (m_root.sliceCount == 1))
			m_root = m_root.data.children[0];

		return returnThis;
	}

	private final static boolean delete(final Node n, final int x, final int minBranches) {
		if (isLeafNode(n)) {
			int foundInx = -1;

			for (int i = 0; i < n.sliceCount; i++) {
				if (x <= n.values[i]) {
					if (x == n.values[i]) {
						foundInx = i;
					}

					break;
				}
			}

			if (foundInx < 0) {
				return false;
			} else {
				fillHole(foundInx, n.values, --n.sliceCount);

				return true;
			}
		} else { // Internal node.

			int deletedPath = -1;

			for (int i = n.sliceCount - 2; i >= -1; i--) {
				int currentMin = ((i < 0) ? Integer.MIN_VALUE : n.data.splitVals[i]);

				if (currentMin <= x) {
					if (delete(n.data.children[i + 1], x, minBranches)) {
						n.data.deepCount--;
						deletedPath = i + 1;

						break;
					}

					if (currentMin < x)
						break;
				}
			}

			if (deletedPath < 0) {
				return false;
			}

			final Node affectedChild = n.data.children[deletedPath];

			if (affectedChild.sliceCount < minBranches) { // Underflow handling.

				final Node leftChild = (deletedPath > 0) ? n.data.children[deletedPath - 1] : null;
				final Node rightChild = ((deletedPath + 1) < n.sliceCount)
				                        ? n.data.children[deletedPath + 1] : null;

				if ((leftChild != null) && (leftChild.sliceCount > minBranches)) {
					n.data.splitVals[deletedPath - 1] = distributeFromLeft(leftChild,
					                                                       affectedChild,
					                                                       n.data.splitVals[deletedPath
					                                                       - 1], minBranches);
				} else if ((rightChild != null) && (rightChild.sliceCount > minBranches)) {
					n.data.splitVals[deletedPath] = distributeFromRight(rightChild, affectedChild,
					                                                    n.data.splitVals[deletedPath],
					                                                    minBranches);
				} else { // Merge with a child sibling.

					final int holeInx;

					if (leftChild != null) // Merge with left child.
						mergeSiblings(leftChild, affectedChild,
						              n.data.splitVals[holeInx = deletedPath - 1]);
					else // Merge with right child.

						mergeSiblings(affectedChild, rightChild,
						              n.data.splitVals[holeInx = deletedPath]);

					fillHole(holeInx + 1, n.data.children, --n.sliceCount);
					fillHole(holeInx, n.data.splitVals, n.sliceCount - 1);
				}
			}

			return true;
		}
	}

	/*
	 * Returns a new splitVal.  Updates counts and nulls out entries as
	 * appropriate.
	 */
	private final static int distributeFromLeft(final Node leftSibling, final Node thisSibling,
	                                            final int oldSplitVal, final int minBranches) {
		final int distributeNum = ((1 + leftSibling.sliceCount) - minBranches) / 2;

		if (isLeafNode(leftSibling)) {
			for (int i = thisSibling.sliceCount, o = i + distributeNum; i > 0;)
				thisSibling.values[--o] = thisSibling.values[--i];

			for (int i = leftSibling.sliceCount - distributeNum, o = 0; o < distributeNum;)
				thisSibling.values[o++] = leftSibling.values[i++];

			leftSibling.sliceCount -= distributeNum;
			thisSibling.sliceCount += distributeNum;

			return thisSibling.values[0];
		} else {
			final int returnThis = leftSibling.data.splitVals[leftSibling.sliceCount
			                       - distributeNum - 1];

			for (int i = thisSibling.sliceCount, o = i + distributeNum; i > 0;)
				thisSibling.data.children[--o] = thisSibling.data.children[--i];

			for (int i = leftSibling.sliceCount - distributeNum, o = 0; o < distributeNum;)
				thisSibling.data.children[o++] = leftSibling.data.children[i++];

			final boolean leafChildren = isLeafNode(leftSibling.data.children[0]);
			int deepCountDiff = 0;

			for (int i = leftSibling.sliceCount - distributeNum; i < leftSibling.sliceCount; i++) {
				deepCountDiff += (leafChildren ? leftSibling.data.children[i].sliceCount
				                               : leftSibling.data.children[i].data.deepCount);
				leftSibling.data.children[i] = null;
			}

			for (int i = thisSibling.sliceCount - 1, o = i + distributeNum; i > 0;)
				thisSibling.data.splitVals[--o] = thisSibling.data.splitVals[--i];

			thisSibling.data.splitVals[distributeNum - 1] = oldSplitVal;

			for (int i = leftSibling.sliceCount - distributeNum, o = 0; o < (distributeNum - 1);)
				thisSibling.data.splitVals[o++] = leftSibling.data.splitVals[i++];

			leftSibling.sliceCount -= distributeNum;
			thisSibling.sliceCount += distributeNum;
			leftSibling.data.deepCount -= deepCountDiff;
			thisSibling.data.deepCount += deepCountDiff;

			return returnThis;
		}
	}

	/*
	 * Returns a new splitVal.  Updates counts and nulls out entries as
	 * appropriate.
	 */
	private final static int distributeFromRight(final Node rightSibling, final Node thisSibling,
	                                             final int oldSplitVal, final int minBranches) {
		final int distributeNum = ((1 + rightSibling.sliceCount) - minBranches) / 2;

		if (isLeafNode(rightSibling)) {
			for (int i = 0, o = thisSibling.sliceCount; i < distributeNum;)
				thisSibling.values[o++] = rightSibling.values[i++];

			for (int i = 0, o = distributeNum; o < rightSibling.sliceCount;)
				rightSibling.values[i++] = rightSibling.values[o++];

			rightSibling.sliceCount -= distributeNum;
			thisSibling.sliceCount += distributeNum;

			return rightSibling.values[0];
		} else {
			final int returnThis = rightSibling.data.splitVals[distributeNum - 1];
			final boolean leafChildren = isLeafNode(rightSibling.data.children[0]);
			int deepCountDiff = 0;

			for (int i = 0, o = thisSibling.sliceCount; i < distributeNum;) {
				deepCountDiff += (leafChildren ? rightSibling.data.children[i].sliceCount
				                               : rightSibling.data.children[i].data.deepCount);
				thisSibling.data.children[o++] = rightSibling.data.children[i++];
			}

			for (int i = distributeNum, o = 0; i < rightSibling.sliceCount;)
				rightSibling.data.children[o++] = rightSibling.data.children[i++];

			for (int i = rightSibling.sliceCount - distributeNum; i < rightSibling.sliceCount;
			     i++)
				rightSibling.data.children[i] = null;

			thisSibling.data.splitVals[thisSibling.sliceCount - 1] = oldSplitVal;

			for (int i = 0, o = thisSibling.sliceCount; i < (distributeNum - 1);)
				thisSibling.data.splitVals[o++] = rightSibling.data.splitVals[i++];

			for (int i = distributeNum, o = 0; i < (rightSibling.sliceCount - 1);)
				rightSibling.data.splitVals[o++] = rightSibling.data.splitVals[i++];

			rightSibling.sliceCount -= distributeNum;
			thisSibling.sliceCount += distributeNum;
			rightSibling.data.deepCount -= deepCountDiff;
			thisSibling.data.deepCount += deepCountDiff;

			return returnThis;
		}
	}

	/*
	 * Copies into leftSibling.  You can discard rightSibling after this.
	 * Updates counts and nulls out entries as appropriate.
	 */
	private final static void mergeSiblings(final Node leftSibling, final Node rightSibling,
	                                        final int splitValue) {
		if (isLeafNode(leftSibling)) {
			for (int i = 0, o = leftSibling.sliceCount; i < rightSibling.sliceCount;)
				leftSibling.values[o++] = rightSibling.values[i++];

			leftSibling.sliceCount += rightSibling.sliceCount;
			rightSibling.sliceCount = 0; /* Pedantic. */
		} else {
			for (int i = 0, o = leftSibling.sliceCount; i < (rightSibling.sliceCount - 1);)
				leftSibling.data.splitVals[o++] = rightSibling.data.splitVals[i++];

			leftSibling.data.splitVals[leftSibling.sliceCount - 1] = splitValue;

			for (int i = 0, o = leftSibling.sliceCount; i < rightSibling.sliceCount;)
				leftSibling.data.children[o++] = rightSibling.data.children[i++];

			for (int i = 0; i < rightSibling.sliceCount; i++) {
				rightSibling.data.children[i] = null; /* Pedantic. */
			}

			leftSibling.sliceCount += rightSibling.sliceCount;
			rightSibling.sliceCount = 0; // Pedantic.
			leftSibling.data.deepCount += rightSibling.data.deepCount;
			rightSibling.data.deepCount = 0; /* Pedantic. */
		}
	}

	/*
	 * I give an example:
	 *
	 *
	 *   INPUTS
	 *   ======
	 *
	 *   holeInx: 5
	 *
	 *        +---+---+---+---+---+---+---+---+---+---+
	 *   arr: | 1 | 2 | 8 | 5 | 9 |   | 4 | 0 |   |   |
	 *        +---+---+---+---+---+---+---+---+---+---+
	 *
	 *   newLen: 7
	 *
	 *
	 *   OUTPUTS
	 *   =======
	 *
	 *        +---+---+---+---+---+---+---+---+---+---+
	 *   arr: | 1 | 2 | 8 | 5 | 9 | 4 | 0 |   |   |   |
	 *        +---+---+---+---+---+---+---+---+---+---+
	 */
	private final static void fillHole(final int holeInx, final int[] arr, final int newLen) {
		for (int i = holeInx; i < newLen;)
			arr[i] = arr[++i];
	}

	/*
	 * I give an example:
	 *
	 *
	 *   INPUTS
	 *   ======
	 *
	 *   holeInx: 1
	 *
	 *        +---+---+---+---+---+---+
	 *   arr: | H |   | L | P | Z |   |
	 *        +---+---+---+---+---+---+
	 *
	 *   newLen: 4
	 *
	 *
	 *   OUTPUTS
	 *   =======
	 *
	 *        +---+---+---+---+---+---+
	 *   arr: | H | L | P | Z | / |   |
	 *        +---+---+---+---+---+---+
	 *
	 *   The last entry (after 'Z' in example) is nulled out so that
	 *   garbage collection would not be obstructed.
	 */
	private final static void fillHole(final int holeInx, final Node[] arr, final int newLen) {
		int i = holeInx;

		while (i < newLen)
			arr[i] = arr[++i];

		arr[i] = null;
	}

	/**
	 * Returns the number of entries of the integer x in this tree.
	 * This method has a time complexity of O(log(N)) where N is the total
	 * number of entries currently in this tree structure.<p>
	 * This method is superfluous because we can use searchRange(x, x, false) to
	 * get the same information, paying the same hit in time complexity.
	 * @param x the integer whose count to query.
	 * @return the number of entries x currently in this structure.
	 */
	public final int count(final int x) {
		return count(m_root, x, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/*
	 * It's important that with every invocation of this method, we have
	 * minBound <= x <= maxBound.
	 */
	private final static int count(final Node n, final int x, final int minBound, final int maxBound) {
		int count = 0;

		if (minBound == maxBound) { // Trivially include node.
			count += (isLeafNode(n) ? n.sliceCount : n.data.deepCount);
		} else { // Cannot trivially include node; must recurse.

			if (isLeafNode(n)) {
				for (int i = 0; i < n.sliceCount; i++)
					if (x <= n.values[i]) {
						if (x == n.values[i])
							count++;
						else

							break;
					}
			} else { // Internal node.

				int currentMax = maxBound;
				int currentMin;

				for (int i = n.sliceCount - 2; i >= -1; i--) {
					currentMin = ((i < 0) ? minBound : n.data.splitVals[i]);

					if (currentMin <= x) {
						count += count(n.data.children[i + 1], x, currentMin, currentMax);

						if (currentMin < x)
							break;
					}

					currentMax = currentMin;
				}
			}
		}

		return count;
	}

	/**
	 * Returns an enumeration of all entries in the range [xMin, xMax] currently
	 * in this tree, duplicates included.  The elements of the enumeration are
	 * returned in non-descending order if the reverseOrder input parameter is
	 * false; otherwise, elements of the enumeration are returned in
	 * non-ascending order.  This method takes O(log(N)) time to compute a
	 * return value, where N is the total number of entries currently in
	 * this tree structure.  The returned enumeration reports the number of
	 * elements remaining in constant time.  The returned enumeration can
	 * be completely traversed in O(K) time, where K is the number of elements
	 * in the returned enumeration.  Note, however, that
	 * there is no guarantee that each successive element of the enumeration
	 * is returned in constant time; instead, the time complexity of accessing
	 * each successive element is only constant on average, and is in fact
	 * O(log(K)).<p>
	 * IMPORTANT: The returned enumeration becomes invalid as soon as any
	 * structure-modifying operation (insert or delete) is performed on this
	 * tree.  Accessing an invalid enumeration's methods will result in
	 * unpredictable and ill-defined behavior in that enumeration, but will
	 * have no effect on the integrity of the underlying tree structure.<p>
	 * IMPLEMENTATION NOTE: To find out how many entries are in this tree,
	 * one should use the size() method.  Doing so using this method will
	 * cost O(log(N)) time, where the size() method returns in constant time.
	 * The reason why this method takes longer is because we pre-compute the
	 * first element of the enumeration in order to reduce the complexity
	 * of the code.
	 * @param xMin the lower [inclusive] bound of the range to search.
	 * @param xMax the upper [inclusive] bound of the range to search.
	 * @param reverseOrder if false, elements returned are in non-descending
	 *   order; if true, elements returned are in non-ascending order.
	 * @return an enumeration of all entries matching this search query,
	 *   duplicates included; elements returned are in non-descending order
	 *   if reverseOrder is false; elements returned are in non-ascending order
	 *   if reverseOrder is true.
	 * @exception IllegalArgumentException if xMin is greater than xMax.
	 */
	public final IntEnumerator searchRange(final int xMin, final int xMax,
	                                       final boolean reverseOrder) {
		if (xMin > xMax)
			throw new IllegalArgumentException("xMin is greater than xMax");

		final NodeStack nodeStack = new NodeStack();

		// If instead of passing Integer.MIN_VALUE and Integer.MAX_VALUE as
		// minBound and maxBound [respectively] to the private recursive
		// searchRange() function we instead had the global minimum and maximum
		// on hand, we could save iterating through the root node's entries in
		// the case where the query interval fully contains the global range of
		// entries.  This would come at the expense of keeping a global range
		// up to date with every insertion and deletion, which is computationally
		// expensive enough that it may defeat the purpose of stated optimization.
		final int totalCount = searchRange(m_root, nodeStack, xMin, xMax, Integer.MIN_VALUE,
		                                   Integer.MAX_VALUE, reverseOrder);

		if (reverseOrder)
			return new DescendingEnumerator(totalCount, nodeStack, xMax);
		else

			return new AscendingEnumerator(totalCount, nodeStack, xMin);
	}

	/*
	 * Returns the count.  The node stack is added to, never read from.
	 * The elements added to the node stack -- leaf nodes should be iterated
	 * through and appropriate values examined; internal nodes represent
	 * regions of the tree which can be included, as whole, as part of the
	 * range query.  Every node on the returned stack will have at least one
	 * leaf entry counting towards the enumeration in the range query (this
	 * statement is important for leaf nodes).  (There is one exception to that
	 * rule: when there are no entries in this tree, an empty node may be added
	 * to the stack.)  [xMin, xMax] must intersect [minBound, maxBound] on each
	 * call to this method.
	 */
	private final static int searchRange(final Node n, final NodeStack nodeStack, final int xMin,
	                                     final int xMax, final int minBound, final int maxBound,
	                                     final boolean reverseOrder) {
		int count = 0;

		if ((minBound >= xMin) && (maxBound <= xMax)) { // Trivially include node.
			count += (isLeafNode(n) ? n.sliceCount : n.data.deepCount);
			nodeStack.push(n);
		} else { // Cannot trivially include node; must recurse.

			if (isLeafNode(n)) {
				int i = 0;

				for (; i < n.sliceCount; i++)
					if (xMin <= n.values[i])
						break;

				for (int j = i; j < n.sliceCount; j++) {
					if (n.values[j] <= xMax)
						count++;
					else

						break;
				}

				if (count > 0)
					nodeStack.push(n);
			} else { // Internal node.

				if (reverseOrder) {
					int currentMin = minBound;
					int currentMax;
					final int maxInx = n.sliceCount - 1; // Slight optimization.

					for (int i = 0; i < n.sliceCount; i++) {
						currentMax = ((i == maxInx) ? maxBound : n.data.splitVals[i]);

						if (currentMax >= xMin) {
							count += searchRange(n.data.children[i], nodeStack, xMin, xMax,
							                     currentMin, currentMax, reverseOrder);

							if (currentMax > xMax)
								break;
						}

						currentMin = currentMax;
					}
				} else { // Not reverse order.

					int currentMax = maxBound;
					int currentMin;

					for (int i = n.sliceCount - 2; i >= -1; i--) {
						currentMin = ((i < 0) ? minBound : n.data.splitVals[i]);

						if (currentMin <= xMax) {
							count += searchRange(n.data.children[i + 1], nodeStack, xMin, xMax,
							                     currentMin, currentMax, reverseOrder);

							if (currentMin < xMin)
								break;
						}

						currentMax = currentMin;
					}
				}
			}
		}

		return count;
	}

	private final static class Node implements java.io.Serializable {
	private final static long serialVersionUID = 121374594970366L;
		private int sliceCount = 0;
		private final int[] values; // null if and only if internal node.
		private final InternalNodeData data;

		private Node(final int maxBranches, final boolean leafNode) {
			if (leafNode) {
				values = new int[maxBranches];
				data = null;
			} else {
				values = null;
				data = new InternalNodeData(maxBranches);
			}
		}
	}

	private final static class InternalNodeData implements java.io.Serializable {
	private final static long serialVersionUID = 121374594986257L;
		private int deepCount;
		private final int[] splitVals;
		private final Node[] children;

		private InternalNodeData(final int maxBranches) {
			splitVals = new int[maxBranches - 1];
			children = new Node[maxBranches];
		}
	}

	private final static class NodeStack {
		private Node[] stack;
		private int currentSize = 0;

		private NodeStack() {
			stack = new Node[3];
		}

		private final void push(final Node value) {
			try {
				stack[currentSize++] = value;
			} catch (ArrayIndexOutOfBoundsException e) {
				currentSize--;

				final int newStackSize = (int) Math.min((long) Integer.MAX_VALUE,
				                                        (((long) stack.length) * 2L) + 1L);

				if (newStackSize == stack.length)
					throw new IllegalStateException("cannot allocate large enough array");

				final Node[] newStack = new Node[newStackSize];
				;
				System.arraycopy(stack, 0, newStack, 0, stack.length);
				stack = newStack;
				stack[currentSize++] = value;
			}
		}

		private final Node pop() {
			try {
				return stack[--currentSize];
			} catch (ArrayIndexOutOfBoundsException e) {
				currentSize++;
				throw e;
			}
		}
	}

	private final static class AscendingEnumerator implements IntEnumerator {
		private int wholeLeafNodes = 0; // Whole leaf nodes on stack.
		private int count;
		private final NodeStack stack;
		private final int xMin;
		private Node currentLeafNode;
		private int currentNodeInx;

		private AscendingEnumerator(final int totalCount, final NodeStack nodeStack, final int xMin) {
			count = totalCount;
			stack = nodeStack;
			this.xMin = xMin;
			computeNextLeafNode();
		}

		public final int numRemaining() {
			return count;
		}

		public final int nextInt() {
			int returnThis = 0; // To keep compiler from complaining.

			if (wholeLeafNodes != 0) // Faster than 'wholeLeafNodes > 0' ?
				returnThis = currentLeafNode.values[currentNodeInx];
			else

				for (; currentNodeInx < currentLeafNode.sliceCount; currentNodeInx++)
					if (currentLeafNode.values[currentNodeInx] >= xMin) {
						returnThis = currentLeafNode.values[currentNodeInx];

						break;
					}

			if (++currentNodeInx == currentLeafNode.sliceCount) {
				if (wholeLeafNodes > 0)
					wholeLeafNodes--;

				computeNextLeafNode();
			}

			count--;

			return returnThis;
		}

		private final void computeNextLeafNode() {
			if (stack.currentSize == 0) {
				currentLeafNode = null;

				return;
			}

			Node next;

			while (true) {
				next = stack.pop();

				if (isLeafNode(next)) {
					currentLeafNode = next;
					currentNodeInx = 0;

					return;
				}

				for (int i = next.sliceCount; i > 0;)
					stack.push(next.data.children[--i]);

				if (isLeafNode(next.data.children[0]))
					wholeLeafNodes += next.sliceCount;
			}
		}
	}

	private final static class DescendingEnumerator implements IntEnumerator {
		private int wholeLeafNodes = 0; // Whole leaf nodes on stack.
		private int count;
		private final NodeStack stack;
		private final int xMax;
		private Node currentLeafNode;
		private int currentNodeInx;

		private DescendingEnumerator(final int totalCount, final NodeStack nodeStack, final int xMax) {
			count = totalCount;
			stack = nodeStack;
			this.xMax = xMax;
			computeNextLeafNode();
		}

		public final int numRemaining() {
			return count;
		}

		public final int nextInt() {
			int returnThis = 0; // To keep compiler from complaining.

			if (wholeLeafNodes != 0) // Faster than 'wholeLeafNodes > 0' ?
				returnThis = currentLeafNode.values[currentNodeInx];
			else

				for (; currentNodeInx >= 0; currentNodeInx--)
					if (currentLeafNode.values[currentNodeInx] <= xMax) {
						returnThis = currentLeafNode.values[currentNodeInx];

						break;
					}

			if (--currentNodeInx < 0) {
				if (wholeLeafNodes > 0)
					wholeLeafNodes--;

				computeNextLeafNode();
			}

			count--;

			return returnThis;
		}

		private final void computeNextLeafNode() {
			if (stack.currentSize == 0) {
				currentLeafNode = null;

				return;
			}

			Node next;

			while (true) {
				next = stack.pop();

				if (isLeafNode(next)) {
					currentLeafNode = next;
					currentNodeInx = currentLeafNode.sliceCount - 1;

					return;
				}

				for (int i = 0; i < next.sliceCount; i++)
					stack.push(next.data.children[i]);

				if (isLeafNode(next.data.children[0]))
					wholeLeafNodes += next.sliceCount;
			}
		}
	}
}
