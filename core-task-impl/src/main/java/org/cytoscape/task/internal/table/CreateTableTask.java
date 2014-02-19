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
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.cytoscape.task.internal.utils.DataUtils;

public class CreateTableTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	final CyTableFactory tableFactory;
	CyTable table = null;

	@Tunable(description="Table name (title)", context="nogui")
	public String title = null;

	@Tunable(description="Key column name", context="nogui")
	public String keyColumn = null;

	@Tunable (description="Type of key column", context="nogui")
	public ListSingleSelection<String> keyColumnType =  
		new ListSingleSelection<String>("integer", "long", "double", "string", "boolean");

	public CreateTableTask(CyApplicationManager appMgr, CyTableFactory factory, CyTableManager tableMgr) {
		super(tableMgr);
		this.appMgr = appMgr;
		this.tableFactory = factory;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (keyColumn == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Name of key column must be specified");
			return;
		}

		Class keyType = DataUtils.getType(keyColumnType.getSelectedValue());
		if (keyType == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Key column type must be specified");
			return;
		}

		if (title == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Table title must be specified");
			return;
		}

		table = tableFactory.createTable(title, keyColumn, keyType, true, true);
		if (table != null) {
			taskMonitor.showMessage(TaskMonitor.Level.INFO, 
				                      "Created table '"+table.toString()+"' (suid:"+table.getSUID()+")");
			cyTableManager.addTable(table);
		} else {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
				                      "Unable to create table'"+title+"'");
		}

	}

	public Object getResults(Class requestedType) {
		if (table == null) return null;
		if (requestedType.equals(String.class)) {
			return table.toString();
		}
		return table;
	}

}
