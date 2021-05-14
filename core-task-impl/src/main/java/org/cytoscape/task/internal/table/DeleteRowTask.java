package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
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

public class DeleteRowTask extends AbstractTableDataTask implements ObservableTask {
	
	CyRow row;
	CyTable table;

	@ContainsTunables
	public TableTunable tableTunable;

	@Tunable(description="Key value for row to delete", context="nogui", required=true,
			longDescription=StringToModel.VALUE_LONG_DESCRIPTION, exampleStringValue = StringToModel.VALUE_EXAMPLE)

	public String keyValue;

	public DeleteRowTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		tableTunable = new TableTunable(serviceRegistrar);
	}

	@Override
	public void run(final TaskMonitor tm) {
		table = tableTunable.getTable();
		if (table == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "Unable to find table '"+tableTunable.getTableString()+"'");
			return;
		}
		if (keyValue == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "Key of row to delete must be specified");
			return;
		}

		// Get the primary key column
		CyColumn primaryKColumn = table.getPrimaryKey();
		Class keyType = primaryKColumn.getType();
		Object key = null;
		try {
			key = DataUtils.convertString(keyValue, keyType);
		} catch (NumberFormatException nfe) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Unable to convert "+keyValue+" to a "+keyType.getName()+": "+nfe.getMessage());
			return;
		}
		if (key == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Unable to convert "+keyValue+" to a "+keyType.getName());
			return;
		}
		if (!table.rowExists(key)) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Row "+keyValue+" doesn't exist");
			return;
		}
		
		table.deleteRows(Collections.singletonList(key));
		tm.showMessage(TaskMonitor.Level.INFO,  "Deleted row '"+keyValue+"'");
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, JSONResult.class);
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class))			return keyValue;
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {		
				if (table == null || keyValue == null) return "{}";
				return "{\"table\":"+table.getSUID()+",\"key\":\"" + keyValue + "\"}";	
			};	
			return res;
			}
		return null;
	}
}
