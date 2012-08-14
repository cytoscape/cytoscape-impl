package org.cytoscape.editor.internal;

import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class CutTaskFactory extends AbstractNetworkViewTaskFactory {
	final CyNetworkManager netMgr;
	final ClipboardManagerImpl clipMgr;
	final UndoSupport undoSupport;

	public CutTaskFactory(final ClipboardManagerImpl clipboardMgr, final UndoSupport undoSupport, 
	                      final CyNetworkManager netMgr) {
		this.netMgr = netMgr;
		this.clipMgr = clipboardMgr;
		this.undoSupport = undoSupport;
	}

	@Override
	public boolean isReady(CyNetworkView networkView) {
		if (!super.isReady(networkView))
			return false;

		// Make sure we've got something selected
		List<CyNode> selNodes = CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED, true);
		if (selNodes != null && selNodes.size() > 0) return true;

		return false;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new CutTask(networkView, clipMgr, undoSupport));
	}
}
