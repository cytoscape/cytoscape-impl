package org.cytoscape.filter.internal.tasks;

import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.view.AbstractPanelController;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExportNamedTransformersTaskFactory extends AbstractTaskFactory {

	private FilterIO filterIo;

	private AbstractPanelController<?,?> panel;

	public ExportNamedTransformersTaskFactory(FilterIO filterIo, AbstractPanelController<?,?> panel) {
		this.filterIo = filterIo;
		this.panel = panel;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ExportNamedTransformersTask(filterIo, panel));
	}

}
