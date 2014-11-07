package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.model.internal.tsort.TopoGraphNode;


/**
 *  Represents a node in a topological graph.
 */
public class AttribTopoGraphNode implements TopoGraphNode {
	private final String nodeName;
	private final Collection<TopoGraphNode> dependents;

	public AttribTopoGraphNode(final String nodeName, final Collection<String> dependents) {
		this.nodeName = nodeName;
		this.dependents = Collections.newSetFromMap(new ConcurrentHashMap<TopoGraphNode, Boolean>(16, 0.75f, 2));

		for (final String dependent : dependents)
			this.dependents.add(new AttribTopoGraphNode(dependent));
	}

	private AttribTopoGraphNode(final String nodeName) {
		this.nodeName = nodeName;
		this.dependents = new HashSet<TopoGraphNode>();
	}

	public String getNodeName() { return nodeName; }
	public Collection<TopoGraphNode> getDependents() { return dependents; }
	@Override public int hashCode() { return nodeName.hashCode(); }
	@Override public boolean equals(final Object o) {
		if (!(o instanceof AttribTopoGraphNode))
			return false;

		final AttribTopoGraphNode other = (AttribTopoGraphNode)o;
		return nodeName.equals(other.nodeName);
	}
}
