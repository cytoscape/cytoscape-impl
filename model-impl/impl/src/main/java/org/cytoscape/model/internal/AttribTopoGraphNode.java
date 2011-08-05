/*
  File: AttribTopoGraphNode.java

  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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


import java.util.Collection;
import java.util.HashSet;

import org.cytoscape.model.internal.tsort.TopoGraphNode;


/**
 *  Represents a node in a topological graph.
 */
public class AttribTopoGraphNode implements TopoGraphNode {
	private final String nodeName;
	private final Collection<TopoGraphNode> dependents;

	public AttribTopoGraphNode(final String nodeName, final Collection<String> dependents) {
		this.nodeName = nodeName;
		this.dependents = new HashSet<TopoGraphNode>();

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
