package org.cytoscape.task.internal.export.table;

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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.util.ListSingleSelection;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.io.write.CyTableWriterManager;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Collections;


public class NoGuiSelectExportTableTask extends AbstractTask {

	@Tunable(description = "Select a table to export:")
	public  ListSingleSelection<String> selectTable;
	
	@ContainsTunables
	public CyTableWriter tableWriter;
	
	private final CyTableWriterManager writerManager;
	private final CyTableManager cyTableManagerServiceRef;
	private final CyNetworkManager cyNetworkManagerServiceRef;

	private HashMap<CyTable, CyNetwork> tableNetworkMap = new HashMap<CyTable, CyNetwork>();
	private HashMap<String, CyTable> titleTableMap = new HashMap<String, CyTable>();
	
	public NoGuiSelectExportTableTask (CyTableWriterManager writerManager,CyTableManager cyTableManagerServiceRef, CyNetworkManager cyNetworkManagerServiceRef){
		this.cyTableManagerServiceRef = cyTableManagerServiceRef;
		this.writerManager = writerManager;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;

		populateNetworkTableMap();
		populateSelectTable();
		
		
		tableWriter = new CyTableWriter(writerManager, titleTableMap.get(selectTable.getSelectedValue()));
		
		
	}

	private void populateSelectTable() {
		final List<String> options = new ArrayList<String>();
		
		for ( CyTable tbl : cyTableManagerServiceRef.getAllTables(false)) {
			String title = tbl.getTitle();
			options.add(title);			
			this.titleTableMap.put(title, tbl);
		}
		
		Collections.sort(options);
		selectTable =  new ListSingleSelection<String>(options);
	}
	
	
	private void populateNetworkTableMap() {
		
		for (CyNetwork net: cyNetworkManagerServiceRef.getNetworkSet()) {
			this.tableNetworkMap.put(net.getDefaultNetworkTable(), net);
			this.tableNetworkMap.put(net.getDefaultNodeTable(), net);
			this.tableNetworkMap.put(net.getDefaultEdgeTable(), net);
		}
	}
	

	@Override
	public void run(TaskMonitor tm) throws IOException {

		//Get the selected table
		final String selectedTitle = selectTable.getSelectedValue();		
		CyTable tbl = this.titleTableMap.get(selectedTitle);
		tableWriter = new CyTableWriter(writerManager, tbl);

		// Export the selected table
		this.insertTasksAfterCurrentTask(tableWriter);		
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Export Table";
	}
}
