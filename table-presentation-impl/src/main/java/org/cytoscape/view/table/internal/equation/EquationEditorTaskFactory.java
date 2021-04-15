package org.cytoscape.view.table.internal.equation;

import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskIterator;

public class EquationEditorTaskFactory implements TableTaskFactory {

	private final CyServiceRegistrar registrar;

	public EquationEditorTaskFactory(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}

	@Override
	public TaskIterator createTaskIterator(CyTable table) {
		return new TaskIterator(new EquationEditorTask(registrar, table));
	}

	@Override
	public boolean isReady(CyTable table) {
		var browserTable = EquationEditorTask.getBrowserTable(table, registrar);
		
		if (browserTable == null)
			return false;
		
		int row = browserTable.getSelectedRow();
		int column = browserTable.getSelectedColumn();

		return row >= 0 && column >= 0 && browserTable.isCellEditable(row, column);
	}
}
