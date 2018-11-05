package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
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

public final class RenameColumnCommandTask extends AbstractTask implements ObservableTask {
	
	private CyTable table;
	
	@ProvidesTitle
	public String getTitle() {
		return "Rename Column";
	}
	
	@Tunable(description="New Column Name",
	         longDescription="The new name of the column.",
	         exampleStringValue="NewColumnName",
	         required=true)
	public String newColumnName;

	@ContainsTunables
	public TableTunable tableTunable;

	@Tunable(description="Column name",
	         longDescription="The name of the column that will be renamed.",
					 exampleStringValue="ColumnName",
	         context="nogui",
	         required=true)
	public String columnName;

	CyColumn column;

	RenameColumnCommandTask(CyServiceRegistrar serviceRegistrar) {
		tableTunable = new TableTunable(serviceRegistrar);
	}

	@Override
	public void run(final TaskMonitor tm) throws Exception {
		table = tableTunable.getTable();
		if (table == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "Unable to find table '"+tableTunable.getTableString()+"'");
			return;
		}

		if (columnName == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "Column name must be specified");
			return;
		}

		if (newColumnName == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "New column name must be specified");
			return;
		}

		column = table.getColumn(columnName);
		if (column == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "Can't find column "+columnName+" in table "+table.toString());
			return;
		}
		column.setName(newColumnName);
		return;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)) {
			if (column == null)
				return (R)"Unable to rename column";
			String res = "Renamed column "+columnName+" in table "+tableTunable.getTable()+" to "+newColumnName;
			return (R)res;
		} else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {if (table == null || column == null)
				return "{}";
			else {
				return "{\"table\":"+table.getSUID()+", \"column\":\""+newColumnName+"\"}";
			}};
			return (R)res;
		}
		return (R)column;
	}

	@Override
	public List<Class<?>> getResultClasses() {	
		return Arrays.asList(CyColumn.class, String.class, JSONResult.class);	
	}
}
