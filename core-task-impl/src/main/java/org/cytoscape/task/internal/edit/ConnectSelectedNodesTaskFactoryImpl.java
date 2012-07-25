package org.cytoscape.task.internal.edit;


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.task.edit.ConnectSelectedNodesTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public class ConnectSelectedNodesTaskFactoryImpl extends AbstractNetworkTaskFactory implements ConnectSelectedNodesTaskFactory{
	private final UndoSupport undoSupport;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmm;
	private final CyNetworkViewManager netViewMgr;

	public ConnectSelectedNodesTaskFactoryImpl(final UndoSupport undoSupport,
											   final CyEventHelper eventHelper,
											   final VisualMappingManager vmm,
											   final CyNetworkViewManager netViewMgr) {
		this.undoSupport = undoSupport;
		this.eventHelper = eventHelper;
		this.vmm = vmm;
		this.netViewMgr = netViewMgr;
	}

	@Override
	public TaskIterator createTaskIterator(final CyNetwork network) {
		return new TaskIterator(new ConnectSelectedNodesTask(undoSupport, network, eventHelper, vmm, netViewMgr));
	}
}
