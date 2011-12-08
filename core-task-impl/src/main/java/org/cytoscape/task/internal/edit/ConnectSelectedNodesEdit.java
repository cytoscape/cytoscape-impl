package org.cytoscape.task.internal.edit;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;


/** An undoable edit that will undo and redo the connecting of selected nodes. */
final class ConnectSelectedNodesEdit extends AbstractCyEdit {
	private final CyNetwork network;
	private Collection<CyEdge> edges;

	ConnectSelectedNodesEdit(final CyNetwork network, final Collection<CyEdge> edges) {
		super("Connect Selected Nodes");

		this.network = network;
		this.edges   = edges;
	}

	public void redo() {
		;

		final List<CyEdge> newEdges = new ArrayList<CyEdge>(edges.size());
		for (final CyEdge edge : edges) {
			final CyNode source = edge.getSource();
			final CyNode target = edge.getTarget();
			final CyEdge newEdge = network.addEdge(source, target, /* isDirected = */ false);
			network.getCyRow(newEdge).set(CyTableEntry.NAME,
			                       network.getCyRow(source).get(CyTableEntry.NAME, String.class)
			                       + " (" + ConnectSelectedNodesTask.INTERACTION + ") "
			                       + network.getCyRow(target).get(CyTableEntry.NAME, String.class));
			network.getCyRow(newEdge).set(CyEdge.INTERACTION, ConnectSelectedNodesTask.INTERACTION);
			newEdges.add(newEdge);
		}

		edges = newEdges;
	}

	public void undo() {
		;

		network.removeEdges(edges);
	}
}
