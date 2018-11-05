package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
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
 * Copyright (C) 2010 - 2018 The Cytoscape Consortium
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

public class DeleteColumnCommandTask extends AbstractTableDataTask implements ObservableTask {
	
	private CyTable table;

	@ContainsTunables
	public TableTunable tableTunable;

	@Tunable(description="Name of column to delete", 
	                      longDescription=StringToModel.COLUMN_LONG_DESCRIPTION, 
	                      exampleStringValue = StringToModel.COLUMN_EXAMPLE, context="nogui")
	public String column = null;

	public DeleteColumnCommandTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		tableTunable = new TableTunable(serviceRegistrar);
	}

	@Override
	public void run(final TaskMonitor tm) {
		table = tableTunable.getTable();
		if (table == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, 
			                        "Unable to find table '"+tableTunable.getTableString()+"'");
			return;
		}

		if (column == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, 
			                        "Column name must be specified");
			return;
		}

		CyColumn col = table.getColumn(column);
		if (col == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, 
			                        "Can't find a '"+column+"' column in table: "+table.toString());
			return;
		}

		if (col.isPrimaryKey()) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Can't delete primary key column");
			return;
		}

		table.deleteColumn(column);

		tm.showMessage(TaskMonitor.Level.INFO, "Deleted column: "+column);

	}

	@Override
	public List<Class<?>> getResultClasses() {	
		return Arrays.asList(CyColumn.class, String.class, JSONResult.class);	
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) 		return (column == null) ? "" : column;
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (table == null || column == null)
					return "{}";
				return "{\"table\":" + table.getSUID() + ",\"column\":\"" + column + "\"}";
			};
			return res;
		}
		return null;
	}
}
