package org.cytoscape.editor.internal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;

import java.util.ArrayList;
import java.util.List;

public class CopyTask extends AbstractNetworkViewTask {
	CyNetwork net;
	ClipboardManagerImpl mgr;
	List<CyNode> selNodes;
	List<CyEdge> selEdges;
	
	public CopyTask(final CyNetworkView netView, final ClipboardManagerImpl clipMgr) {
		super(netView);
		// Get all of the selected nodes and edges
		selNodes = CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true);
		selEdges = CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true);

		// Save them in our list
		mgr = clipMgr;
	}

	public void run(TaskMonitor tm) throws Exception {
		mgr.copy(view, selNodes, selEdges);
	}
}
