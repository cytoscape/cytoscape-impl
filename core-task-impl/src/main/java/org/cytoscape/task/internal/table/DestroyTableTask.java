package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
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

public class DestroyTableTask extends AbstractTableDataTask implements ObservableTask {
	
	private long tableSUID = -1L;
	private String title;
	
	@ContainsTunables
	public TableTunable tableTunable;

	public DestroyTableTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		tableTunable = new TableTunable(serviceRegistrar);
	}
	
	@Override
	public void run(final TaskMonitor tm) {
		CyTable table = tableTunable.getTable();
		if (table == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Unable to find table '"+tableTunable.getTableString()+"'");
			return;
		}

		tableSUID = table.getSUID();
		title = table.getTitle();
		String withId = title +" (suid:"+table.getSUID()+")";
		serviceRegistrar.getService(CyTableManager.class).deleteTable(table.getSUID());
		tm.showMessage(TaskMonitor.Level.INFO,  "Deleted table '" + withId + "'");
	}

	@Override
	public List<Class<?>> getResultClasses() {	
		return Arrays.asList(String.class, JSONResult.class);	
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) 		return title;
		if (requestedType.equals(JSONResult.class)) {
			// JSONResult res = () -> {		return "{ \"title\": \"" + title + "\" }";	};	
			JSONResult res = () -> { 
				if (tableSUID < 0L)
					return "{}";
				return "{\"table\":"+tableSUID+"}";
			};
			return res;
		}
		return null;
	}
}
