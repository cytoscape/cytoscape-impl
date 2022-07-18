package org.cytoscape.view.table.internal.equation;

import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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

		return row >= 0 
			&& column >= 0 
			&& browserTable.isCellEditable(row, column) 
			&& browserTable.getCellEditor() == null;
	}
}
