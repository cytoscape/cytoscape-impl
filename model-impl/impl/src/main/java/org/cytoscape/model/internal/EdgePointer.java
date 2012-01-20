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


import org.cytoscape.model.CyEdge;
import java.util.Arrays; 


/**
 * Element of the edge linked list used in {@link ArrayGraph}.
 * You should only touch this if you know what you're doing.
 */
final class EdgePointer {
	final CyEdge cyEdge;
	final int index;
	final boolean directed;
	final NodePointer source;
	final NodePointer target;

	// See comments in NodePointer for explanation of INITIAL_ALLOCATION
	EdgePointer[] nextOutEdge = new EdgePointer[NodePointer.INITIAL_ALLOCATION];
	EdgePointer[] prevOutEdge = new EdgePointer[NodePointer.INITIAL_ALLOCATION];
	EdgePointer[] nextInEdge = new EdgePointer[NodePointer.INITIAL_ALLOCATION];
	EdgePointer[] prevInEdge = new EdgePointer[NodePointer.INITIAL_ALLOCATION];

	boolean[] includes = new boolean[NodePointer.INITIAL_ALLOCATION];

	EdgePointer(final NodePointer s, final NodePointer t, final boolean dir, final int ind, final CyEdge edge) {
		index = ind;
		source = s;
		target = t;
		directed = dir;
		cyEdge = edge;

		nextOutEdge[0] = null;
		prevOutEdge[0] = null;

		nextInEdge[0] = null;
		prevInEdge[0] = null;

		Arrays.fill(includes,false);
	}

	void expandTo(final int z) {
		final int x = z+1;

		if (z < nextOutEdge.length)
			return;

		nextOutEdge = expandEdgePointerArray(nextOutEdge, x);
		prevOutEdge = expandEdgePointerArray(prevOutEdge, x);
		nextInEdge = expandEdgePointerArray(nextInEdge, x);
		prevInEdge = expandEdgePointerArray(prevInEdge, x);
		includes = NodePointer.expandBooleanArray(includes,x);
	}

	static EdgePointer[] expandEdgePointerArray(final EdgePointer[] np, final int n) {
		final EdgePointer[] nnp = new EdgePointer[n+1];
		System.arraycopy(np,0,nnp,0,np.length);
		return nnp;
	}

	void insert(final int inId) {
		includes[inId] = true;

		nextOutEdge[inId] = source.firstOutEdge[inId];

		if (source.firstOutEdge[inId] != null)
			source.firstOutEdge[inId].prevOutEdge[inId] = this;

		source.firstOutEdge[inId] = this;

		nextInEdge[inId] = target.firstInEdge[inId];

		if (target.firstInEdge[inId] != null)
			target.firstInEdge[inId].prevInEdge[inId] = this;

		target.firstInEdge[inId] = this;

		if (directed) {
			source.outDegree[inId]++;
			target.inDegree[inId]++;
		} else {
			source.undDegree[inId]++;
			target.undDegree[inId]++;
		}

		// Self-edge
		if (source == target) {
			if (directed) {
				source.selfEdges[inId]++;
			} else {
				source.undDegree[inId]--;
			}
		}
	}

	void remove(final int inId) {
		includes[inId] = false;

		if (prevOutEdge[inId] != null)
			prevOutEdge[inId].nextOutEdge[inId] = nextOutEdge[inId];
		else
			source.firstOutEdge[inId] = nextOutEdge[inId];

		if (nextOutEdge[inId] != null)
			nextOutEdge[inId].prevOutEdge[inId] = prevOutEdge[inId];

		if (prevInEdge[inId] != null)
			prevInEdge[inId].nextInEdge[inId] = nextInEdge[inId];
		else
			target.firstInEdge[inId] = nextInEdge[inId];

		if (nextInEdge[inId] != null)
			nextInEdge[inId].prevInEdge[inId] = prevInEdge[inId];

		if (directed) {
			source.outDegree[inId]--;
			target.inDegree[inId]--;
		} else {
			source.undDegree[inId]--;
			target.undDegree[inId]--;
		}

		// Self-edge.
		if (source == target) {
			if (directed) {
				source.selfEdges[inId]--;
			} else {
				source.undDegree[inId]--;
			}
		}

		nextOutEdge[inId] = null; // ?? wasn't here in DynamicGraph
		prevOutEdge[inId] = null;
		nextInEdge[inId] = null;
		prevInEdge[inId] = null;
	}

    boolean isSet(final int inId) {
		return ( inId >= 0 &&
		         inId < includes.length && 
		         includes[inId] );
    }
}
