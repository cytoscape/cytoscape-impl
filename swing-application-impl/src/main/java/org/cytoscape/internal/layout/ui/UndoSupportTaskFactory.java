package org.cytoscape.internal.layout.ui;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class UndoSupportTaskFactory extends AbstractLayoutAlgorithm {
	
	private NetworkViewTaskFactory delegate;
	private UndoSupport undo;
	private CyEventHelper eventHelper;
	private String name;

	public UndoSupportTaskFactory(AbstractLayoutAlgorithm delegate, UndoSupport undo, CyEventHelper eventHelper) {
		super(undo, delegate.getName(), delegate.toString(), delegate.supportsSelectedOnly());
		this.name = delegate.toString();
		this.undo = undo;
		this.delegate = delegate;
		this.eventHelper = eventHelper;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		TaskIterator source = delegate.createTaskIterator();
		Task[] tasks = new Task[source.getNumTasks() + 1];
		tasks[0] = new UndoSupportTask(name, undo, eventHelper, networkView);
		for (int i = 1; i < tasks.length; i++) {
			tasks[i] = source.next();
		}
		return new TaskIterator(tasks.length, tasks);
	}

	@Override
	public void setNetworkView(CyNetworkView netView) {
		super.setNetworkView(netView);
		delegate.setNetworkView(netView);
	}
}
