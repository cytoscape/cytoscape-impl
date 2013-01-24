package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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


import org.cytoscape.model.CyNode;


/**
 * Element of the edge linked list used in {@link ArrayGraph}.
 * You should only touch this if you know what you're doing!
 */
final class NodePointer {
	final CyNode cyNode;
	final long index;

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
		index = cyn.getSUID();

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
