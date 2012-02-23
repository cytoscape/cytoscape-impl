package org.cytoscape.task.internal.hide;


import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;


import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo hiding of nodes and edges. */
final class HideEdit extends AbstractCyEdit {
	private final CyEventHelper eventHelper;
	private final CyNetwork network;
	private final CyNetworkView view;
	private List<NodeAndHideState> nodeStates;
	private List<EdgeAndHideState> edgeStates;

	HideEdit(final CyEventHelper eventHelper, final String description,
	         final CyNetwork network, final CyNetworkView view)
	{
		super(description);

		this.eventHelper = eventHelper;
		this.network     = network;
		this.view        = view;

		saveHideState();
	}

	public void redo() {
		;
		saveAndRestoreState();
	}

	public void undo() {
		;
		saveAndRestoreState();
	}

	private void saveAndRestoreState() {
		final List<NodeAndHideState> oldNodeStates = nodeStates;
		final List<EdgeAndHideState> oldEdgeStates = edgeStates;

		saveHideState();

		for (final NodeAndHideState nodeAndState : oldNodeStates)
			view.getNodeView(nodeAndState.getNode()).setVisualProperty(
				NODE_VISIBLE, nodeAndState.isHidden());

		for (final EdgeAndHideState edgeAndState : oldEdgeStates)
			view.getEdgeView(edgeAndState.getEdge()).setVisualProperty(
				EDGE_VISIBLE, edgeAndState.isHidden());

		eventHelper.flushPayloadEvents();
		view.updateView();
	}

	private void saveHideState() {
		final List<CyNode> nodes = network.getNodeList();
		nodeStates = new ArrayList<NodeAndHideState>(nodes.size());
		for (final CyNode node : nodes)
			nodeStates.add(new NodeAndHideState(node, view.getNodeView(node).getVisualProperty(NODE_VISIBLE)));

		final List<CyEdge> edges = network.getEdgeList();
		edgeStates = new ArrayList<EdgeAndHideState>(edges.size());
		for (final CyEdge edge : edges)
			edgeStates.add(new EdgeAndHideState(edge, view.getEdgeView(edge).getVisualProperty(EDGE_VISIBLE)));
	}
}


final class NodeAndHideState {
	private final CyNode node;
	private final Boolean hidden;

	NodeAndHideState(final CyNode node, final Boolean hidden) {
		this.node   = node;
		this.hidden = hidden;
	}

	CyNode getNode() { return node; }
	Boolean isHidden() { return hidden; }
}


final class EdgeAndHideState {
	private final CyEdge edge;
	private final Boolean hidden;

	EdgeAndHideState(final CyEdge edge, final Boolean hidden) {
		this.edge   = edge;
		this.hidden = hidden;
	}

	CyEdge getEdge() { return edge; }
	Boolean isHidden() { return hidden; }
}
