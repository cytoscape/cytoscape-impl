package org.cytoscape.task.internal.table;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.TableTunable;

public class GetValueTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	Object resultValue = null;

	@ContainsTunables
	public TableTunable tableTunable = null;

	@Tunable(description="Key value for row", context="nogui")
	public String keyValue = null;

	@Tunable(description="Name of column", context="nogui")
	public String column = null;

	public GetValueTask(CyApplicationManager appMgr, CyTableManager tableMgr) {
		super(tableMgr);
		this.appMgr = appMgr;
		tableTunable = new TableTunable(tableMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		CyTable table = tableTunable.getTable();
		if (table == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Unable to find table '"+tableTunable.getTableString()+"'");
			return;
		}

		if (keyValue == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Key of desired row must be specified");
			return;
		}

		if (column == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Column name must be specified");
			return;
		}

		// Get the primary key column
		CyColumn primaryKColumn = table.getPrimaryKey();
		Class keyType = primaryKColumn.getType();
		Object key;
		try {
			key = DataUtils.convertString(keyValue, keyType);
		} catch (NumberFormatException nfe) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Unable to convert "+keyValue+" to a "+keyType.getName()+": "+nfe.getMessage());
			return;
		}
		if (key == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Unable to convert "+keyValue+" to a "+keyType.getName());
			return;
		}

		CyColumn targetColumn = table.getColumn(column);
		if (targetColumn == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Can't find a '"+column+"' column in this table");
			return;
		}

		CyRow row = table.getRow(key);
		if (row == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Can't find a '"+keyValue+"' row in this table");
			return;
		}
		
		Class columnType = targetColumn.getType();
		if (targetColumn.getType().equals(List.class)) {
			Class elementType = targetColumn.getListElementType();
			List<?> valueList = row.getList(column, elementType);
			if (valueList == null) 
				taskMonitor.showMessage(TaskMonitor.Level.INFO, 
					                      " "+column+"=<null>");
			else
				taskMonitor.showMessage(TaskMonitor.Level.INFO, 
					                      " "+column+"="+DataUtils.convertData(valueList));
			resultValue = valueList;
		} else {
			Object value = row.get(column, columnType);
			if (value == null)
				taskMonitor.showMessage(TaskMonitor.Level.INFO, 
					                      " "+column+"=<null>");
			else
				taskMonitor.showMessage(TaskMonitor.Level.INFO, 
					                      " "+column+"="+DataUtils.convertData(value));
			resultValue = value;
		}

	}

	public Object getResults(Class requestedType) {
		if (resultValue == null) return null;
		if (requestedType.equals(String.class)) {
			return DataUtils.convertData(resultValue);
		}
		return resultValue;
	}

}
