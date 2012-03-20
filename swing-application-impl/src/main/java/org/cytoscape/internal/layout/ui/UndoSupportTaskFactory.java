package org.cytoscape.internal.layout.ui;


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.layout.AbstractLayoutAlgorithmContext;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class UndoSupportTaskFactory<T extends AbstractLayoutAlgorithmContext> extends AbstractLayoutAlgorithm<T> {
	
	private AbstractLayoutAlgorithm<T> delegate;
	private UndoSupport undo;
	private CyEventHelper eventHelper;
	private String name;

	public UndoSupportTaskFactory(AbstractLayoutAlgorithm<T> delegate, UndoSupport undo, CyEventHelper eventHelper) {
		super(delegate.getName(), delegate.toString(), delegate.supportsSelectedOnly());
		this.name = delegate.toString();
		this.undo = undo;
		this.delegate = delegate;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator createTaskIterator(T tunableContext) {
		CyNetworkView networkView = tunableContext.getNetworkView();
		TaskIterator source = delegate.createTaskIterator(tunableContext);
		Task[] tasks = new Task[source.getNumTasks() + 1];
		tasks[0] = new UndoSupportTask(name, undo, eventHelper, networkView);
		for (int i = 1; i < tasks.length; i++) {
			tasks[i] = source.next();
		}
		return new TaskIterator(tasks.length, tasks);
	}
	
	@Override
	public boolean isReady(T tunableContext) {
		return delegate.isReady(tunableContext);
	}
	
	@Override
	public T createLayoutContext() {
		return delegate.createLayoutContext();
	}
}
