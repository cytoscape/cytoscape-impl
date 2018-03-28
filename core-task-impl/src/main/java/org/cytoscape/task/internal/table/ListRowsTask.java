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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.RowTunable;
import org.cytoscape.util.json.CyJSONUtil;

public class ListRowsTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	private final CyServiceRegistrar serviceRegistrar;
	List<CyRow> rowList = null;
	CyTable table = null;

	@ContainsTunables
	public RowTunable rowTunable = null;

	public ListRowsTask(CyApplicationManager appMgr, CyTableManager tableMgr, CyServiceRegistrar reg) {
		super(tableMgr);
		this.appMgr = appMgr;
		serviceRegistrar =reg;
		rowTunable = new RowTunable(tableMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		table = rowTunable.getTable();
		if (table == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Unable to find table '"+rowTunable.getTableString()+"'");
			return;
		}

		rowList = rowTunable.getRowList();
		if (rowList == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No rows returned");
			return;
		}

		String primaryKey = table.getPrimaryKey().getName();
		CyColumn nameColumn = table.getColumn(CyNetwork.NAME);
		String nameKey = null;
		if (nameColumn != null) nameKey = nameColumn.getName();

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Retreived "+rowList.size()+" rows:");
		for (CyRow row: rowList) {
			String message = "     Key: "+row.getRaw(primaryKey).toString();
			if (nameKey != null)
				message += " Name: "+row.get(nameKey, String.class);
			taskMonitor.showMessage(TaskMonitor.Level.INFO, message);
		}
	}

	@Override
	public List<Class<?>> getResultClasses() {	
		return Arrays.asList(List.class, String.class, JSONResult.class);	
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class))
			return DataUtils.convertData(rowList);

		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				String out = rowListAsJson();
				return out;
			};
			return res;
		}

		return rowList;
	}

	String rowListAsJson()
	{
		if (rowList == null || rowList.size() == 0) return "{}";
		String primaryKey = table.getPrimaryKey().getName();
		StringJoiner rows = new StringJoiner(",", "[","]");
		for (CyRow row : rowList)
			rows.add("\""+row.getRaw(primaryKey).toString()+"\"");

		return "{\"table\":"+table.getSUID()+", \"rows\": "+rows.toString()+"}";
	}
}
