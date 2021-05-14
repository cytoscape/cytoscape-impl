package org.cytoscape.task.internal.export.table;

import java.io.IOException;
import java.util.HashMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

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

public class NoGuiSelectExportTableTask extends AbstractTask {

	@ContainsTunables
	public TableTunable selectTable;

	@ContainsTunables
	public CyTableWriter tableWriter;
	
	private CyTable tbl;
	private HashMap<CyTable, CyNetwork> tableNetworkMap = new HashMap<>();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public NoGuiSelectExportTableTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;

		populateNetworkTableMap();
		selectTable = new TableTunable(serviceRegistrar);

		// Grab an arbitrary table
		CyTable table = getFirstTable();
		
		if (table == null)
			throw new RuntimeException("No tables available to export");

		// We need to initialize this here to the Tunables get picked up.
		tableWriter = new CyTableWriter(table, serviceRegistrar);
	}

	private void populateNetworkTableMap() {
		var netManager = serviceRegistrar.getService(CyNetworkManager.class);
		
		for (var net : netManager.getNetworkSet()) {
			tableNetworkMap.put(net.getDefaultNetworkTable(), net);
			tableNetworkMap.put(net.getDefaultNodeTable(), net);
			tableNetworkMap.put(net.getDefaultEdgeTable(), net);
		}
	}

	private CyTable getFirstTable() {
		var tableManager = serviceRegistrar.getService(CyTableManager.class);
		
		for (CyTable table : tableManager.getAllTables(true))
			return table;

		return null;
	}
	
	@Override
	public void run(TaskMonitor tm) throws IOException {
		//Get the selected table
		tbl = this.selectTable.getTable();
		tableWriter = new CyTableWriter(tbl, serviceRegistrar);

		// Export the selected table
		insertTasksAfterCurrentTask(tableWriter);		
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Export Table";
	}
}
