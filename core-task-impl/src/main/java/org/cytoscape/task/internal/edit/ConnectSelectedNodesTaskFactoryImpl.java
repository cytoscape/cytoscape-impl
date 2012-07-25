package org.cytoscape.task.internal.edit;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.edit.ConnectSelectedNodesTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class ConnectSelectedNodesTaskFactoryImpl extends AbstractNetworkTaskFactory implements
		ConnectSelectedNodesTaskFactory, NodeViewTaskFactory {
	
	private final UndoSupport undoSupport;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmm;
	private final CyNetworkViewManager netViewMgr;

	public ConnectSelectedNodesTaskFactoryImpl(final UndoSupport undoSupport, final CyEventHelper eventHelper,
			final VisualMappingManager vmm, final CyNetworkViewManager netViewMgr) {
		this.undoSupport = undoSupport;
		this.eventHelper = eventHelper;
		this.vmm = vmm;
		this.netViewMgr = netViewMgr;
	}

	@Override
	public TaskIterator createTaskIterator(final CyNetwork network) {
		return new TaskIterator(new ConnectSelectedNodesTask(undoSupport, network, eventHelper, vmm, netViewMgr));
	}

	/**
	 * This is for registering this to node context menu
	 */
	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new ConnectSelectedNodesTask(undoSupport, networkView.getModel(), eventHelper, vmm, netViewMgr));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return nodeView != null && networkView != null;
	}
}
