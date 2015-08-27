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
		if (source == target)
			source.selfEdges++;
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
		if (source == target)
			source.selfEdges--;

		nextOutEdge = null; // ?? wasn't here in DynamicGraph
		prevOutEdge = null;
		nextInEdge = null;
		prevInEdge = null;
	}
}
