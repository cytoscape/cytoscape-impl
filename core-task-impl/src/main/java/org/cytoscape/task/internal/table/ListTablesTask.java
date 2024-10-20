package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.util.json.CyJSONUtil;
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

public class ListTablesTask extends AbstractTableDataTask implements ObservableTask {
	
	List<CyTable> tables;

	@Tunable(description="Type of table", context="nogui", longDescription="One of ''network'', ''node'', ''edge'', ''unattached'', ''all'', to constrain the type of table listed", exampleStringValue = "all")
	public ListSingleSelection<String> type;

	@Tunable(description="Table namespace", context="nogui", longDescription="An optional argument to contrain output to a single namespace, or ALL", exampleStringValue = "all")
	public String namespace = "default";

	@Tunable(description="Include private tables?", context="nogui", longDescription="A boolean value determining whether to return private as well as public tables", exampleStringValue = "true")
	public boolean includePrivate = true;

	public ListTablesTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		type = new ListSingleSelection<String>("network", "node", "edge", "unattached", "all");
		type.setSelectedValue("all");
	}

	@Override
	public void run(final TaskMonitor tm) {
		Set<CyTable> allTables = serviceRegistrar.getService(CyTableManager.class).getAllTables(includePrivate);
		
		tables = new ArrayList<>();
		String requestedType = type.getSelectedValue();
		String requestedNamespace = getNamespace(namespace);

		CyNetworkTableManager networkTableMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		
		for (CyTable table: allTables) {
			if (!requestedType.equals("all")) {
				Class<? extends CyIdentifiable> tableClass = networkTableMgr.getTableType(table);
				if (tableClass != null && !requestedType.equals("unattached")) {
					String strClass = DataUtils.getIdentifiableType(tableClass);
					if (!strClass.equalsIgnoreCase(requestedType))
						continue;
					if (!requestedNamespace.equalsIgnoreCase(networkTableMgr.getTableNamespace(table)))
						continue;
				} else if (tableClass != null && requestedType.equals("unattached")) {
					continue;
				}
			}
			tm.showMessage(TaskMonitor.Level.INFO, "Table "+getTableDescription(table));
			tables.add(table);
		}
	}

	@Override
	public List<Class<?>> getResultClasses() {	
		return Arrays.asList(List.class, String.class, JSONResult.class);
	}

	@Override
	public Object getResults(Class requestedType) {
		System.out.println(requestedType + "---------------");
		if (requestedType.equals(String.class)) {
			return DataUtils.convertData(tables);
		}
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {	
				if (tables == null) {
					return "{}";} 
				else {
					CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
					return "{\"tables\":"+cyJSONUtil.cyIdentifiablesToJson(tables)+"}";
			}
		};
			return res;
		}
		return null;
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
