package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

public class CreateTableTask extends AbstractTableDataTask implements ObservableTask {
	
	CyTable table;

	@Tunable(description="Table name (title)", context="nogui", 
			longDescription=StringToModel.TABLE_TITLE_LONG_DESCRIPTION, exampleStringValue = StringToModel.TABLE_TITLE_EXAMPLE)
	public String title = null;

	@Tunable(description="Key column name", context="nogui", 
			longDescription=StringToModel.COLUMN_LONG_DESCRIPTION, exampleStringValue = StringToModel.COLUMN_EXAMPLE)
	public String keyColumn = null;

	@Tunable (description="Type of key column", context="nogui", 
			longDescription=StringToModel.KEY_TYPE_LONG_DESCRIPTION, exampleStringValue = StringToModel.KEY_TYPE_EXAMPLE)
	public ListSingleSelection<String> keyColumnType =  
		new ListSingleSelection<>("integer", "long", "double", "string", "boolean");

	public CreateTableTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
	}

	@Override
	public void run(final TaskMonitor tm) {
		if (keyColumn == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "Name of key column must be specified");
			return;
		}

		Class keyType = DataUtils.getType(keyColumnType.getSelectedValue());
		if (keyType == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "Key column type must be specified");
			return;
		}

		if (title == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "Table title must be specified");
			return;
		}

		table = serviceRegistrar.getService(CyTableFactory.class).createTable(title, keyColumn, keyType, true, true);
		
		if (table != null) {
			tm.showMessage(TaskMonitor.Level.INFO,  "Created table '"+table.toString()+"' (suid:"+table.getSUID()+")");
			serviceRegistrar.getService(CyTableManager.class).addTable(table);
		} else {
			tm.showMessage(TaskMonitor.Level.ERROR, "Unable to create table'"+title+"'");
		}
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(CyTable.class, String.class, JSONResult.class);
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(CyTable.class)) 				return table;
		if (requestedType.equals(String.class)) 			return "" + table.getSUID();
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {		return "{\"table\":" + table.getSUID() + "}";	};
			return res;
		}
		return null;
	}
}
