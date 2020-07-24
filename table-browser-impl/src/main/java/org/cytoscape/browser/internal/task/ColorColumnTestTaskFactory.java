package org.cytoscape.browser.internal.task;

import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ColorColumnTestTaskFactory extends AbstractTableColumnTaskFactory {

	private final CyServiceRegistrar registrar;

	public ColorColumnTestTaskFactory(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyColumn column) {
		return new TaskIterator(new ColorColumnTestTask(column, registrar));
	}

}
