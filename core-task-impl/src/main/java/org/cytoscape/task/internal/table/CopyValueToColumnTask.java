package org.cytoscape.task.internal.table;

import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableCellTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2018 The Cytoscape Consortium
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

final class CopyValueToColumnTask extends AbstractTableCellTask {
	
	private final boolean selectedOnly;
	private final String taskFactoryName;
	private final CyServiceRegistrar serviceRegistrar;
	
	CopyValueToColumnTask(final CyColumn column, final Object primaryKeyValue, final boolean selectedOnly,
			String taskFactoryName, final CyServiceRegistrar serviceRegistrar) {
		super(column, primaryKeyValue);
		this.selectedOnly = selectedOnly;
		this.taskFactoryName = taskFactoryName;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Copying...");

		final CyRow sourceRow = column.getTable().getRow(primaryKeyValue);
		final String columnName = column.getName();
		final Object sourceValue = sourceRow.getRaw(columnName);
		
		serviceRegistrar.getService(UndoSupport.class).postEdit(
			new CopyValueToColumnEdit(column, sourceValue, taskFactoryName));

		final List<CyRow> rows = column.getTable().getAllRows();
		final int total = rows.size() - 1;
		int count = 0;
		
		for (final CyRow row : rows) {
			if (row == sourceRow)
				continue;
			if (selectedOnly && !row.get(CyNetwork.SELECTED, Boolean.class))
				continue;
			
			row.set(columnName, sourceValue);
			
			if ((++count % 1000) == 0)
				taskMonitor.setProgress((100.0 * count) / total);
		}
	}
}