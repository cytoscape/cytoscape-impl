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
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.util.json.CyJSONUtil;

public class ListColumnsTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	final CyNetworkTableManager networkTableMgr;
	private final CyServiceRegistrar serviceRegistrar;
	List<CyColumn> columns;

	@ContainsTunables
	public TableTunable table = null;

	public ListColumnsTask(CyApplicationManager appMgr, CyTableManager tableMgr, 
	                       CyNetworkTableManager networkTableMgr, CyServiceRegistrar reg) {
		super(tableMgr);
		this.appMgr = appMgr;
		serviceRegistrar =reg;
		this.networkTableMgr = networkTableMgr;
		table = new TableTunable(tableMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		CyTable requestedTable = table.getTable();
		if (requestedTable == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Unable to find table '"+table.getTableString()+"'");
			return;
		}

		columns = new ArrayList<CyColumn> (requestedTable.getColumns());
		taskMonitor.showMessage(TaskMonitor.Level.INFO,   "Columns for table "+getTableDescription(requestedTable)+":");
		for (CyColumn column: columns)
			taskMonitor.showMessage(TaskMonitor.Level.INFO, "         "+column.toString());
	}

	public List<Class<?>> getResultClasses() {	return Arrays.asList(String.class, JSONResult.class);	}
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class) && columns != null)  return DataUtils.convertData(columns);
		
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (columns == null) 		return "{}";
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				return cyJSONUtil.cyColumnsToJson(columns);
			};
		return res;
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
