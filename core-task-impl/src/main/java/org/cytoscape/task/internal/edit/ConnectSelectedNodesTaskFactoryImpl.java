package org.cytoscape.task.internal.edit;


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.edit.ConnectSelectedNodesTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public class ConnectSelectedNodesTaskFactoryImpl extends AbstractTaskFactory implements ConnectSelectedNodesTaskFactory{
	private final UndoSupport undoSupport;
	private final CyApplicationManager appManager;
	private final CyEventHelper eventHelper;

	public ConnectSelectedNodesTaskFactoryImpl(final UndoSupport undoSupport,
	                                       final CyApplicationManager appManager,
	                                       final CyEventHelper eventHelper)
	{
		this.undoSupport = undoSupport;
		this.appManager  = appManager;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new ConnectSelectedNodesTask(undoSupport, appManager.getCurrentNetwork(),
			                             eventHelper));
	}

}
