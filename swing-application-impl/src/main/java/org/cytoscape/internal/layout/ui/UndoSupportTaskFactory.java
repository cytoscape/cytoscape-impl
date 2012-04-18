package org.cytoscape.internal.layout.ui;


import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.layout.AbstractLayoutContext;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class UndoSupportTaskFactory<T extends AbstractLayoutContext> extends AbstractLayoutAlgorithm<T> {
	
	private AbstractLayoutAlgorithm<T> delegate;
	private UndoSupport undo;
	private CyEventHelper eventHelper;
	private String name;

	public UndoSupportTaskFactory(AbstractLayoutAlgorithm<T> delegate, UndoSupport undo, CyEventHelper eventHelper) {
		super(delegate.getName(), delegate.toString());
		this.name = delegate.toString();
		this.undo = undo;
		this.delegate = delegate;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, T tunableContext, Set<View<CyNode>> nodesToLayOut) {
		TaskIterator source = delegate.createTaskIterator(networkView, tunableContext, nodesToLayOut);
		Task[] tasks = new Task[source.getNumTasks() + 1];
		tasks[0] = new UndoSupportTask(name, undo, eventHelper, networkView);
		for (int i = 1; i < tasks.length; i++) {
			tasks[i] = source.next();
		}
		return new TaskIterator(tasks.length, tasks);
	}
	
	@Override
	public boolean isReady(CyNetworkView networkView, T tunableContext, Set<View<CyNode>> nodesToLayOut) {
		return delegate.isReady(networkView, tunableContext, nodesToLayOut);
	}
	
	@Override
	public T createLayoutContext() {
		return delegate.createLayoutContext();
	}
}
