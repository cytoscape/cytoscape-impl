package org.cytoscape.view.table.internal.equation;

import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskIterator;

public class EquationEditorTaskFactory extends AbstractTableTaskFactory implements TableTaskFactory {

	private final CyServiceRegistrar registrar;
	
	
	public EquationEditorTaskFactory(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}

	@Override
	public TaskIterator createTaskIterator(CyTable table) {
		return new TaskIterator(new EquationEditorTask(registrar, table));
	}


}
