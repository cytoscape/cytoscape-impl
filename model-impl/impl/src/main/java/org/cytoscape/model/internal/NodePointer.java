/*
  Copyright (c) 2008, 2011, The Cytoscape Consortium (www.cytoscape.org)

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


import org.cytoscape.model.CyNode;
import java.util.Arrays; 


/**
 * Element of the edge linked list used in {@link ArrayGraph}.
 * You should only touch this if you know what you're doing!
 */
final class NodePointer {
	final CyNode cyNode;
	final int index;

	// In general, there will always be the root network,
	// the base network, and a subnetwork for visualization,
	// so we use 3 as a sensible default.
	final static int INITIAL_ALLOCATION = 3;
	NodePointer[] nextNode = new NodePointer[INITIAL_ALLOCATION];
	NodePointer[] prevNode = new NodePointer[INITIAL_ALLOCATION];
	EdgePointer[] firstOutEdge = new EdgePointer[INITIAL_ALLOCATION];
	EdgePointer[] firstInEdge = new EdgePointer[INITIAL_ALLOCATION];

	// The number of directed edges whose source is this node.
	int[] outDegree = new int[INITIAL_ALLOCATION];

	// The number of directed edges whose target is this node.
	int[] inDegree = new int[INITIAL_ALLOCATION];

	// The number of undirected edges which touch this node.
	int[] undDegree = new int[INITIAL_ALLOCATION];

	// The number of directed self-edges on this node.
	int[] selfEdges = new int[INITIAL_ALLOCATION];

	// Whether the node pointer is included in the subnetwork 
	// specified by the index of the array.
	boolean[] includes = new boolean[INITIAL_ALLOCATION];

	NodePointer(final int nodeIndex, final CyNode cyn) {
		index = nodeIndex;
		cyNode = cyn;

		outDegree[0] = 0;
		inDegree[0] = 0;
		undDegree[0] = 0;
		selfEdges[0] = 0;

		firstOutEdge[0] = null;
		firstInEdge[0] = null;

		Arrays.fill(includes,false);
	}

	void expandTo(final int z) {
		final int x = z+1;

		if (z < nextNode.length)
			return;

		nextNode = expandNodePointerArray(nextNode, x);
		prevNode = expandNodePointerArray(prevNode, x);
		firstOutEdge = EdgePointer.expandEdgePointerArray(firstOutEdge, x);
		firstInEdge = EdgePointer.expandEdgePointerArray(firstInEdge, x);
		outDegree = expandIntArray(outDegree, x);
		inDegree = expandIntArray(inDegree, x);
		undDegree = expandIntArray(undDegree, x);
		selfEdges = expandIntArray(selfEdges, x);
		includes = expandBooleanArray(includes, x);
	}

	static NodePointer[] expandNodePointerArray(final NodePointer[] np, final int n) {
		final NodePointer[] nnp = new NodePointer[n+1];
		System.arraycopy(np,0,nnp,0,np.length);
		return nnp;
	}

	static int[] expandIntArray(final int[] np, final int n) {
		final int[] nnp = new int[n+1];
		System.arraycopy(np,0,nnp,0,np.length);
		return nnp;
	}

	static boolean[] expandBooleanArray(final boolean[] np, final int n) {
		final boolean[] nnp = new boolean[n+1];
		System.arraycopy(np,0,nnp,0,np.length);
		return nnp;
	}

	NodePointer insert(final NodePointer next, final int inId) {
		includes[inId] = true;

		nextNode[inId] = next;
		if (next != null)
			next.prevNode[inId] = this;
		// return instead of:
		// next = this;
		return this;
	}

	NodePointer remove(final NodePointer first, final int inId) {
		includes[inId] = false;

		NodePointer ret = first;
		if (prevNode[inId] != null)
			prevNode[inId].nextNode[inId] = nextNode[inId];
		else
			ret = nextNode[inId];

		if (nextNode[inId] != null)
			nextNode[inId].prevNode[inId] = prevNode[inId];

		nextNode[inId] = null; // ??
		prevNode[inId] = null;
		firstOutEdge[inId] = null;
		firstInEdge[inId] = null;

		return ret;
	}

	boolean isSet(final int inId) {
		return ( inId >= 0 &&
		         inId < includes.length && 
		         includes[inId] );
	}
}
