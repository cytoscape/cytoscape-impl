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

public class SIFInterpreterNodeEdit extends AbstractCyEdit {
    private CyNetwork network;
    private List<CyNode> nodes;

	public SIFInterpreterNodeEdit(CyNetwork network, final List<CyNode> nodes) { 
        super("SIF Adding Nodes");
        this.network = network;
        this.nodes = nodes;

	}


    public void redo() {
		CyNode node1 = network.addNode(); 
        network.getRow(node1).set("name", SIFInterpreterTask.nodeNames.remove(0));
	}

	public void undo() {
		network.removeNodes(nodes);
	}
}
