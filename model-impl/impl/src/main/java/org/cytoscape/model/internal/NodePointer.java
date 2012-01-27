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


/**
 * Element of the edge linked list used in {@link ArrayGraph}.
 * You should only touch this if you know what you're doing!
 */
final class NodePointer {
	final CyNode cyNode;
	final int index;

	NodePointer nextNode; 
	NodePointer prevNode;
	EdgePointer firstOutEdge; 
	EdgePointer firstInEdge;

	// The number of directed edges whose source is this node.
	int outDegree; 

	// The number of directed edges whose target is this node.
	int inDegree; 

	// The number of undirected edges which touch this node.
	int undDegree; 

	// The number of directed self-edges on this node.
	int selfEdges; 

	NodePointer(final CyNode cyn) {
		cyNode = cyn;
		index = cyn.getIndex();

		outDegree = 0;
		inDegree = 0;
		undDegree = 0;
		selfEdges = 0;

		firstOutEdge = null;
		firstInEdge = null;
	}

	NodePointer insert(final NodePointer next) {
		nextNode = next;
		if (next != null)
			next.prevNode = this;
		// return instead of:
		// next = this;
		return this;
	}

	NodePointer remove(final NodePointer first) {
		NodePointer ret = first;
		if (prevNode != null)
			prevNode.nextNode = nextNode;
		else
			ret = nextNode;

		if (nextNode != null)
			nextNode.prevNode = prevNode;

		nextNode = null; 
		prevNode = null;
		firstOutEdge = null;
		firstInEdge = null;

		return ret;
	}
}
