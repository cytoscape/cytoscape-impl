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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.util.json.CyJSONUtil;

public class AddRowTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	CyRow row = null;
	private final CyServiceRegistrar serviceRegistrar;

	@ContainsTunables
	public TableTunable tableTunable = null;

	@Tunable(description="Key value for new row", context="nogui",
	         longDescription=StringToModel.VALUE_LONG_DESCRIPTION+"  Note that network, node, and edge tables ``must`` have Long values as keys", 
	         exampleStringValue = StringToModel.VALUE_EXAMPLE)
	public String keyValue = null;

	public AddRowTask(CyApplicationManager appMgr, CyTableManager tableMgr, CyServiceRegistrar reg) {
		super(tableMgr);
		this.appMgr = appMgr;
		serviceRegistrar = reg;
		tableTunable = new TableTunable(tableMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		CyTable table = tableTunable.getTable();
		if (table == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,   "Unable to find table '"+tableTunable.getTableString()+"'");
			return;
		}

		if (keyValue == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Key of new row must be specified");
			return;
		}

		// Get the primary key column
		CyColumn primaryKColumn = table.getPrimaryKey();
		Class keyType = primaryKColumn.getType();
		Object key = null;
		try {
			key = DataUtils.convertString(keyValue, keyType);
		} catch (NumberFormatException nfe) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Unable to convert "+keyValue+" to a "+keyType.getName()+": "+nfe.getMessage());
			return;
		}
		if (key == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Unable to convert "+keyValue+" to a "+keyType.getName());
			return;
		}

		if (table.rowExists(key)) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Row "+keyValue+" already exists");
			return;
		}
			
		row = table.getRow(key);
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Created new row '"+keyValue+"'");
	}

	@Override
	public List<Class<?>> getResultClasses() {	
		return Arrays.asList(CyRow.class, String.class, JSONResult.class);	
	}
	
	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(CyRow.class))			return row;
		if (requestedType.equals(String.class))			return (row == null) ?  null : row.toString();
		if (requestedType.equals(JSONResult.class)) 
		{
			JSONResult res = () -> { 
				if (row == null) 		return "{}";
				return "{\"table\":"+tableTunable.getTable().getSUID()+", \"row\":\""+keyValue+"\"}";
			};
			return res;
		}
		return null;
	}

}
