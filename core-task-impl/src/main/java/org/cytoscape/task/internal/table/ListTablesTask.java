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
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.cytoscape.task.internal.utils.DataUtils;

public class ListTablesTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	final CyNetworkTableManager networkTableMgr;
	List<CyTable> tables;

	@Tunable(description="Type of table", context="nogui")
	public ListSingleSelection<String> type;

	@Tunable(description="Table namespace", context="nogui")
	public String namespace = "default";

	@Tunable(description="Include private tables?", context="nogui")
	public boolean includePrivate = true;

	public ListTablesTask(CyApplicationManager appMgr, CyTableManager tableMgr, 
	                      CyNetworkTableManager networkTableMgr) {
		super(tableMgr);
		this.appMgr = appMgr;
		this.networkTableMgr = networkTableMgr;
		type = new ListSingleSelection<>("network", "node", "edge", "unattached", "all");
		type.setSelectedValue("all");
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		Set<CyTable> allTables = cyTableManager.getAllTables(includePrivate);
		tables = new ArrayList<>();
		String requestedType = type.getSelectedValue();
		String requestedNamespace = getNamespace(namespace);

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
			taskMonitor.showMessage(TaskMonitor.Level.INFO, "Table "+getTableDescription(table));
			tables.add(table);
		}
	}

	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) {
			return DataUtils.convertData(tables);
		}
		return tables;
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
