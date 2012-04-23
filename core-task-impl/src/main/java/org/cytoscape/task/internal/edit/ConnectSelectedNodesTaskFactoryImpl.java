package org.cytoscape.task.internal.edit;


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.edit.ConnectSelectedNodesTaskFactory;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.model.CyNetwork;


public class ConnectSelectedNodesTaskFactoryImpl extends AbstractNetworkTaskFactory implements ConnectSelectedNodesTaskFactory{
	private final UndoSupport undoSupport;
	private final CyEventHelper eventHelper;

	public ConnectSelectedNodesTaskFactoryImpl(final UndoSupport undoSupport,
	                                       final CyEventHelper eventHelper)
	{
		this.undoSupport = undoSupport;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator( new ConnectSelectedNodesTask(undoSupport, network, eventHelper));
	}

}
