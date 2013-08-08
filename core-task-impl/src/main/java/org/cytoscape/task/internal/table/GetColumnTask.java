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
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.TableTunable;

public class GetColumnTask extends AbstractTableDataTask {
	final CyApplicationManager appMgr;
	CyColumn returnValue;

	@ContainsTunables
	public TableTunable tableTunable = null;

	@Tunable(description="Name of column", context="nogui")
	public String column = null;

	public GetColumnTask(CyApplicationManager appMgr, CyTableManager tableMgr) {
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

		if (column == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Column name must be specified");
			return;
		}

		returnValue = table.getColumn(column);
		if (returnValue == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Column '"+column+"' doesn't exist in table: "+table.toString());
			return;
		}

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Retrieved column: "+returnValue.toString());
	}

	public Object getResults(Class requestedType) {
		if (returnValue == null) return null;
		if (requestedType.equals(String.class)) {
			return returnValue.toString();
		}
		return returnValue;
	}

}
