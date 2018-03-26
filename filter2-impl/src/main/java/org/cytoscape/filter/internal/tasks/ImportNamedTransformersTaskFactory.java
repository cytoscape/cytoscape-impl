package org.cytoscape.filter.internal.tasks;

import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.view.AbstractPanel;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ImportNamedTransformersTaskFactory extends AbstractTaskFactory {

	private FilterIO filterIo;

	@SuppressWarnings("rawtypes")
	private AbstractPanel panel;

	@SuppressWarnings("rawtypes")
	public ImportNamedTransformersTaskFactory(FilterIO filterIo, AbstractPanel panel) {
		this.filterIo = filterIo;
		this.panel = panel;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ImportNamedTransformersTask(filterIo, panel));
	}

}
