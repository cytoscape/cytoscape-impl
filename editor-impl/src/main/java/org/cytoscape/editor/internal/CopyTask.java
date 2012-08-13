package org.cytoscape.editor.internal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CopyTask extends AbstractTask {
	CyNetworkView netView;
	ClipboardManagerImpl mgr;
	List<CyNode> selNodes;
	List<CyEdge> selEdges;
	
	public CopyTask(final CyNetworkView netView, final ClipboardManagerImpl clipMgr) {
		this.netView = netView;
		// Get all of the selected nodes and edges
		selNodes = CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true);
		selEdges = CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true);

		// Save them in our list
		mgr = clipMgr;
	}

	public CopyTask(final CyNetworkView netView, final View<?extends CyIdentifiable> objView, 
	                final ClipboardManagerImpl clipMgr) {

		// Get all of the selected nodes and edges first
		this(netView, clipMgr);

		// Now, make sure we add our
		if (objView.getModel() instanceof CyNode) {
			CyNode node = ((View<CyNode>)objView).getModel();
			if (!selNodes.contains(node))
				selNodes.add(node);
		} else if (objView.getModel() instanceof CyEdge) {
			CyEdge edge = ((View<CyEdge>)objView).getModel();
			if (!selEdges.contains(edge))
				selEdges.add(edge);
		}
	}

	public void run(TaskMonitor tm) throws Exception {
		mgr.copy(netView, selNodes, selEdges);
	}
}
