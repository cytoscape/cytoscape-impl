
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

import org.cytoscape.spacial.SpacialIndex2D;
import org.cytoscape.spacial.SpacialEntry2DEnumerator;
import org.cytoscape.util.intr.IntEnumerator;
import org.cytoscape.util.intr.IntObjHash;
import org.cytoscape.util.intr.IntStack;

import java.util.Iterator;


/**
 * An in-memory R-tree over real numbers in two dimensions.<p>
 * An instance of this class is serializable; however, serialized instances of
 * this class should not be stored in a persistent manner because the
 * serialization implemented in this class makes no attempt at handling
 * class versioning.
 */
public final class RTree implements SpacialIndex2D, java.io.Serializable {
	public final static int DEFAULT_MAX_BRANCHES = 11;
	private final float[] m_MBR; // { xMin, yMin, xMax, yMax }.
	private final static long serialVersionUID = 1213746741383404L;
	private final int m_maxBranches;
	private final int m_minBranches;
	private Node m_root;
	private IntObjHash m_entryMap; // Keys are objKey, values are type Node,
	private final Object m_deletedEntry = ""; // except when "deleted".
	private int m_deletedEntries;
	private int m_mapExpansionThreshold;

	// These buffers are used during node splitting.
	private final int[] m_objKeyBuff;
	private final Node[] m_childrenBuff;
	private final float[] m_xMinBuff;
	private final float[] m_yMinBuff;
	private final float[] m_xMaxBuff;
	private final float[] m_yMaxBuff;
	private final float[] m_tempBuff1;
	private final float[] m_tempBuff2;
	private final float[] m_extentsStack;
	private final ObjStack m_nodeStack;

	/**
	 * Instantiates a new R-tree.  A new R-tree is empty (it has no entries).
	 */
	public RTree() {
		this(DEFAULT_MAX_BRANCHES);
	}

	/**
	 * Instantiates a new R-tree with the specified maximum branching factor.
	 * Overriding the default maximum branching factor is only useful for
	 * testing purposes; there are no performance gains to be had.
	 * @param maxBranches the maximum branching factor of this tree.
	 * @exception IllegalArgumentException if maxBranches is less than three.
	 */
	public RTree(final int maxBranches) {
		if (maxBranches < 3)
			throw new IllegalArgumentException("maxBranches is less than three");

		m_MBR = new float[] {
		            Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
		            Float.NEGATIVE_INFINITY
		        };
		m_maxBranches = maxBranches;
		m_minBranches = Math.max(2, (int) (((double) (m_maxBranches + 1)) * 0.4d));
		m_root = new Node(m_maxBranches, true);
		m_entryMap = new IntObjHash();
		m_deletedEntries = 0;
		m_mapExpansionThreshold = IntObjHash.maxCapacity(m_entryMap.size());
		m_objKeyBuff = new int[m_maxBranches + 1];
		m_childrenBuff = new Node[m_maxBranches + 1];
		m_xMinBuff = new float[m_maxBranches + 1];
		m_yMinBuff = new float[m_maxBranches + 1];
		m_xMaxBuff = new float[m_maxBranches + 1];
		m_yMaxBuff = new float[m_maxBranches + 1];
		m_tempBuff1 = new float[m_maxBranches + 1];
		m_tempBuff2 = new float[m_maxBranches + 1];

		// With a m_maxBranches of 7, m_minBranches will be 3, and such a tree of
		// depth 20 holds at least 3.4 billion entries.
		m_extentsStack = new float[20 * 7 * 4];
		m_nodeStack = new ObjStack();
	}

	/**
	 * Empties this R-tree of all entries.  This method returns in constant
	 * time (note however that garbage collection will take place in the
	 * background).
	 */
	public final void empty() {
		m_root = new Node(m_maxBranches, true);
		m_entryMap = new IntObjHash();
		m_deletedEntries = 0;
		m_mapExpansionThreshold = IntObjHash.maxCapacity(m_entryMap.size());
	}

	/**
	 * Returns the number of entries currently in this R-tree.  This method
	 * returns in constant time.<p>
	 * NOTE: To retrieve an enumeration of all entries in this R-tree, call
	 * queryOverlap() with Float.NEGATIVE_INFINITY minimum values and
	 * Float.POSITIVE_INFINITY maximum values.
	 */
	public final int size() {
		return (isLeafNode(m_root) ? m_root.entryCount : m_root.data.deepCount);
	}

	/*
	 * This gets used a lot.  This test is in the form of a function to make
	 * the code more readable (as opposed to being inlined).
	 */
	private final static boolean isLeafNode(final Node n) {
		return n.data == null;
	}

	/**
	 * Inserts a new data entry into this tree; the entry's extents
	 * are specified by the input parameters.  "Extents" is a short way
	 * of saying "minimum bounding rectangle".  The minimum bounding rectangle
	 * of an entry is axis-aligned, meaning that its sides are parallel to the
	 * axes of the data space.
	 * @param objKey a user-defined unique identifier used to refer to the entry
	 *   being inserted in later operations; this identifier must be
	 *   non-negative.
	 * @param xMin the minimum X coordinate of the entry's extents rectangle.
	 * @param yMin the minimum Y coordinate of the entry's extents rectangle.
	 * @param xMax the maximum X coordinate of the entry's extents rectangle.
	 * @param yMax the maximum Y coordinate of the entry's extents rectangle.
	 * @exception IllegalStateException if objKey is already used for an
	 *   existing entry in this R-tree.
	 * @exception IllegalArgumentException if objKey is negative,
	 *   if xMin is not less than or equal to xMax, or
	 *   if yMin is not less than or equal to yMax.
	 */
	public final void insert(final int objKey, final float xMin, final float yMin,
	                         final float xMax, final float yMax) {
		if (objKey < 0)
			throw new IllegalArgumentException("objKey is negative");

		if (!(xMin <= xMax))
			throw new IllegalArgumentException("xMin <= xMax not true");

		if (!(yMin <= yMax))
			throw new IllegalArgumentException("yMin <= yMax not true");

		if (m_entryMap.get(objKey) != null) { // Hashtable lookups are cached.

			if (m_entryMap.get(objKey) != m_deletedEntry)
				throw new IllegalStateException("objkey " + objKey + " is already in this tree");

			m_deletedEntries--;
		} // Old entry is m_deletedEntry.

		// We only allow underlying hashtable expansions if the number of deleted
		// keys in the table is one quarter or less of the total number of keys.
		else {
			if (m_entryMap.size() == m_mapExpansionThreshold) { // Expansion.

				if ((m_deletedEntries * 4) > m_entryMap.size()) { // Prune map.

					final IntObjHash newEntryMap = new IntObjHash();
					final IntEnumerator objKeys = m_entryMap.keys();
					final Iterator leafNodes = m_entryMap.values();

					while (objKeys.numRemaining() > 0) {
						final Object leafNode = leafNodes.next();

						if (leafNode == m_deletedEntry) {
							objKeys.nextInt();

							continue;
						}

						newEntryMap.put(objKeys.nextInt(), leafNode);
					}

					m_entryMap = newEntryMap;
					m_deletedEntries = 0;
				}

				m_mapExpansionThreshold = IntObjHash.maxCapacity(m_entryMap.size() + 1);
			}
		}

		final Node rootSplit = insert(m_root, objKey, xMin, yMin, xMax, yMax, m_maxBranches,
		                              m_minBranches, m_entryMap, m_MBR, m_objKeyBuff,
		                              m_childrenBuff, m_xMinBuff, m_yMinBuff, m_xMaxBuff,
		                              m_yMaxBuff, m_tempBuff1, m_tempBuff2);

		if (rootSplit != null) {
			final Node newRoot = new Node(m_maxBranches, false);
			newRoot.entryCount = 2;
			m_root.parent = newRoot;
			rootSplit.parent = newRoot;
			newRoot.data.children[0] = m_root;
			newRoot.data.children[1] = rootSplit;
			newRoot.xMins[0] = m_root.xMins[m_maxBranches - 1];
			newRoot.yMins[0] = m_root.yMins[m_maxBranches - 1];
			newRoot.xMaxs[0] = m_root.xMaxs[m_maxBranches - 1];
			newRoot.yMaxs[0] = m_root.yMaxs[m_maxBranches - 1];
			newRoot.xMins[1] = rootSplit.xMins[m_maxBranches - 1];
			newRoot.yMins[1] = rootSplit.yMins[m_maxBranches - 1];
			newRoot.xMaxs[1] = rootSplit.xMaxs[m_maxBranches - 1];
			newRoot.yMaxs[1] = rootSplit.yMaxs[m_maxBranches - 1];

			if (isLeafNode(m_root))
				newRoot.data.deepCount = m_root.entryCount + rootSplit.entryCount;
			else
				newRoot.data.deepCount = m_root.data.deepCount + rootSplit.data.deepCount;

			m_root = newRoot;
			m_MBR[0] = Math.min(m_root.xMins[0], m_root.xMins[1]);
			m_MBR[1] = Math.min(m_root.yMins[0], m_root.yMins[1]);
			m_MBR[2] = Math.max(m_root.xMaxs[0], m_root.xMaxs[1]);
			m_MBR[3] = Math.max(m_root.yMaxs[0], m_root.yMaxs[1]);
		}
	}

	/*
	 * This is the routine that inserts an entry into a leaf node.
	 * Returns a non-null node in the case that the root was split; in this
	 * case the globalMBR is not updated, and the MBR at index
	 * maxBranches - 1 in both root and the returned node will contain the
	 * overall MBR of that node.
	 */
	private final static Node insert(final Node root, final int objKey, final float xMin,
	                                 final float yMin, final float xMax, final float yMax,
	                                 final int maxBranches, final int minBranches,
	                                 final IntObjHash entryMap, final float[] globalMBR,
	                                 final int[] objKeyBuff, final Node[] childrenBuff,
	                                 final float[] xMinBuff, final float[] yMinBuff,
	                                 final float[] xMaxBuff, final float[] yMaxBuff,
	                                 final float[] tempBuff1, final float[] tempBuff2) {
		final int deepCountIncrease = 1;
		final Node chosenLeaf = chooseLeaf(root, xMin, yMin, xMax, yMax);

		if (chosenLeaf.entryCount < maxBranches) { // No split is necessary.

			final int newInx = chosenLeaf.entryCount++;
			chosenLeaf.objKeys[newInx] = objKey;
			chosenLeaf.xMins[newInx] = xMin;
			chosenLeaf.yMins[newInx] = yMin;
			chosenLeaf.xMaxs[newInx] = xMax;
			chosenLeaf.yMaxs[newInx] = yMax;
			entryMap.put(objKey, chosenLeaf);
			adjustTreeNoSplit(chosenLeaf, deepCountIncrease, globalMBR);

			return null;
		} else { // A split is necessary.

			final Node newLeaf = splitLeafNode(chosenLeaf, objKey, xMin, yMin, xMax, yMax,
			                                   maxBranches, minBranches, objKeyBuff, xMinBuff,
			                                   yMinBuff, xMaxBuff, yMaxBuff, tempBuff1, tempBuff2);

			for (int i = 0; i < chosenLeaf.entryCount; i++)
				entryMap.put(chosenLeaf.objKeys[i], chosenLeaf);

			for (int i = 0; i < newLeaf.entryCount; i++)
				entryMap.put(newLeaf.objKeys[i], newLeaf);

			return adjustTreeWithSplit(chosenLeaf, newLeaf, deepCountIncrease, maxBranches,
			                           minBranches, globalMBR, childrenBuff, xMinBuff, yMinBuff,
			                           xMaxBuff, yMaxBuff, tempBuff1, tempBuff2);
		}
	}

	/*
	 * This is the routine that re-inserts a node into the tree.
	 * Inserts specified node into a parent at specified depth.  Depth zero is
	 * defined to be the depth of the root.  Returns a non-null node in the
	 * case that the root was split; in this case the globalMBR is not
	 * updated, and the MBR at index maxBranches - 1 in both root and the
	 * returned node will contain the overall MBR of that node.
	 */
	private final static Node insert(final Node root, final int depth, final Node n,
	                                 final float xMin, final float yMin, final float xMax,
	                                 final float yMax, final int maxBranches,
	                                 final int minBranches, final float[] globalMBR,
	                                 final Node[] childrenBuff, final float[] xMinBuff,
	                                 final float[] yMinBuff, final float[] xMaxBuff,
	                                 final float[] yMaxBuff, final float[] tempBuff1,
	                                 final float[] tempBuff2) {
		final int deepCountIncrease = (isLeafNode(n) ? n.entryCount : n.data.deepCount);
		final Node chosenParent = chooseParent(root, depth, xMin, yMin, xMax, yMax);

		if (chosenParent.entryCount < maxBranches) { // No split is necessary.

			final int newInx = chosenParent.entryCount++;
			n.parent = chosenParent;
			chosenParent.data.children[newInx] = n;
			chosenParent.xMins[newInx] = xMin;
			chosenParent.yMins[newInx] = yMin;
			chosenParent.xMaxs[newInx] = xMax;
			chosenParent.yMaxs[newInx] = yMax;
			chosenParent.data.deepCount += deepCountIncrease;
			adjustTreeNoSplit(chosenParent, deepCountIncrease, globalMBR);

			return null;
		} else { // A split is necessary.

			final Node parentSibling = splitInternalNode(chosenParent, n, xMin, yMin, xMax, yMax,
			                                             maxBranches, minBranches, childrenBuff,
			                                             xMinBuff, yMinBuff, xMaxBuff, yMaxBuff,
			                                             tempBuff1, tempBuff2);

			return adjustTreeWithSplit(chosenParent, parentSibling, deepCountIncrease, maxBranches,
			                           minBranches, globalMBR, childrenBuff, xMinBuff, yMinBuff,
			                           xMaxBuff, yMaxBuff, tempBuff1, tempBuff2);
		}
	}

	/*
	 * Returns a leaf node.  The returned leaf node is chosen by this
	 * algorithm as the most suitable leaf node [under specified root] in
	 * which to place specified new entry.
	 */
	private final static Node chooseLeaf(final Node root, final float xMin, final float yMin,
	                                     final float xMax, final float yMax) {
		Node n = root;

		while (!isLeafNode(n))
			n = n.data.children[chooseSubtree(n, xMin, yMin, xMax, yMax)];

		return n;
	}

	/*
	 * The root is defined to be at depth zero.  This function returns a node
	 * at specified depth such that the returned node is the most suitable such
	 * node in which to place specified MBR.
	 */
	private final static Node chooseParent(final Node root, final int depth, final float xMin,
	                                       final float yMin, final float xMax, final float yMax) {
		Node n = root;
		int currDepth = 0;

		while (currDepth != depth) {
			n = n.data.children[chooseSubtree(n, xMin, yMin, xMax, yMax)];
			currDepth++;
		}

		return n;
	}

	/*
	 * Returns the index of entry in n whose rectangular boundary
	 * needs least enlargment to swallow the input rectangle.  Ties are resolved
	 * by choosing the entry with the rectangle of smallest area.
	 */
	private final static int chooseSubtree(final Node n, final float xMin, final float yMin,
	                                       final float xMax, final float yMax) {
		float bestAreaDelta = Float.POSITIVE_INFINITY;
		float bestArea = Float.POSITIVE_INFINITY;
		int bestInx = -1;

		for (int i = 0; i < n.entryCount; i++) {
			// A possible optimization would be to add to each node an area cache
			// for each entry.  That way we wouldn't have to compute this area on
			// each insertion.
			final float currArea = (n.xMaxs[i] - n.xMins[i]) * (n.yMaxs[i] - n.yMins[i]);
			final float newArea = (Math.max(n.xMaxs[i], xMax) - Math.min(n.xMins[i], xMin)) * (Math
			                                                                                   .max(n.yMaxs[i],
			                                                                                        yMax)
			                                                                                  - Math
			                                                                                    .min(n.yMins[i],
			                                                                                         yMin));
			final float currAreaDelta = newArea - currArea;

			if ((currAreaDelta < bestAreaDelta)
			    || ((currAreaDelta == bestAreaDelta) && (currArea < bestArea))) {
				bestAreaDelta = currAreaDelta;
				bestArea = currArea;
				bestInx = i;
			}
		}

		return bestInx;
	}

	/*
	 * This is the quadratic-cost algorithm described in Guttman's 1984
	 * R-tree paper.  The parent pointer of returned node is not set.  The
	 * parent pointer in the full node is not modified, and nothing in that
	 * parent is modified.  Everything else in the input node and in the
	 * returned node is set as appropriate.  The MBRs at index
	 * maxBranches - 1 in both nodes are set to be the new overall MBR of
	 * corresponding node.  The node returned is also a leaf node.
	 * No claim is made as to the resulting values in the buff arrays.
	 */
	private final static Node splitLeafNode(final Node fullLeafNode, final int newObjKey,
	                                        final float newXMin, final float newYMin,
	                                        final float newXMax, final float newYMax,
	                                        final int maxBranches, final int minBranches,
	                                        final int[] objKeyBuff, final float[] xMinBuff,
	                                        final float[] yMinBuff, final float[] xMaxBuff,
	                                        final float[] yMaxBuff, final float[] tempBuff1,
	                                        final float[] tempBuff2) {
		// Copy node MBRs and objKeys and new MBR and objKey into arrays.
		for (int i = 0; i < fullLeafNode.entryCount; i++) {
			objKeyBuff[i] = fullLeafNode.objKeys[i];
			xMinBuff[i] = fullLeafNode.xMins[i];
			yMinBuff[i] = fullLeafNode.yMins[i];
			xMaxBuff[i] = fullLeafNode.xMaxs[i];
			yMaxBuff[i] = fullLeafNode.yMaxs[i];
		}

		objKeyBuff[fullLeafNode.entryCount] = newObjKey;
		xMinBuff[fullLeafNode.entryCount] = newXMin;
		yMinBuff[fullLeafNode.entryCount] = newYMin;
		xMaxBuff[fullLeafNode.entryCount] = newXMax;
		yMaxBuff[fullLeafNode.entryCount] = newYMax;

		// Pick seeds.  Add seeds to two groups (fullLeafNode and returnThis).
		final int totalEntries = fullLeafNode.entryCount + 1;
		final long seeds = pickSeeds(totalEntries, xMinBuff, yMinBuff, xMaxBuff, yMaxBuff, tempBuff1);

		// tempBuff1 now contains the areas of the MBRs - we won't use this.
		final int seed1 = (int) (seeds >> 32);
		fullLeafNode.objKeys[0] = objKeyBuff[seed1];
		fullLeafNode.xMins[0] = xMinBuff[seed1];
		fullLeafNode.yMins[0] = yMinBuff[seed1];
		fullLeafNode.xMaxs[0] = xMaxBuff[seed1];
		fullLeafNode.yMaxs[0] = yMaxBuff[seed1];
		fullLeafNode.entryCount = 1;

		final int seed2 = (int) seeds;
		final Node returnThis = new Node(maxBranches, true);
		returnThis.objKeys[0] = objKeyBuff[seed2];
		returnThis.xMins[0] = xMinBuff[seed2];
		returnThis.yMins[0] = yMinBuff[seed2];
		returnThis.xMaxs[0] = xMaxBuff[seed2];
		returnThis.yMaxs[0] = yMaxBuff[seed2];
		returnThis.entryCount = 1;

		// Initialize the overall MBRs at index maxBranches - 1.
		fullLeafNode.xMins[maxBranches - 1] = fullLeafNode.xMins[0];
		fullLeafNode.yMins[maxBranches - 1] = fullLeafNode.yMins[0];
		fullLeafNode.xMaxs[maxBranches - 1] = fullLeafNode.xMaxs[0];
		fullLeafNode.yMaxs[maxBranches - 1] = fullLeafNode.yMaxs[0];
		returnThis.xMins[maxBranches - 1] = returnThis.xMins[0];
		returnThis.yMins[maxBranches - 1] = returnThis.yMins[0];
		returnThis.xMaxs[maxBranches - 1] = returnThis.xMaxs[0];
		returnThis.yMaxs[maxBranches - 1] = returnThis.yMaxs[0];

		// Plug the holes where seeds used to be.
		int entriesRemaining = totalEntries;

		if (seed2 != --entriesRemaining) { // seed1 < seed2, guaranteed.
			objKeyBuff[seed2] = objKeyBuff[entriesRemaining];
			xMinBuff[seed2] = xMinBuff[entriesRemaining];
			yMinBuff[seed2] = yMinBuff[entriesRemaining];
			xMaxBuff[seed2] = xMaxBuff[entriesRemaining];
			yMaxBuff[seed2] = yMaxBuff[entriesRemaining];
		}

		if (seed1 != --entriesRemaining) {
			objKeyBuff[seed1] = objKeyBuff[entriesRemaining];
			xMinBuff[seed1] = xMinBuff[entriesRemaining];
			yMinBuff[seed1] = yMinBuff[entriesRemaining];
			xMaxBuff[seed1] = xMaxBuff[entriesRemaining];
			yMaxBuff[seed1] = yMaxBuff[entriesRemaining];
		}

		boolean buff1Valid = false;
		boolean buff2Valid = false;

		while (true) {
			// Test to see if we're all done.
			if (entriesRemaining == 0)
				break;

			final Node restGroup;

			if ((entriesRemaining + fullLeafNode.entryCount) == minBranches)
				restGroup = fullLeafNode;
			else if ((entriesRemaining + returnThis.entryCount) == minBranches)
				restGroup = returnThis;
			else
				restGroup = null;

			if (restGroup != null) { // Assign remaining entries to this group.

				for (int i = 0; i < entriesRemaining; i++) {
					// Add entry to "rest" group.
					final int newInx = restGroup.entryCount++;
					restGroup.objKeys[newInx] = objKeyBuff[i];
					restGroup.xMins[newInx] = xMinBuff[i];
					restGroup.yMins[newInx] = yMinBuff[i];
					restGroup.xMaxs[newInx] = xMaxBuff[i];
					restGroup.yMaxs[newInx] = yMaxBuff[i];

					// Update the overall MBR of "rest" group.
					restGroup.xMins[maxBranches - 1] = Math.min(restGroup.xMins[maxBranches - 1],
					                                            xMinBuff[i]);
					restGroup.yMins[maxBranches - 1] = Math.min(restGroup.yMins[maxBranches - 1],
					                                            yMinBuff[i]);
					restGroup.xMaxs[maxBranches - 1] = Math.max(restGroup.xMaxs[maxBranches - 1],
					                                            xMaxBuff[i]);
					restGroup.yMaxs[maxBranches - 1] = Math.max(restGroup.yMaxs[maxBranches - 1],
					                                            yMaxBuff[i]);
				}

				break;
			}

			// We're not done; pick next.
			final int next = pickNext(fullLeafNode, returnThis, entriesRemaining, maxBranches,
			                          xMinBuff, yMinBuff, xMaxBuff, yMaxBuff, tempBuff1,
			                          buff1Valid, tempBuff2, buff2Valid);
			final boolean chooseGroup1;

			if (tempBuff1[next] < tempBuff2[next])
				chooseGroup1 = true;
			else if (tempBuff1[next] > tempBuff2[next])
				chooseGroup1 = false;
			else { // Tie for how much group's covering rectangle will increase.
				   // If we had an area cache array field in each node we could prevent
				   // these two computations.

				final float group1Area = (fullLeafNode.xMaxs[maxBranches - 1]
				                         - fullLeafNode.xMins[maxBranches - 1]) * (fullLeafNode.yMaxs[maxBranches
				                                                                  - 1]
				                                                                  - fullLeafNode.yMins[maxBranches
				                                                                  - 1]);
				final float group2Area = (returnThis.xMaxs[maxBranches - 1]
				                         - returnThis.xMins[maxBranches - 1]) * (returnThis.yMaxs[maxBranches
				                                                                - 1]
				                                                                - returnThis.yMins[maxBranches
				                                                                - 1]);

				if (group1Area < group2Area)
					chooseGroup1 = true;
				else if (group1Area > group2Area)
					chooseGroup1 = false;
				else if (fullLeafNode.entryCount < returnThis.entryCount)
					chooseGroup1 = true;
				else
					chooseGroup1 = false;
			}

			final Node chosenGroup;
			final float[] validTempBuff;

			if (chooseGroup1) {
				chosenGroup = fullLeafNode;
				validTempBuff = tempBuff2;
				buff1Valid = false;
				buff2Valid = true;
			} else {
				chosenGroup = returnThis;
				validTempBuff = tempBuff1;
				buff1Valid = true;
				buff2Valid = false;
			}

			// Add next to chosen group.
			final int newInx = chosenGroup.entryCount++;
			chosenGroup.objKeys[newInx] = objKeyBuff[next];
			chosenGroup.xMins[newInx] = xMinBuff[next];
			chosenGroup.yMins[newInx] = yMinBuff[next];
			chosenGroup.xMaxs[newInx] = xMaxBuff[next];
			chosenGroup.yMaxs[newInx] = yMaxBuff[next];

			// Update the MBR of chosen group.
			// Note: If we see that the MBR stays the same, we could mark the
			// "invalid" temp buff array as valid to save even more on computations.
			// Because this is a rare occurance (seeds of small area tend to be
			// chosen), I choose not to make this optimization.
			chosenGroup.xMins[maxBranches - 1] = Math.min(chosenGroup.xMins[maxBranches - 1],
			                                              xMinBuff[next]);
			chosenGroup.yMins[maxBranches - 1] = Math.min(chosenGroup.yMins[maxBranches - 1],
			                                              yMinBuff[next]);
			chosenGroup.xMaxs[maxBranches - 1] = Math.max(chosenGroup.xMaxs[maxBranches - 1],
			                                              xMaxBuff[next]);
			chosenGroup.yMaxs[maxBranches - 1] = Math.max(chosenGroup.yMaxs[maxBranches - 1],
			                                              yMaxBuff[next]);

			// Plug the hole where next used to be.
			if (next != --entriesRemaining) {
				objKeyBuff[next] = objKeyBuff[entriesRemaining];
				xMinBuff[next] = xMinBuff[entriesRemaining];
				yMinBuff[next] = yMinBuff[entriesRemaining];
				xMaxBuff[next] = xMaxBuff[entriesRemaining];
				yMaxBuff[next] = yMaxBuff[entriesRemaining];
				validTempBuff[next] = validTempBuff[entriesRemaining];
			}
		} // End while loop.

		return returnThis;
	}

	/*
	 * This is the quadratic-cost algorithm described in Guttman's 1984
	 * R-tree paper.  The parent pointer of returned node is not set.  The
	 * parent pointer in the full node is not modified, and nothing in that
	 * parent is modified.  Everything else in the input node and in the
	 * returned node is set as appropriate (deep count, etc.).  The MBRs at index
	 * maxBranches - 1 in both nodes are set to be the new overall MBR of
	 * corresponding node.  To picture what this function does, imagine
	 * adding newChild (with specified MBR) to fullInternalNode.  Note that
	 * newChild may be either an internal node or a leaf node.
	 * No claim is made as to the resulting values in the buff arrays other
	 * than the claim that all entries in childrenBuff will be null when this
	 * method returns.
	 */
	private final static Node splitInternalNode(final Node fullInternalNode, final Node newChild,
	                                            final float newXMin, final float newYMin,
	                                            final float newXMax, final float newYMax,
	                                            final int maxBranches, final int minBranches,
	                                            final Node[] childrenBuff, final float[] xMinBuff,
	                                            final float[] yMinBuff, final float[] xMaxBuff,
	                                            final float[] yMaxBuff, final float[] tempBuff1,
	                                            final float[] tempBuff2) {
		// Copy node MBRs and children and new MBR and child into arrays.
		for (int i = 0; i < fullInternalNode.entryCount; i++) {
			childrenBuff[i] = fullInternalNode.data.children[i];
			xMinBuff[i] = fullInternalNode.xMins[i];
			yMinBuff[i] = fullInternalNode.yMins[i];
			xMaxBuff[i] = fullInternalNode.xMaxs[i];
			yMaxBuff[i] = fullInternalNode.yMaxs[i];
		}

		childrenBuff[fullInternalNode.entryCount] = newChild;
		xMinBuff[fullInternalNode.entryCount] = newXMin;
		yMinBuff[fullInternalNode.entryCount] = newYMin;
		xMaxBuff[fullInternalNode.entryCount] = newXMax;
		yMaxBuff[fullInternalNode.entryCount] = newYMax;

		// Pick seeds.  Add seeds to two groups (fullInternalNode and returnThis).
		final int totalEntries = fullInternalNode.entryCount + 1;
		final long seeds = pickSeeds(totalEntries, xMinBuff, yMinBuff, xMaxBuff, yMaxBuff, tempBuff1);

		// tempBuff1 now contains the areas of the MBRs - we won't use this.
		final int seed1 = (int) (seeds >> 32);
		childrenBuff[seed1].parent = fullInternalNode;
		fullInternalNode.data.children[0] = childrenBuff[seed1];
		fullInternalNode.xMins[0] = xMinBuff[seed1];
		fullInternalNode.yMins[0] = yMinBuff[seed1];
		fullInternalNode.xMaxs[0] = xMaxBuff[seed1];
		fullInternalNode.yMaxs[0] = yMaxBuff[seed1];
		fullInternalNode.entryCount = 1;

		final int seed2 = (int) seeds;
		final Node returnThis = new Node(maxBranches, false);
		childrenBuff[seed2].parent = returnThis;
		returnThis.data.children[0] = childrenBuff[seed2];
		returnThis.xMins[0] = xMinBuff[seed2];
		returnThis.yMins[0] = yMinBuff[seed2];
		returnThis.xMaxs[0] = xMaxBuff[seed2];
		returnThis.yMaxs[0] = yMaxBuff[seed2];
		returnThis.entryCount = 1;

		// Initialize the overall MBRs at index maxBranches - 1.
		fullInternalNode.xMins[maxBranches - 1] = fullInternalNode.xMins[0];
		fullInternalNode.yMins[maxBranches - 1] = fullInternalNode.yMins[0];
		fullInternalNode.xMaxs[maxBranches - 1] = fullInternalNode.xMaxs[0];
		fullInternalNode.yMaxs[maxBranches - 1] = fullInternalNode.yMaxs[0];
		returnThis.xMins[maxBranches - 1] = returnThis.xMins[0];
		returnThis.yMins[maxBranches - 1] = returnThis.yMins[0];
		returnThis.xMaxs[maxBranches - 1] = returnThis.xMaxs[0];
		returnThis.yMaxs[maxBranches - 1] = returnThis.yMaxs[0];

		// Plug the holes where seeds used to be.
		int entriesRemaining = totalEntries;

		if (seed2 != --entriesRemaining) { // seed1 < seed2, guaranteed.
			childrenBuff[seed2] = childrenBuff[entriesRemaining];
			xMinBuff[seed2] = xMinBuff[entriesRemaining];
			yMinBuff[seed2] = yMinBuff[entriesRemaining];
			xMaxBuff[seed2] = xMaxBuff[entriesRemaining];
			yMaxBuff[seed2] = yMaxBuff[entriesRemaining];
		}

		if (seed1 != --entriesRemaining) {
			childrenBuff[seed1] = childrenBuff[entriesRemaining];
			xMinBuff[seed1] = xMinBuff[entriesRemaining];
			yMinBuff[seed1] = yMinBuff[entriesRemaining];
			xMaxBuff[seed1] = xMaxBuff[entriesRemaining];
			yMaxBuff[seed1] = yMaxBuff[entriesRemaining];
		}

		boolean buff1Valid = false;
		boolean buff2Valid = false;

		while (true) {
			// Test to see if we're all done.
			if (entriesRemaining == 0)
				break;

			final Node restGroup;

			if ((entriesRemaining + fullInternalNode.entryCount) == minBranches)
				restGroup = fullInternalNode;
			else if ((entriesRemaining + returnThis.entryCount) == minBranches)
				restGroup = returnThis;
			else
				restGroup = null;

			if (restGroup != null) { // Assign remaining entries to this group.

				for (int i = 0; i < entriesRemaining; i++) {
					// Add entry to "rest" group.
					final int newInx = restGroup.entryCount++;
					childrenBuff[i].parent = restGroup;
					restGroup.data.children[newInx] = childrenBuff[i];
					restGroup.xMins[newInx] = xMinBuff[i];
					restGroup.yMins[newInx] = yMinBuff[i];
					restGroup.xMaxs[newInx] = xMaxBuff[i];
					restGroup.yMaxs[newInx] = yMaxBuff[i];

					// Update the overall MBR of "rest" group.
					restGroup.xMins[maxBranches - 1] = Math.min(restGroup.xMins[maxBranches - 1],
					                                            xMinBuff[i]);
					restGroup.yMins[maxBranches - 1] = Math.min(restGroup.yMins[maxBranches - 1],
					                                            yMinBuff[i]);
					restGroup.xMaxs[maxBranches - 1] = Math.max(restGroup.xMaxs[maxBranches - 1],
					                                            xMaxBuff[i]);
					restGroup.yMaxs[maxBranches - 1] = Math.max(restGroup.yMaxs[maxBranches - 1],
					                                            yMaxBuff[i]);
				}

				break;
			}

			// We're not done; pick next.
			final int next = pickNext(fullInternalNode, returnThis, entriesRemaining, maxBranches,
			                          xMinBuff, yMinBuff, xMaxBuff, yMaxBuff, tempBuff1,
			                          buff1Valid, tempBuff2, buff2Valid);
			final boolean chooseGroup1;

			if (tempBuff1[next] < tempBuff2[next])
				chooseGroup1 = true;
			else if (tempBuff1[next] > tempBuff2[next])
				chooseGroup1 = false;
			else { // Tie for how much group's covering rectangle will increase.
				   // If we had an area cache array field in each node we could prevent
				   // these two computations.

				final float group1Area = (fullInternalNode.xMaxs[maxBranches - 1]
				                         - fullInternalNode.xMins[maxBranches - 1]) * (fullInternalNode.yMaxs[maxBranches
				                                                                      - 1]
				                                                                      - fullInternalNode.yMins[maxBranches
				                                                                      - 1]);
				final float group2Area = (returnThis.xMaxs[maxBranches - 1]
				                         - returnThis.xMins[maxBranches - 1]) * (returnThis.yMaxs[maxBranches
				                                                                - 1]
				                                                                - returnThis.yMins[maxBranches
				                                                                - 1]);

				if (group1Area < group2Area)
					chooseGroup1 = true;
				else if (group1Area > group2Area)
					chooseGroup1 = false;
				else if (fullInternalNode.entryCount < returnThis.entryCount)
					chooseGroup1 = true;
				else
					chooseGroup1 = false;
			}

			final Node chosenGroup;
			final float[] validTempBuff;

			if (chooseGroup1) {
				chosenGroup = fullInternalNode;
				validTempBuff = tempBuff2;
				buff1Valid = false;
				buff2Valid = true;
			} else {
				chosenGroup = returnThis;
				validTempBuff = tempBuff1;
				buff1Valid = true;
				buff2Valid = false;
			}

			// Add next to chosen group.
			final int newInx = chosenGroup.entryCount++;
			childrenBuff[next].parent = chosenGroup;
			chosenGroup.data.children[newInx] = childrenBuff[next];
			chosenGroup.xMins[newInx] = xMinBuff[next];
			chosenGroup.yMins[newInx] = yMinBuff[next];
			chosenGroup.xMaxs[newInx] = xMaxBuff[next];
			chosenGroup.yMaxs[newInx] = yMaxBuff[next];

			// Update the MBR of chosen group.
			// Note: If we see that the MBR stays the same, we could mark the
			// "invalid" temp buff array as valid to save even more on computations.
			// Because this is a rare occurance (seeds of small area tend to be
			// chosen), I choose not to make this optimization.
			chosenGroup.xMins[maxBranches - 1] = Math.min(chosenGroup.xMins[maxBranches - 1],
			                                              xMinBuff[next]);
			chosenGroup.yMins[maxBranches - 1] = Math.min(chosenGroup.yMins[maxBranches - 1],
			                                              yMinBuff[next]);
			chosenGroup.xMaxs[maxBranches - 1] = Math.max(chosenGroup.xMaxs[maxBranches - 1],
			                                              xMaxBuff[next]);
			chosenGroup.yMaxs[maxBranches - 1] = Math.max(chosenGroup.yMaxs[maxBranches - 1],
			                                              yMaxBuff[next]);

			// Plug the hole where next used to be.
			if (next != --entriesRemaining) {
				childrenBuff[next] = childrenBuff[entriesRemaining];
				xMinBuff[next] = xMinBuff[entriesRemaining];
				yMinBuff[next] = yMinBuff[entriesRemaining];
				xMaxBuff[next] = xMaxBuff[entriesRemaining];
				yMaxBuff[next] = yMaxBuff[entriesRemaining];
				validTempBuff[next] = validTempBuff[entriesRemaining];
			}
		} // End while loop.

		fullInternalNode.data.deepCount = 0; // Update deep counts.

		if (isLeafNode(fullInternalNode.data.children[0])) {
			for (int i = 0; i < fullInternalNode.entryCount; i++)
				fullInternalNode.data.deepCount += fullInternalNode.data.children[i].entryCount;

			for (int i = 0; i < returnThis.entryCount; i++)
				returnThis.data.deepCount += returnThis.data.children[i].entryCount;
		} else { // fullInternalNode's children are internal nodes.

			for (int i = 0; i < fullInternalNode.entryCount; i++)
				fullInternalNode.data.deepCount += fullInternalNode.data.children[i].data.deepCount;

			for (int i = 0; i < returnThis.entryCount; i++)
				returnThis.data.deepCount += returnThis.data.children[i].data.deepCount;
		}

		// Null things out so as to not hinder future garbage collection.
		for (int i = fullInternalNode.entryCount; i < fullInternalNode.data.children.length; i++)
			fullInternalNode.data.children[i] = null;

		for (int i = 0; i < childrenBuff.length; i++)
			childrenBuff[i] = null;

		return returnThis;
	}

	/*
	 * This is the quadratic-cost algorithm described by Guttman.
	 * The first seed's index is returned as the 32 most significant bits
	 * of returned quantity.  The second seed's index is returned as the 32
	 * least significant bits of returned quantity.  The first seed's index
	 * is closer to zero than the second seed's index.  None of the input
	 * arrays are modified except for tempBuff.  tempBuff is populated with
	 * the areas of the MBRs.
	 */
	private final static long pickSeeds(final int count, final float[] xMins, final float[] yMins,
	                                    final float[] xMaxs, final float[] yMaxs,
	                                    final float[] tempBuff) {
		for (int i = 0; i < count; i++)
			tempBuff[i] = (xMaxs[i] - xMins[i]) * (yMaxs[i] - yMins[i]); // Area.

		float maximumD = Float.NEGATIVE_INFINITY;
		int maximumInx1 = -1;
		int maximumInx2 = -1;

		for (int i = 0; i < (count - 1); i++)
			for (int j = i + 1; j < count; j++) {
				final float areaJ = (Math.max(xMaxs[i], xMaxs[j]) - Math.min(xMins[i], xMins[j])) * (Math
				                                                                                     .max(yMaxs[i],
				                                                                                          yMaxs[j])
				                                                                                    - Math
				                                                                                      .min(yMins[i],
				                                                                                           yMins[j]));
				final float d = areaJ - tempBuff[i] - tempBuff[j];

				if (d > maximumD) {
					maximumD = d;
					maximumInx1 = i;
					maximumInx2 = j;
				}
			}

		return (((long) maximumInx1) << 32) | ((long) maximumInx2);
	}

	/*
	 * Returns the index (in xMins, etc.) of next entry to add to a group.
	 * The arrays tempBuff1 and tempBuff2 are used to store the [positive]
	 * area increase required in respective groups to swallow corresponding
	 * MBR at same index.  If buff1Valid is true then tempBuff1 already
	 * contains this information and it need not be computed by this
	 * method.  Analagous is true for buff2Valid and tempBuff2.  The nodes
	 * group1 and group2 are only used by this method to read information
	 * of current MBR of corresponding group - the MBR is stored at index
	 * m_maxBranches - 1.  None of the input variables are modified except
	 * for tempBuff1 and tempBuff2.
	 */
	private final static int pickNext(final Node group1, final Node group2, final int count,
	                                  final int maxBranches, final float[] xMins,
	                                  final float[] yMins, final float[] xMaxs,
	                                  final float[] yMaxs, final float[] tempBuff1,
	                                  final boolean buff1Valid, final float[] tempBuff2,
	                                  final boolean buff2Valid) {
		if (!buff1Valid) {
			// If we had an area cache array field in each node we could prevent
			// this computation.
			final float group1Area = (group1.xMaxs[maxBranches - 1] - group1.xMins[maxBranches - 1]) * (group1.yMaxs[maxBranches
			                                                                                           - 1]
			                                                                                           - group1.yMins[maxBranches
			                                                                                           - 1]);

			for (int i = 0; i < count; i++) {
				tempBuff1[i] = ((Math.max(group1.xMaxs[maxBranches - 1], xMaxs[i])
				                - Math.min(group1.xMins[maxBranches - 1], xMins[i])) * (Math.max(group1.yMaxs[maxBranches
				                                                                                 - 1],
				                                                                                 yMaxs[i])
				                                                                       - Math.min(group1.yMins[maxBranches
				                                                                                  - 1],
				                                                                                  yMins[i])))
				               - group1Area;
			}
		}

		if (!buff2Valid) {
			// If we had an area cache array field in each node we could prevent
			// this computation.      
			final float group2Area = (group2.xMaxs[maxBranches - 1] - group2.xMins[maxBranches - 1]) * (group2.yMaxs[maxBranches
			                                                                                           - 1]
			                                                                                           - group2.yMins[maxBranches
			                                                                                           - 1]);

			for (int i = 0; i < count; i++) {
				tempBuff2[i] = ((Math.max(group2.xMaxs[maxBranches - 1], xMaxs[i])
				                - Math.min(group2.xMins[maxBranches - 1], xMins[i])) * (Math.max(group2.yMaxs[maxBranches
				                                                                                 - 1],
				                                                                                 yMaxs[i])
				                                                                       - Math.min(group2.yMins[maxBranches
				                                                                                  - 1],
				                                                                                  yMins[i])))
				               - group2Area;
			}
		}

		float maxDDifference = Float.NEGATIVE_INFINITY;
		int maxInx = -1;

		for (int i = 0; i < count; i++) {
			final float currDDifference = Math.abs(tempBuff1[i] - tempBuff2[i]);

			if (currDDifference > maxDDifference) {
				maxDDifference = currDDifference;
				maxInx = i;
			}
		}

		return maxInx;
	}

	/*
	 * This method can only be used to adjust a tree after inserting a single
	 * new entry or node into nodeWithNewEntry.  It is assumed that the new entry
	 * or node in nodeWithNewEntry is at index nodeWithNewEntry.entryCount - 1.
	 * We will use this knowledge to optimize this function.  Deep counts are
	 * updated from nodeWithNewEntry's parent to root.
	 */
	private final static void adjustTreeNoSplit(final Node nodeWithNewEntry,
	                                            final int deepCountIncrease, final float[] globalMBR) {
		int currModInx = nodeWithNewEntry.entryCount - 1;
		Node n = nodeWithNewEntry;

		while (true) {
			final Node p = n.parent;

			// "If N is the root, stop."  Adjust the globalMBR.
			if (p == null) {
				if (currModInx >= 0) {
					globalMBR[0] = Math.min(globalMBR[0], n.xMins[currModInx]);
					globalMBR[1] = Math.min(globalMBR[1], n.yMins[currModInx]);
					globalMBR[2] = Math.max(globalMBR[2], n.xMaxs[currModInx]);
					globalMBR[3] = Math.max(globalMBR[3], n.yMaxs[currModInx]);
				}

				break;
			}

			// Update the deep count.
			p.data.deepCount += deepCountIncrease;

			if (currModInx >= 0) {
				final int nInxInP;

				for (int i = 0;; i++)
					if (p.data.children[i] == n) {
						nInxInP = i;

						break;
					}

				// Compute the MBR that tightly encloses all entries in n.
				final float newXMin = Math.min(p.xMins[nInxInP], n.xMins[currModInx]);
				final float newYMin = Math.min(p.yMins[nInxInP], n.yMins[currModInx]);
				final float newXMax = Math.max(p.xMaxs[nInxInP], n.xMaxs[currModInx]);
				final float newYMax = Math.max(p.yMaxs[nInxInP], n.yMaxs[currModInx]);

				// If the overall MBR of n does not change, we don't need to
				// update any further MBRs, just deep counts.
				if ((newXMin == p.xMins[nInxInP]) && (newYMin == p.yMins[nInxInP])
				    && (newXMax == p.xMaxs[nInxInP]) && (newYMax == p.yMaxs[nInxInP])) {
					currModInx = -1;
				}
				else { // n's overall MBR did increase in size.
					p.xMins[nInxInP] = newXMin;
					p.yMins[nInxInP] = newYMin;
					p.xMaxs[nInxInP] = newXMax;
					p.yMaxs[nInxInP] = newYMax;
					currModInx = nInxInP;
				}
			}

			n = p;
		}
	}

	/*
	 * It is required that the MBRs at index maxBranches - 1 in both
	 * input nodes contain the overall MBR of corresponding node.
	 * Returns a node if root was split, otherwise returns null.
	 * If a node is returned, then both the old root and the returned node
	 * will have an MBR entry at index maxBranches - 1 which will be the
	 * overall MBR of that node.  The globalMBR is only updated when null
	 * is returned.  Deep counts are updated from leaf to root.  This method
	 * puts newNode into the tree by trying to insert it as a child into
	 * originalNode's parent - if it does not fit, the parent is split,
	 * and the split may go recursively upwards towards the root.
	 */
	private final static Node adjustTreeWithSplit(final Node originalNode, final Node newNode,
	                                              final int deepCountIncrease,
	                                              final int maxBranches, final int minBranches,
	                                              final float[] globalMBR,
	                                              final Node[] childrenBuff,
	                                              final float[] xMinBuff, final float[] yMinBuff,
	                                              final float[] xMaxBuff, final float[] yMaxBuff,
	                                              final float[] tempBuff1, final float[] tempBuff2) {
		int currModInx = -1;
		boolean newNodeAdded = false; // New node added as last entry in n?
		                              // (Only when nn is null.)

		Node n = originalNode;
		Node nn = newNode;

		while (true) {
			final Node p = n.parent;

			// "If N is the root, stop."  Update globalMBR if root not split.
			if (p == null) {
				if ((nn == null) && (currModInx >= 0)) {
					globalMBR[0] = Math.min(globalMBR[0], n.xMins[currModInx]);
					globalMBR[1] = Math.min(globalMBR[1], n.yMins[currModInx]);
					globalMBR[2] = Math.max(globalMBR[2], n.xMaxs[currModInx]);
					globalMBR[3] = Math.max(globalMBR[3], n.yMaxs[currModInx]);

					if (newNodeAdded) { // Will only be true when currModInx >= 0.

						final int countMin1 = n.entryCount - 1;
						globalMBR[0] = Math.min(globalMBR[0], n.xMins[countMin1]);
						globalMBR[1] = Math.min(globalMBR[1], n.yMins[countMin1]);
						globalMBR[2] = Math.max(globalMBR[2], n.xMaxs[countMin1]);
						globalMBR[3] = Math.max(globalMBR[3], n.yMaxs[countMin1]);
					}
				}

				break;
			}

			// Update the deep count.  Will get rewritten if p is split - that's OK.
			p.data.deepCount += deepCountIncrease;

			// Node n was split into two in previous iterative step.
			if (nn != null) {
				final int nInxInP; // Only compute this if we need it.

				for (int i = 0;; i++)
					if (p.data.children[i] == n) {
						nInxInP = i;

						break;
					}

				p.xMins[nInxInP] = n.xMins[maxBranches - 1]; // A split implies
				p.yMins[nInxInP] = n.yMins[maxBranches - 1]; // overall MBR at inx
				p.xMaxs[nInxInP] = n.xMaxs[maxBranches - 1]; // maxBranches - 1.
				p.yMaxs[nInxInP] = n.yMaxs[maxBranches - 1];

				if (p.entryCount < maxBranches) { // No further split is necessary.

					final int newInxInP = p.entryCount++;
					nn.parent = p;
					p.data.children[newInxInP] = nn;
					p.xMins[newInxInP] = nn.xMins[maxBranches - 1]; // A split implies
					p.yMins[newInxInP] = nn.yMins[maxBranches - 1]; // overall MBR at inx
					p.xMaxs[newInxInP] = nn.xMaxs[maxBranches - 1]; // maxBranches - 1.
					p.yMaxs[newInxInP] = nn.yMaxs[maxBranches - 1];

					// The recursive step.
					currModInx = nInxInP;
					newNodeAdded = true;
					nn = null;
				}
				else { // A split is necessary as the iterative step.
					   // We require that the MBR at index maxBranches - 1 in nn contain
					   // nn's overall MBR at the time this is called.
					nn = splitInternalNode(p, nn, nn.xMins[maxBranches - 1],
					                       nn.yMins[maxBranches - 1], nn.xMaxs[maxBranches - 1],
					                       nn.yMaxs[maxBranches - 1], maxBranches, minBranches,
					                       childrenBuff, xMinBuff, yMinBuff, xMaxBuff, yMaxBuff,
					                       tempBuff1, tempBuff2);
				}
			}
			// Node n was not split into two in previous step, but the updating
			// of the MBR has percolated up to this level.
			else if (currModInx >= 0) { // nn == null.

				final int nInxInP; // Only compute this if we need it.

				for (int i = 0;; i++)
					if (p.data.children[i] == n) {
						nInxInP = i;

						break;
					}

				// Compute the new overall MBR for n (stored in n's parent).
				float newXMin = Math.min(p.xMins[nInxInP], n.xMins[currModInx]);
				float newYMin = Math.min(p.yMins[nInxInP], n.yMins[currModInx]);
				float newXMax = Math.max(p.xMaxs[nInxInP], n.xMaxs[currModInx]);
				float newYMax = Math.max(p.yMaxs[nInxInP], n.yMaxs[currModInx]);

				if (newNodeAdded) { // Nodes added always as last index.

					final int countMin1 = n.entryCount - 1;
					newXMin = Math.min(newXMin, n.xMins[countMin1]);
					newYMin = Math.min(newYMin, n.yMins[countMin1]);
					newXMax = Math.max(newXMax, n.xMaxs[countMin1]);
					newYMax = Math.max(newYMax, n.yMaxs[countMin1]);
					newNodeAdded = false;
				}

				if ((newXMin == p.xMins[nInxInP]) && (newYMin == p.yMins[nInxInP])
				    && (newXMax == p.xMaxs[nInxInP]) && (newYMax == p.yMaxs[nInxInP])) {
					currModInx = -1;
				} else {
					p.xMins[nInxInP] = newXMin;
					p.yMins[nInxInP] = newYMin;
					p.xMaxs[nInxInP] = newXMax;
					p.yMaxs[nInxInP] = newYMax;
					currModInx = nInxInP;
				}
			}

			n = p;
		} // End while loop.

		return nn;
	}

	/**
	 * Determines whether or not a given entry exists in this R-tree structure,
	 * and conditionally retrieves the extents of that entry.  The parameter
	 * extentsArr is written into by this method only if it is not null
	 * and if objKey exists in this R-tree.  The information written into
	 * extentsArr consists of the minimum bounding rectangle (MBR) of objKey:
	 * <blockquote><table border="1" cellpadding="5" cellspacing="0">
	 *   <tr>  <th>array index</th>  <th>value if objKey exists</th>  </tr>
	 *   <tr>  <td>offset</td>       <td>xMin of MBR</td>             </tr>
	 *   <tr>  <td>offset+1</td>     <td>yMin of MBR</td>             </tr>
	 *   <tr>  <td>offset+2</td>     <td>xMax of MBR</td>             </tr>
	 *   <tr>  <td>offset+3</td>     <td>yMax of MBR</td>             </tr>
	 * </table></blockquote>
	 * The values written into extentsArr are exactly the same ones that
	 * were previously passed to insert() using the same objKey.
	 * @param objKey a user-defined identifier that was [potentially] used in
	 *   a previous insertion.
	 * @param extentsArr an array to which extent values will be written by this
	 *   method; may be null.
	 * @param offset specifies the beginning index of where to write extent
	 *   values into extentsArr; exactly four entries are written starting at
	 *   this index (see above table); if extentsArr is null then this offset
	 *   is ignored.
	 * @return true if and only if objKey was previously inserted into this
	 *   R-tree and has not since been deleted.
	 * @exception ArrayIndexOutOfBoundsException if objKey exists, if
	 *   extentsArr is not null, and if extentsArr cannot be written
	 *   to in the index range [offset, offset+3].
	 */
	public final boolean exists(final int objKey, final float[] extentsArr, final int offset) {
		if (objKey < 0)
			return false;

		final Object o = m_entryMap.get(objKey);

		if ((o == null) || (o == m_deletedEntry))
			return false;

		if (extentsArr != null) {
			final Node n = (Node) o;
			int i = -1;

			while (n.objKeys[++i] != objKey)
				;

			extentsArr[offset] = n.xMins[i];
			extentsArr[offset + 1] = n.yMins[i];
			extentsArr[offset + 2] = n.xMaxs[i];
			extentsArr[offset + 3] = n.yMaxs[i];
		}

		return true;
	}

	/**
	 * Deletes the specified data entry from this tree.
	 * @param objKey a user-defined identifier that was potentially used in a
	 *   previous insertion.
	 * @return true if and only if objKey existed in this R-tree prior to this
	 *   method invocation.
	 */
	public final boolean delete(final int objKey) {
		if (objKey < 0)
			return false;

		final Node n; // Todo: Rename 'n' to 'leafNode'.

		{
			final Object o = m_entryMap.get(objKey);

			if ((o == null) || (o == m_deletedEntry))
				return false;

			n = (Node) o;
		}

		// Delete record from leaf node.
		final int delInx;

		for (int i = 0;; i++)
			if (n.objKeys[i] == objKey) {
				delInx = i;

				break;
			}

		n.entryCount--;

		if (delInx != n.entryCount) { // Plug the hole at index delInx.
			n.objKeys[delInx] = n.objKeys[n.entryCount];
			n.xMins[delInx] = n.xMins[n.entryCount];
			n.yMins[delInx] = n.yMins[n.entryCount];
			n.xMaxs[delInx] = n.xMaxs[n.entryCount];
			n.yMaxs[delInx] = n.yMaxs[n.entryCount];
		}

		// Fix up the tree from leaf to root.
		int currentDepth = condenseTree(n, 1, m_nodeStack, m_minBranches, m_MBR)
		                   - m_nodeStack.size() + 1;

		while (m_nodeStack.size() > 0) {
			final Node eliminatedNode = (Node) m_nodeStack.pop();

			for (int i = 0; i < eliminatedNode.entryCount; i++) {
				final Node rootSplit;

				if (isLeafNode(eliminatedNode)) {
					rootSplit = insert(m_root, eliminatedNode.objKeys[i], eliminatedNode.xMins[i],
					                   eliminatedNode.yMins[i], eliminatedNode.xMaxs[i],
					                   eliminatedNode.yMaxs[i], m_maxBranches, m_minBranches,
					                   m_entryMap, m_MBR, m_objKeyBuff, m_childrenBuff, m_xMinBuff,
					                   m_yMinBuff, m_xMaxBuff, m_yMaxBuff, m_tempBuff1, m_tempBuff2);
				} else {
					rootSplit = insert(m_root, currentDepth, eliminatedNode.data.children[i],
					                   eliminatedNode.xMins[i], eliminatedNode.yMins[i],
					                   eliminatedNode.xMaxs[i], eliminatedNode.yMaxs[i],
					                   m_maxBranches, m_minBranches, m_MBR, m_childrenBuff,
					                   m_xMinBuff, m_yMinBuff, m_xMaxBuff, m_yMaxBuff, m_tempBuff1,
					                   m_tempBuff2);
					eliminatedNode.data.children[i] = null; /* Facilitate gc. */
				}

				if (rootSplit != null) {
					final Node newRoot = new Node(m_maxBranches, false);
					newRoot.entryCount = 2;
					m_root.parent = newRoot;
					rootSplit.parent = newRoot;
					newRoot.data.children[0] = m_root;
					newRoot.data.children[1] = rootSplit;
					newRoot.xMins[0] = m_root.xMins[m_maxBranches - 1];
					newRoot.yMins[0] = m_root.yMins[m_maxBranches - 1];
					newRoot.xMaxs[0] = m_root.xMaxs[m_maxBranches - 1];
					newRoot.yMaxs[0] = m_root.yMaxs[m_maxBranches - 1];
					newRoot.xMins[1] = rootSplit.xMins[m_maxBranches - 1];
					newRoot.yMins[1] = rootSplit.yMins[m_maxBranches - 1];
					newRoot.xMaxs[1] = rootSplit.xMaxs[m_maxBranches - 1];
					newRoot.yMaxs[1] = rootSplit.yMaxs[m_maxBranches - 1];
					newRoot.data.deepCount = m_root.data.deepCount + rootSplit.data.deepCount;
					m_root = newRoot;
					m_MBR[0] = Math.min(m_root.xMins[0], m_root.xMins[1]);
					m_MBR[1] = Math.min(m_root.yMins[0], m_root.yMins[1]);
					m_MBR[2] = Math.max(m_root.xMaxs[0], m_root.xMaxs[1]);
					m_MBR[3] = Math.max(m_root.yMaxs[0], m_root.yMaxs[1]);
					currentDepth++;
				}
			}

			currentDepth++;
		} // End while loop.

		// If the root node has only one child, make the child the new root.
		if ((!isLeafNode(m_root)) && (m_root.entryCount == 1)) {
			final Node newRoot = m_root.data.children[0];
			newRoot.parent = null;
			m_root = newRoot;
		}

		// Finally, delete the objKey from m_entryMap.
		// If m_entryMap contains too many deleted entries, prune.
		m_entryMap.put(objKey, m_deletedEntry);

		if (((++m_deletedEntries * 2) > m_entryMap.size()) && (m_deletedEntries > 5)) {
			final IntObjHash newEntryMap = new IntObjHash();
			final IntEnumerator objKeys = m_entryMap.keys();
			final Iterator leafNodes = m_entryMap.values();

			while (objKeys.numRemaining() > 0) {
				final Object leafNode = leafNodes.next();

				if (leafNode == m_deletedEntry) {
					objKeys.nextInt();

					continue;
				}

				newEntryMap.put(objKeys.nextInt(), leafNode);
			}

			m_entryMap = newEntryMap;
			m_deletedEntries = 0;
			m_mapExpansionThreshold = IntObjHash.maxCapacity(m_entryMap.size());
		}

		return true;
	}

	/*
	 * This does not re-insert orphaned nodes and entries - instead, the
	 * stack eliminatedNodes is populated so that the caller of this function
	 * can re-insert.  eliminatedNodes should be empty when this function is
	 * called.  This method is used for adjusting a tree after deleting one
	 * or more entries or children from nodeWithDeletions.  Deep counts are
	 * updated from nodeWithDeletions' parent to root.
	 * Returns the distance (height) from nodeWithDeletions to root.
	 */
	private final static int condenseTree(final Node nodeWithDeletions, int deepCountDecrease,
	                                      final ObjStack eliminatedNodes, final int minBranches,
	                                      final float[] globalMBR) {
		int depth = 0;
		boolean updateMBR = true;
		Node n = nodeWithDeletions;

		while (true) {
			final Node p = n.parent;

			// If N is the root, adjust the globalMBR and stop.
			if (p == null) { // n is the root.

				if (updateMBR) {
					globalMBR[0] = Float.POSITIVE_INFINITY;
					globalMBR[1] = Float.POSITIVE_INFINITY;
					globalMBR[2] = Float.NEGATIVE_INFINITY;
					globalMBR[3] = Float.NEGATIVE_INFINITY;

					for (int i = 0; i < n.entryCount; i++) {
						globalMBR[0] = Math.min(globalMBR[0], n.xMins[i]);
						globalMBR[1] = Math.min(globalMBR[1], n.yMins[i]);
						globalMBR[2] = Math.max(globalMBR[2], n.xMaxs[i]);
						globalMBR[3] = Math.max(globalMBR[3], n.yMaxs[i]);
					}
				}

				break;
			}

			// Compute n's index in p.
			final int nInxInP;

			for (int i = 0;; i++)
				if (p.data.children[i] == n) {
					nInxInP = i;

					break;
				}

			// If n is underfull, eliminate it.
			if (n.entryCount < minBranches) { // Delete n from p.
				p.entryCount--;

				if (nInxInP != p.entryCount) { // Plug the hole at index nInxInP.
					p.data.children[nInxInP] = p.data.children[p.entryCount];
					p.xMins[nInxInP] = p.xMins[p.entryCount];
					p.yMins[nInxInP] = p.yMins[p.entryCount];
					p.xMaxs[nInxInP] = p.xMaxs[p.entryCount];
					p.yMaxs[nInxInP] = p.yMaxs[p.entryCount];
				}

				p.data.children[p.entryCount] = null; // Important for gc.
				n.parent = null; // For some strange reason removing this line will
				                 // cause OutOfMemoryError in the basic quiet test.

				eliminatedNodes.push(n);
				deepCountDecrease += (isLeafNode(n) ? n.entryCount : n.data.deepCount);
			}
			// Keep n and adjust MBRs if necessary.
			else { // n has not been eliminated.  Adjust covering rectangle.

				if (updateMBR) {
					final float oldXMin = p.xMins[nInxInP];
					final float oldYMin = p.yMins[nInxInP];
					final float oldXMax = p.xMaxs[nInxInP];
					final float oldYMax = p.yMaxs[nInxInP];
					p.xMins[nInxInP] = Float.POSITIVE_INFINITY;
					p.yMins[nInxInP] = Float.POSITIVE_INFINITY;
					p.xMaxs[nInxInP] = Float.NEGATIVE_INFINITY;
					p.yMaxs[nInxInP] = Float.NEGATIVE_INFINITY;

					for (int i = 0; i < n.entryCount; i++) {
						p.xMins[nInxInP] = Math.min(p.xMins[nInxInP], n.xMins[i]);
						p.yMins[nInxInP] = Math.min(p.yMins[nInxInP], n.yMins[i]);
						p.xMaxs[nInxInP] = Math.max(p.xMaxs[nInxInP], n.xMaxs[i]);
						p.yMaxs[nInxInP] = Math.max(p.yMaxs[nInxInP], n.yMaxs[i]);
					}

					if ((oldXMin == p.xMins[nInxInP]) && (oldYMin == p.yMins[nInxInP])
					    && (oldXMax == p.xMaxs[nInxInP]) && (oldYMax == p.yMaxs[nInxInP]))
						updateMBR = false;
				}
			}

			// Update deep count and make the necessary recursive steps.
			p.data.deepCount -= deepCountDecrease;
			n = p;
			depth++;
		} // End while loop.

		return depth;
	}

	/**
	 * Returns an enumeration of entries whose extents intersect the
	 * specified axis-aligned rectangular area.  By "axis-aligned" I mean that
	 * the query rectangle's sides are parallel to the axes of the data
	 * space.<p>
	 * The parameter extentsArr is written into by this method if it is not null.
	 * It provides a way for this method to communicate additional information
	 * to the caller of this method.  If not null, extentsArr is populated with
	 * information regarding the minimum bounding rectangle (MBR) that contains
	 * all returned entries.  The following table describes what is written to
	 * extentsArr if it is not null:
	 * <blockquote><table border="1" cellpadding="5" cellspacing="0">
	 *   <tr>  <th>array index</th>  <th>value if query generates results</th>
	 *           <th>value if query does not generate results</th>  </tr>
	 *   <tr>  <td>offset</td>       <td>xMin of MBR</td>
	 *           <td>Float.POSITIVE_INFINITY</td>                   </tr>
	 *   <tr>  <td>offset+1</td>     <td>yMin of MBR</td>
	 *           <td>Float.POSITIVE_INFINITY</td>                   </tr>
	 *   <tr>  <td>offset+2</td>     <td>xMax of MBR</td>
	 *           <td>Float.NEGATIVE_INFINITY</td>                   </tr>
	 *   <tr>  <td>offset+3</td>     <td>yMax of MBR</td>
	 *           <td>Float.NEGATIVE_INFINITY</td>                   </tr>
	 * </table></blockquote><p>
	 * This R-tree has the subquery order-preserving property, which can
	 * be described as follows.  Suppose we query the R-tree using this
	 * queryOverlap() method, specifying the
	 * maximum possible query rectangle (spanned by Float.NEGATIVE_INFINITY and
	 * Float.POSITIVE_INFINITY values); we get back all entries currently
	 * in this R-tree; the entries returned have a certain order to them.
	 * Let's remember this order.  Now if we immediately perform any further
	 * query,
	 * specifying a different (or the same) query rectangle, then the
	 * entries that are returned in the query are returned in the same order
	 * as they were returned in the "all" query.  This phenomenon continues
	 * to hold true with any additional queries until the R-tree undergoes
	 * a mutating operation such as insert() or delete().  The order of entries
	 * returned is in fact a left-to-right order in the underlying tree
	 * structure; in the future, if additional query methods are implemented,
	 * then they will also return entries in this same order.<p>
	 * IMPORTANT: The returned enumeration becomes invalid as soon as any
	 * structure-modifying operation (insert or delete) is performed on this
	 * R-tree.  Accessing an invalid enumeration's methods will result in
	 * unpredictable and ill-defined behavior in that enumeration, but will
	 * have no effect on the integrity of the underlying tree structure.<p>
	 * NOTE: It may be possible to provide a more optimized version of this
	 * algorithm for point queries.  Such a public method may be a future
	 * addition to this class.
	 * @param xMin the minimum X coordinate of the query rectangle.
	 * @param yMin the minimum Y coordinate of the query rectangle.
	 * @param xMax the maximum X coordinate of the query rectangle.
	 * @param yMax the maximum Y coordinate of the query rectangle.
	 * @param extentsArr an array to which
	 *   extent values will be written by this method; may be null.
	 * @param offset specifies the beginning index of where to write extent
	 *   values into extentsArr; exactly four entries are written starting at
	 *   this index (see table above); if extentsArr is null then this offset
	 *   is ignored.
	 * @param reverse if true, the order in which the query hits
	 *   are returned is reversed.
	 * @return a non-null enumeration of all [distinct] R-tree entries
	 *   (objKeys) whose extents intersect the specified rectangular query area.
	 * @exception IllegalArgumentException if xMin is not less than or equal to
	 *   xMax or if yMin is not less than or equal to yMax.
	 * @exception ArrayIndexOutOfBoundsException if extentsArr is not null
	 *   and if it cannot be written to in the index range
	 *   [offset, offset+3].
	 */
	public final SpacialEntry2DEnumerator queryOverlap(final float xMin, final float yMin,
	                                                   final float xMax, final float yMax,
	                                                   final float[] extentsArr, final int offset,
	                                                   final boolean reverse) {
		if (!(xMin <= xMax))
			throw new IllegalArgumentException("xMin <= xMax not true");

		if (!(yMin <= yMax))
			throw new IllegalArgumentException("yMin <= yMax not true");

		if (extentsArr != null) {
			extentsArr[offset] = Float.POSITIVE_INFINITY;
			extentsArr[offset + 1] = Float.POSITIVE_INFINITY;
			extentsArr[offset + 2] = Float.NEGATIVE_INFINITY;
			extentsArr[offset + 3] = Float.NEGATIVE_INFINITY;
		}

		m_nodeStack.push(m_root); // This stack should always be left empty after
		                          // previous uses.

		m_extentsStack[0] = m_MBR[0];
		m_extentsStack[1] = m_MBR[1];
		m_extentsStack[2] = m_MBR[2];
		m_extentsStack[3] = m_MBR[3];

		final ObjStack nodeStack = new ObjStack();
		final ObjStack stackStack = new ObjStack();
		final int totalCount = queryOverlap(m_nodeStack, m_extentsStack, nodeStack, stackStack,
		                                    xMin, yMin, xMax, yMax, extentsArr, offset, reverse);

		// m_nodeStack will now be empty.
		return new OverlapEnumerator(totalCount, nodeStack, stackStack, reverse);
	}

	/*
	 * Returns the number of entries under n that overlap specified query
	 * rectangle.  Nodes are added to the node stack - internal nodes added
	 * recursively contain only overlapping entries, and leaf nodes added
	 * should be iterated through to find overlapping entries.
	 * (In fact internal nodes added to the node
	 * stack are completely contained within specified query rectangle.)
	 * An important property is that every node on the returned node stack
	 * will recursively contain at least one entry that overlaps the
	 * query rectangle, unless n is completely empty.  If n is completely
	 * empty, it is expected that its MBR [represented by xMinN, yMinN,
	 * xMaxN, and yMaxN] be the infinite inverted rectangle (that is, its
	 * min values should all be Float.POSITIVE_INFINITY and its max values
	 * should all be Float.NEGATIVE_INFINITY).
	 * I'd like to discuss stackStack.  Objects of type IntStack are tossed onto
	 * this stack (in other words, stackStack is a stack of IntStack).  For every
	 * leaf node on nodeStack, stackStack will contain
	 * a corresponding IntStack - if the IntStack is null,
	 * then every entry in that leaf node overlaps the query rectangle; if
	 * the IntStack is of positive length, then the IntStack contains indices of
	 * entries that overlap the query rectangle.
	 */
	private final static int queryOverlap(final ObjStack unprocessedNodes, final float[] extStack,
	                                      final ObjStack nodeStack, final ObjStack stackStack,
	                                      final float xMinQ, final float yMinQ, final float xMaxQ,
	                                      final float yMaxQ, final float[] extents, final int off,
	                                      final boolean reverse) { // Depth first search.

		final int incr = reverse ? (-1) : 1;
		int count = 0;
		int extOff = 4; // Into extStack.

		while (unprocessedNodes.size() > 0) {
			final Node n = (Node) unprocessedNodes.pop();
			extOff -= 4;

			if ((xMinQ <= extStack[extOff]) // Rectangle Q contains
			    && (xMaxQ >= extStack[extOff + 2]) // rectangle N - trivially
			    && (yMinQ <= extStack[extOff + 1]) // include node.
			    && (yMaxQ >= extStack[extOff + 3])) {
				if (isLeafNode(n)) {
					count += n.entryCount;
					stackStack.push(null);
				} else {
					count += n.data.deepCount;
				}

				nodeStack.push(n);

				if (extents != null) {
					extents[off] = Math.min(extents[off], extStack[extOff]);
					extents[off + 1] = Math.min(extents[off + 1], extStack[extOff + 1]);
					extents[off + 2] = Math.max(extents[off + 2], extStack[extOff + 2]);
					extents[off + 3] = Math.max(extents[off + 3], extStack[extOff + 3]);
				}
			} else { // Cannot trivially include node; must recurse.

				if (isLeafNode(n)) {
					final IntStack stack = new IntStack();

					for (int cntr = n.entryCount, i = reverse ? 0 : (n.entryCount - 1); cntr > 0;
					     cntr--, i -= incr) {
						// Overlaps test of two rectangles.
						if ((Math.max(xMinQ, n.xMins[i]) <= Math.min(xMaxQ, n.xMaxs[i]))
						    && (Math.max(yMinQ, n.yMins[i]) <= Math.min(yMaxQ, n.yMaxs[i]))) {
							stack.push(i);

							if (extents != null) {
								extents[off] = Math.min(extents[off], n.xMins[i]);
								extents[off + 1] = Math.min(extents[off + 1], n.yMins[i]);
								extents[off + 2] = Math.max(extents[off + 2], n.xMaxs[i]);
								extents[off + 3] = Math.max(extents[off + 3], n.yMaxs[i]);
							}
						}
					}

					if (stack.size() > 0) {
						count += stack.size();
						stackStack.push(stack);
						nodeStack.push(n);
					}
				} else { // Internal node.

					for (int cntr = n.entryCount, i = reverse ? (n.entryCount - 1) : 0; cntr > 0;
					     cntr--, i += incr) {
						// Overlaps test of two rectangles.
						if ((Math.max(xMinQ, n.xMins[i]) <= Math.min(xMaxQ, n.xMaxs[i]))
						    && (Math.max(yMinQ, n.yMins[i]) <= Math.min(yMaxQ, n.yMaxs[i]))) {
							unprocessedNodes.push(n.data.children[i]);
							extStack[extOff++] = n.xMins[i];
							extStack[extOff++] = n.yMins[i];
							extStack[extOff++] = n.xMaxs[i];
							extStack[extOff++] = n.yMaxs[i];
						}
					}
				}
			}
		}

		return count;
	}

	private final static class Node implements java.io.Serializable {
	private final static long serialVersionUID = 1213746741283564L;
		private Node parent;
		private int entryCount = 0;
		private final float[] xMins;
		private final float[] yMins;
		private final float[] xMaxs;
		private final float[] yMaxs;
		private final int[] objKeys; // null if and only if internal node.
		private final InternalNodeData data;

		private Node(final int maxBranches, final boolean leafNode) {
			xMins = new float[maxBranches];
			yMins = new float[maxBranches];
			xMaxs = new float[maxBranches];
			yMaxs = new float[maxBranches];

			if (leafNode) {
				objKeys = new int[maxBranches];
				data = null;
			} else {
				objKeys = null;
				data = new InternalNodeData(maxBranches);
			}
		}
	}

	private final static class InternalNodeData implements java.io.Serializable {
	private final static long serialVersionUID = 1213746741362592L;
		private int deepCount = 0;
		private final Node[] children;

		private InternalNodeData(final int maxBranches) {
			children = new Node[maxBranches];
		}
	}

	private final static class OverlapEnumerator implements SpacialEntry2DEnumerator {
		private int count;
		private final ObjStack nodeStack;
		private final ObjStack stackStack;
		private final boolean reverse;
		private final int inxIncr;
		private Node currentLeafNode;
		private IntStack currentStack;
		private int currentInx;
		private int boundaryInx;

		private OverlapEnumerator(final int totalCount, final ObjStack nodeStack,
		                          final ObjStack stackStack, final boolean reverse) {
			count = totalCount;
			this.nodeStack = nodeStack;
			this.stackStack = stackStack;
			this.reverse = reverse;
			inxIncr = this.reverse ? (-1) : 1;
			computeNextLeafNode();
		}

		public final int numRemaining() {
			return count;
		}

		public final int nextExtents(final float[] extentsArr, final int offset) {
			final Node leaf = currentLeafNode;
			final int inx;

			if (currentStack == null) {
				inx = currentInx;
				currentInx += inxIncr;

				if (currentInx == boundaryInx)
					computeNextLeafNode();
			} else {
				inx = currentStack.pop();

				if (currentStack.size() == 0)
					computeNextLeafNode();
			}

			count--;
			extentsArr[offset] = leaf.xMins[inx];
			extentsArr[offset + 1] = leaf.yMins[inx];
			extentsArr[offset + 2] = leaf.xMaxs[inx];
			extentsArr[offset + 3] = leaf.yMaxs[inx];

			return leaf.objKeys[inx];
		}

		public final int nextInt() {
			int returnThis = -1;

			if (currentStack == null) {
				returnThis = currentLeafNode.objKeys[currentInx];
				currentInx += inxIncr;

				if (currentInx == boundaryInx) {
					computeNextLeafNode();
				}
			} else {
				returnThis = currentLeafNode.objKeys[currentStack.pop()];

				if (currentStack.size() == 0) {
					computeNextLeafNode();
				}
			}

			count--;

			return returnThis;
		}

		private final void computeNextLeafNode() {
			if (nodeStack.size() == 0) {
				currentLeafNode = null;
				currentStack = null;

				return;
			}

			Node next;

			while (true) {
				next = (Node) nodeStack.pop();

				if (isLeafNode(next)) {
					currentLeafNode = next;
					currentStack = (IntStack) stackStack.pop(); // May be null.

					if (currentStack == null) { // Otherwise these vars are ignored.

						if (reverse) {
							currentInx = currentLeafNode.entryCount - 1;
							boundaryInx = -1;
						} else {
							currentInx = 0;
							boundaryInx = currentLeafNode.entryCount;
						}
					}

					return;
				}

				for (int cntr = next.entryCount, i = reverse ? 0 : (next.entryCount - 1); cntr > 0;
				     cntr--, i -= inxIncr) {
					// This 'if' statement could be taken out of 'for' loop for speed.
					if (isLeafNode(next.data.children[i]))
						stackStack.push(null);

					nodeStack.push(next.data.children[i]);
				}
			}
		}
	}
}
