package org.cytoscape.editor.internal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.AbstractCyEdit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SIFInterpreterEdgeEdit extends AbstractCyEdit {
    private CyNetwork network;
    private List<CyEdge> edges;


	public SIFInterpreterEdgeEdit(CyNetwork network, final List<CyEdge> edges) { 
        super("SIF Adding Edges");
        this.network = network;
        this.edges = edges;

	}


    public void redo() {

		final List<CyEdge> newEdges = new ArrayList<CyEdge>(edges.size());
		for (final CyEdge edge : edges) {
			final CyNode source = edge.getSource();
			final CyNode target = edge.getTarget();
			final CyEdge newEdge = network.addEdge(source, target, false);
            network.getRow(newEdge).set(
                CyNetwork.NAME,
                network.getRow(source).get(CyNetwork.NAME, String.class) + " ("
                        + SIFInterpreterTask.interactionType + ") "
                        + network.getRow(target).get(CyNetwork.NAME, String.class));
            network.getRow(newEdge).set(CyEdge.INTERACTION, SIFInterpreterTask.interactionType);
			newEdges.add(newEdge);
		}
		edges = newEdges;
	}
    

	public void undo() {
		network.removeEdges(edges);
	}
}
