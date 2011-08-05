package org.cytoscape.task.internal.edit;


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public class ConnectSelectedNodesTaskFactory implements TaskFactory {
	private final UndoSupport undoSupport;
	private final CyApplicationManager appManager;
	private final CyEventHelper eventHelper;

	public ConnectSelectedNodesTaskFactory(final UndoSupport undoSupport,
	                                       final CyApplicationManager appManager,
	                                       final CyEventHelper eventHelper)
	{
		this.undoSupport = undoSupport;
		this.appManager  = appManager;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(
			new ConnectSelectedNodesTask(undoSupport, appManager.getCurrentNetwork(),
			                             eventHelper));
	}

}
