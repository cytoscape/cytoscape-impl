package org.cytoscape.task.internal.table;

import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.internal.utils.ColumnTypeTunable;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CreateColumnTask extends AbstractTableDataTask {
	
	@ContainsTunables
	public TableTunable tableTunable;

	@Tunable(description = "Name of column", context = "nogui")
	public String column;

	@ContainsTunables
	public ColumnTypeTunable columnType;

	public CreateColumnTask(final CyTableManager tableMgr) {
		super(tableMgr);
		tableTunable = new TableTunable(tableMgr);
		columnType = new ColumnTypeTunable();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		CyTable table = tableTunable.getTable();
		
		if (table == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Unable to find table '"+tableTunable.getTableString()+"'");
			return;
		}

		if (column == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Column name must be specified");
			return;
		}
		
		column = column.trim();
		
		if (column.isEmpty()) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Column name must not be blank");
			return;
		}

		CyColumn c = table.getColumn(column);
		
		if (c != null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Column '"+column+"' already exists in table: "+table.toString());
			return;
		}

		String baseTypeName = columnType.getColumnType();
		
		if (baseTypeName == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Column type must be specified.");
			return;
		}

		Class<?> baseType = DataUtils.getType(baseTypeName);
		
		if (baseType.equals(List.class)) {
			String listTypeName = columnType.getListElementType();
			
			if (listTypeName == null) {
				taskMonitor.showMessage(TaskMonitor.Level.ERROR, "List element type must be specified for list columns.");
				return;
			}
			
			Class<?> listType = DataUtils.getType(listTypeName);
			table.createListColumn(column, listType, false);
			taskMonitor.showMessage(TaskMonitor.Level.INFO, "Created list column: "+column);
		} else {
			table.createColumn(column, baseType, false);
			taskMonitor.showMessage(TaskMonitor.Level.INFO, "Created column: "+column);
		}
	}
}
