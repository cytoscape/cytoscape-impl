package org.cytoscape.internal.layout.ui;

import java.util.List;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.ListSingleSelection;

public class UndoSupportTaskFactory extends AbstractLayoutAlgorithm {
	
	private AbstractLayoutAlgorithm delegate;
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
	
	@Override
	public Set<Class<?>> supportsEdgeAttributes() {
		return delegate.supportsEdgeAttributes();
	}
	
	@Override
	public Set<Class<?>> supportsNodeAttributes() {
		return delegate.supportsNodeAttributes();
	}
	
	@Override
	public List<String> getInitialAttributeList() {
		return delegate.getInitialAttributeList();
	}
	
	@Override
	public void setLayoutAttribute(String attributeName) {
		delegate.setLayoutAttribute(attributeName);
	}
	
	@Override
	public void setSelectedOnly(boolean selectedOnly) {
		delegate.setSelectedOnly(selectedOnly);
	}
	
	@Override
	public void setSubmenuOptions(ListSingleSelection<String> opts) {
		delegate.setSubmenuOptions(opts);
	}
}
