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
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.TableTunable;

public class ListColumnsTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	final CyNetworkTableManager networkTableMgr;
	List<CyColumn> columns;

	@ContainsTunables
	public TableTunable table = null;

	public ListColumnsTask(CyApplicationManager appMgr, CyTableManager tableMgr, 
	                       CyNetworkTableManager networkTableMgr) {
		super(tableMgr);
		this.appMgr = appMgr;
		this.networkTableMgr = networkTableMgr;
		table = new TableTunable(tableMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		CyTable requestedTable = table.getTable();
		if (requestedTable == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Unable to find table '"+table.getTableString()+"'");
			return;
		}

		columns = new ArrayList<CyColumn> (requestedTable.getColumns());
		taskMonitor.showMessage(TaskMonitor.Level.INFO, 
		                        "Columns for table "+getTableDescription(requestedTable)+":");
		for (CyColumn column: columns)
			taskMonitor.showMessage(TaskMonitor.Level.INFO, "         "+column.toString());
	}

	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class) && columns != null) {
			return DataUtils.convertData(columns);
		}
		return columns;
	}

	private String getTableDescription(CyTable table) {
		String result = "["+table.getSUID()+"]";
		int rows = table.getRowCount();
		int cols = table.getColumns().size();
		if (table.isPublic())
			result += " is a public table with ";
		else
			result += " is a private table with ";
		result += ""+rows+" rows and "+cols+" columns with title:\n";
		result += "        "+table.getTitle();
		return result;
	}
}
