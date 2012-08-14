package org.cytoscape.editor.internal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CutTask extends AbstractTask {
	CyNetwork net;
	CyNetworkView view;
	ClipboardManagerImpl mgr;
	final UndoSupport undoSupport;
	List<CyNode> selNodes;
	List<CyEdge> selEdges;
	
	public CutTask(final CyNetworkView netView, final ClipboardManagerImpl clipMgr, 
	               final UndoSupport undoSupport) {
		this.view = netView;
		this.undoSupport = undoSupport;
		// Get all of the selected nodes and edges
		selNodes = CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true);
		selEdges = CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true);

		// Save them in our list
		mgr = clipMgr;
	}

	public CutTask(final CyNetworkView netView, final View<?extends CyIdentifiable> objView, 
	               final ClipboardManagerImpl clipMgr, final UndoSupport undoSupport) {

		// Get all of the selected nodes and edges first
		this(netView, clipMgr, undoSupport);

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
		mgr.cut(view, selNodes, selEdges);
		undoSupport.postEdit(new CutEdit(mgr, view));
	}
}
