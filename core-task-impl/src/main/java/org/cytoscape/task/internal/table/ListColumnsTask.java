package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class ListColumnsTask extends AbstractTableDataTask implements ObservableTask {
	
	List<CyColumn> columns;

	@ContainsTunables
	public TableTunable table;

	public ListColumnsTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		table = new TableTunable(serviceRegistrar);
	}

	@Override
	public void run(final TaskMonitor tm) {
		CyTable requestedTable = table.getTable();
		if (requestedTable == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "Unable to find table '"+table.getTableString()+"'");
			return;
		}

		columns = new ArrayList<CyColumn> (requestedTable.getColumns());
		tm.showMessage(TaskMonitor.Level.INFO,   "Columns for table "+getTableDescription(requestedTable)+":");
		for (CyColumn column: columns)
			tm.showMessage(TaskMonitor.Level.INFO, "         "+column.toString());
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, JSONResult.class);	
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class) && columns != null)  
			return DataUtils.convertData(columns);

		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (columns == null)
					return "{}";
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				return cyJSONUtil.cyColumnsToJson(columns);
			};
			return res;
		}

		if (requestedType.equals(List.class)) {
			return columns;
		}
		return null;
	}

	private String getTableDescription(CyTable table) {
		String result = "["+table.getSUID()+"]";
		int rows = table.getRowCount();
		int cols = table.getColumns().size();
		result += " is a " + (table.isPublic() ? "public" : "private") + " table with ";
		result += ""+rows+" rows and "+cols+" columns with title:\n";
		result += "        "+table.getTitle();
		return result;
	}
}
