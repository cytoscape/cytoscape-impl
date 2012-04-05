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


/**
 * Element of the edge linked list used in {@link SimpleNetwork}.
 * You should only touch this if you know what you're doing.
 */
final class EdgePointer {
	final CyEdge cyEdge;
	final long index;
	final boolean directed;
	final NodePointer source;
	final NodePointer target;

	EdgePointer nextOutEdge; 
	EdgePointer prevOutEdge;
	EdgePointer nextInEdge;
	EdgePointer prevInEdge;

	EdgePointer(final NodePointer s, final NodePointer t, final boolean dir, final CyEdge edge) {
		source = s;
		target = t;
		directed = dir;
		cyEdge = edge;
		index = edge.getSUID();

		nextOutEdge = null;
		prevOutEdge = null;

		nextInEdge = null;
		prevInEdge = null;

		insertSelf();
	}

	private void insertSelf() {

		nextOutEdge = source.firstOutEdge;

		if (source.firstOutEdge != null)
			source.firstOutEdge.prevOutEdge = this;

		source.firstOutEdge = this;

		nextInEdge = target.firstInEdge;

		if (target.firstInEdge != null)
			target.firstInEdge.prevInEdge = this;

		target.firstInEdge = this;

		if (directed) {
			source.outDegree++;
			target.inDegree++;
		} else {
			source.undDegree++;
			target.undDegree++;
		}

		// Self-edge
		if (source == target) {
			if (directed) {
				source.selfEdges++;
			} else {
				source.undDegree--;
			}
		}
	}

	void remove() {

		if (prevOutEdge != null)
			prevOutEdge.nextOutEdge = nextOutEdge;
		else
			source.firstOutEdge = nextOutEdge;

		if (nextOutEdge != null)
			nextOutEdge.prevOutEdge = prevOutEdge;

		if (prevInEdge != null)
			prevInEdge.nextInEdge = nextInEdge;
		else
			target.firstInEdge = nextInEdge;

		if (nextInEdge != null)
			nextInEdge.prevInEdge = prevInEdge;

		if (directed) {
			source.outDegree--;
			target.inDegree--;
		} else {
			source.undDegree--;
			target.undDegree--;
		}

		// Self-edge.
		if (source == target) {
			if (directed) {
				source.selfEdges--;
			} else {
				source.undDegree--;
			}
		}

		nextOutEdge = null; // ?? wasn't here in DynamicGraph
		prevOutEdge = null;
		nextInEdge = null;
		prevInEdge = null;
	}

}
