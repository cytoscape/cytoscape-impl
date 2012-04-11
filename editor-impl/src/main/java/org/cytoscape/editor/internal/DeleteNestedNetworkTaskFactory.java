package org.cytoscape.editor.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.TaskIterator;

public class DeleteNestedNetworkTaskFactory extends AbstractNodeViewTaskFactory {
	final CyNetworkManager netMgr;
	final SelectedVisualStyleManager svsmMgr;

	public DeleteNestedNetworkTaskFactory(final CyNetworkManager netMgr, final SelectedVisualStyleManager svsmMgr) {
		this.netMgr = netMgr;
		this.svsmMgr = svsmMgr;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new DeleteNestedNetworkTask(nodeView, networkView, netMgr, svsmMgr));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		if (!super.isReady(nodeView, networkView))
			return false;
		
		// Check if there is a network pointer and if it is registered.
		// Nodes with unregistered network pointers should be ignored because they are probably being used as something
		// else other than regular nested networks (e.g. groups).
		final CyNode node  = nodeView.getModel();
		final CyNetwork netPointer = node.getNetworkPointer();
		
		return netPointer != null && netMgr.networkExists(netPointer.getSUID());
	}
}
