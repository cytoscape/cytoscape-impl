package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.command.StringToModel;
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
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class SetTableTitleTask extends AbstractTableDataTask implements ObservableTask {
	
	private CyTable table;

	@ContainsTunables
	public TableTunable tableTunable;

	@Tunable(description="New table title", context="nogui",
			longDescription=StringToModel.TABLE_TITLE_LONG_DESCRIPTION, exampleStringValue = "Filtered Edges")
	public String title ;

	public SetTableTitleTask(CyServiceRegistrar serviceRegistrar) {
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

		if (title == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "New title must be specified");
			return;
		}

		String oldTitle = table.getTitle();
		table.setTitle(title);
		tm.showMessage(TaskMonitor.Level.INFO, "Changed title of table '"+oldTitle+"' to '"+title+"'");
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, JSONResult.class);	
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) 		return title;
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (table == null) return "{}";
				return "{\"table\":"+table.getSUID()+", \"title\":\""+title+"\"}";	
			};
			return res;
		}
		return null;
	}
}
