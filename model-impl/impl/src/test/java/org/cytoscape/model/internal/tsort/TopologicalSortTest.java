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
import java.util.HashSet;
import java.util.Collection;
import junit.framework.*;


class Node implements TopoGraphNode {
	private final String nodeName;
	private final Collection<TopoGraphNode> dependents;

	Node(final String nodeName) {
		this.nodeName = nodeName;
		this.dependents = new HashSet<>();
	}

	@Override public String toString() { return nodeName; }
	public Collection<TopoGraphNode> getDependents() { return dependents; }
	void addDependent(final TopoGraphNode newDependent) { dependents.add(newDependent); }
}


public class TopologicalSortTest extends TestCase {
	public void testWikipediaExample() { // Based on the example at http://en.wikipedia.org/wiki/Topological_sorting
		final Node node7 = new Node("7");
		final Node node5 = new Node("5");
		final Node node3 = new Node("3");
		final Node node11 = new Node("11");
		final Node node8 = new Node("8");
		final Node node2 = new Node("2");
		final Node node9 = new Node("9");
		final Node node10 = new Node("10");

		node7.addDependent(node11);
		node7.addDependent(node8);
		node5.addDependent(node11);
		node3.addDependent(node8);
		node3.addDependent(node10);
		node11.addDependent(node2);
		node11.addDependent(node9);
		node11.addDependent(node10);
		node8.addDependent(node9);

		final Collection<TopoGraphNode> nodes = new ArrayList<>();
		nodes.add(node7);
		nodes.add(node5);
		nodes.add(node3);
		nodes.add(node11);
		nodes.add(node8);
		nodes.add(node2);
		nodes.add(node9);
		nodes.add(node10);

		final Collection<TopoGraphNode> order = TopologicalSort.sort(nodes);
		assertTrue(isTopologicalOrder(order));
	}

	public void testGraphWithSomeUnconnectedNodes() {
		final Node node7 = new Node("7");
		final Node node5 = new Node("5");
		final Node node3 = new Node("3");
		final Node node11 = new Node("11");
		final Node node8 = new Node("8");
		final Node node2 = new Node("2");
		final Node node9 = new Node("9");
		final Node node10 = new Node("10");
		final Node nodeU1 = new Node("U3");
		final Node nodeU2 = new Node("U2");
		final Node nodeU3 = new Node("U1");

		node7.addDependent(node11);
		node7.addDependent(node8);
		node5.addDependent(node11);
		node3.addDependent(node8);
		node3.addDependent(node10);
		node11.addDependent(node2);
		node11.addDependent(node9);
		node11.addDependent(node10);
		node8.addDependent(node9);

		final Collection<TopoGraphNode> nodes = new ArrayList<>();
		nodes.add(node7);
		nodes.add(node5);
		nodes.add(node3);
		nodes.add(node11);
		nodes.add(node8);
		nodes.add(node2);
		nodes.add(node9);
		nodes.add(node10);

		final Collection<TopoGraphNode> order = TopologicalSort.sort(nodes);
		assertTrue(isTopologicalOrder(order));
	}

	public void testIndirectCycle() {
		final Node nodeA = new Node("A");
		final Node nodeB = new Node("B");
		final Node nodeC = new Node("C");

		nodeA.addDependent(nodeB);
		nodeB.addDependent(nodeC);
		nodeC.addDependent(nodeA);

		final Collection<TopoGraphNode> nodes = new ArrayList<>();
		nodes.add(nodeA);
		nodes.add(nodeB);
		nodes.add(nodeC);

		boolean failed;
		try {
			TopologicalSort.sort(nodes);
			failed = false;
		} catch (final IllegalStateException e) {
			failed = true;
		}

		assertTrue(failed);
	}

	public void testSelfLoopCycle() {
		final Node nodeA = new Node("A");
		final Node nodeB = new Node("B");
		final Node nodeC = new Node("C");

		nodeB.addDependent(nodeB);

		final Collection<TopoGraphNode> nodes = new ArrayList<>();
		nodes.add(nodeA);
		nodes.add(nodeB);
		nodes.add(nodeC);

		boolean failed;
		try {
			TopologicalSort.sort(nodes);
			failed = false;
		} catch (final IllegalStateException e) {
			failed = true;
		}

		assertTrue(failed);
	}

	private boolean isTopologicalOrder(final Collection<TopoGraphNode> topoOrderCandidate) {
		final TopoGraphNode[] orderAsArray = topoOrderCandidate.toArray(new  TopoGraphNode[topoOrderCandidate.size()]);

		for (int i = 0; i < orderAsArray.length; ++i) {
			final Collection<TopoGraphNode> dependents = orderAsArray[i].getDependents();
			for (int k = i + 1; k < orderAsArray.length; ++k) {
				if (dependents.contains(orderAsArray[k]))
					return false;
			}
		}

		return true;
	}
}
