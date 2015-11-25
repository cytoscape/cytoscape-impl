package org.cytoscape.task.internal.edit;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.undo.AbstractCyEdit;

/**
 * An undoable edit that will undo and redo the connecting of selected nodes.
 * 
 * */
final class ConnectSelectedNodesEdit extends AbstractCyEdit {

	private final CyNetwork network;
	private Collection<CyEdge> edges;

	ConnectSelectedNodesEdit(final CyNetwork network, final Collection<CyEdge> edges) {
		super("Connect Selected Nodes");

		this.network = network;
		this.edges = edges;
	}

	@Override
	public void redo() {

		final List<CyEdge> newEdges = new ArrayList<CyEdge>(edges.size());
		for (final CyEdge edge : edges) {
			final CyNode source = edge.getSource();
			final CyNode target = edge.getTarget();
			final CyEdge newEdge = network.addEdge(source, target, false);
			network.getRow(newEdge).set(
					CyNetwork.NAME,
					network.getRow(source).get(CyNetwork.NAME, String.class) + " ("
							+ ConnectSelectedNodesTask.DEFAULT_INTERACTION + ") "
							+ network.getRow(target).get(CyNetwork.NAME, String.class));
			network.getRow(newEdge).set(CyEdge.INTERACTION, ConnectSelectedNodesTask.DEFAULT_INTERACTION);
			newEdges.add(newEdge);
		}
		edges = newEdges;
	}

	@Override
	public void undo() {
		network.removeEdges(edges);
	}
}
