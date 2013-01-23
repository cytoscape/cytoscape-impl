package org.cytoscape.model.internal.tsort;

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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;


/**
 *  Implements topological sorting of nodes in a graph.
 *  See for example http://en.wikipedia.org/wiki/Topological_sorting (the Tarjan algorithm)
 */
public class TopologicalSort {
	/**
	 *  @param nodes the list of all nodes
	 *  @param edges the edges that connect the nodes that need to be sorted.
	 *  @return the topological order
	 *  @throws IllegalStateException if a cycle has been detected
	 *  N.B. it might be a good idea to make sure that whatever the concrete type of the nodes in
	 *  "nodes" are has a toString() method that returns the name of a node since this method
	 *  will be used if a cycle has been detected to report one of the nodes in the cycle.
	 */
	public static List<TopoGraphNode> sort(final Collection<TopoGraphNode> nodes)
		throws IllegalStateException
	{
		final List<TopoGraphNode> order = new ArrayList<TopoGraphNode>();
		final Set<TopoGraphNode> visited = new HashSet<TopoGraphNode>();

		final Set<TopoGraphNode> alreadySeen = new HashSet<TopoGraphNode>();
		for (final TopoGraphNode n : nodes) {
			alreadySeen.clear();
			visit(n, alreadySeen, visited, order);
		}

		return order;
	}

	private static void visit(final TopoGraphNode n, final Set<TopoGraphNode> alreadySeen,
	                          final Set<TopoGraphNode> visited, final List<TopoGraphNode> order)
	{
		if (alreadySeen.contains(n))
			throw new IllegalStateException("cycle containing " + n + " found.");
		alreadySeen.add(n);

		if (!visited.contains(n)) {
			visited.add(n);
			for (final TopoGraphNode m : n.getDependents())
				visit(m, alreadySeen, visited, order);
			order.add(n);
		}

		alreadySeen.remove(n);
	}
}
