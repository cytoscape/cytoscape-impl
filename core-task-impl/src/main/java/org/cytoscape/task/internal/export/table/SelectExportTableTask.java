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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListChangeListener;
import org.cytoscape.work.util.ListSelection;
import org.cytoscape.work.util.ListSingleSelection;


public class SelectExportTableTask extends AbstractTask implements RequestsUIHelper, TunableValidator{

	@Tunable(description = "Select a table to export:", gravity = 1.05)
	public ListSingleSelection<String> selectTable;
	
	@ContainsTunables
	public CyTableWriter writer = null;
	
	private TunableUIHelper helper;
	
	private final CyTableWriterManager writerManager;
	private final CyTableManager cyTableManagerServiceRef;
	private final CyNetworkManager cyNetworkManagerServiceRef;
	private final CyApplicationManager cyApplicationManagerServiceRef;

	private HashMap<CyTable, CyNetwork> tableNetworkMap = new HashMap<>();
	private HashMap<String, CyTable> titleTableMap = new HashMap<>();
	
	public SelectExportTableTask(final CyTableWriterManager writerManager, final CyTableManager cyTableManagerServiceRef, 
			final CyNetworkManager cyNetworkManagerServiceRef, final CyApplicationManager cyApplicationManagerServiceRef) {
		this.cyTableManagerServiceRef = cyTableManagerServiceRef;
		this.writerManager = writerManager;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		this.cyApplicationManagerServiceRef = cyApplicationManagerServiceRef;

		populateNetworkTableMap();
		populateSelectTable();
		updateWriter();
	}

	private void populateSelectTable() {
		final List<String> options = new ArrayList<>();
		
		for (CyTable tbl : cyTableManagerServiceRef.getAllTables(false)) {
			String title = tbl.getTitle();
			options.add(title);			
			this.titleTableMap.put(title, tbl);
		}
		
		Collections.sort(options);
		selectTable =  new ListSingleSelection<String>(options);
		selectTable.addListener(new ListChangeListener<String>(){
			@Override
			public void selectionChanged(ListSelection<String> source) {
				updateWriter();
				if(helper != null)
					helper.refresh(SelectExportTableTask.this);
			}

			@Override
			public void listChanged(ListSelection<String> source) {
			}});
	}
	
	private void populateNetworkTableMap() {
		for (CyNetwork net: cyNetworkManagerServiceRef.getNetworkSet()) {
			this.tableNetworkMap.put(net.getDefaultNetworkTable(), net);
			this.tableNetworkMap.put(net.getDefaultNodeTable(), net);
			this.tableNetworkMap.put(net.getDefaultEdgeTable(), net);
		}
	}
	
	private void updateWriter() {
		final String selectedTitle = selectTable.getSelectedValue();		
		CyTable tbl = titleTableMap.get(selectedTitle);
		if(tbl != null)
			writer = new CyTableWriter(writerManager, cyApplicationManagerServiceRef, tbl);
		else
			writer = null;
	}

	@Override
	public void run(final TaskMonitor tm) throws IOException {
		this.insertTasksAfterCurrentTask(writer);		
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Export Table";
	}

	@Override
	public void setUIHelper(TunableUIHelper helper) {
		this.helper = helper;
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if(selectTable.getPossibleValues().isEmpty()) {
			try {
				errMsg.append("No tables exist.");
			} catch (IOException e) {
			}
			return ValidationState.INVALID;
		}
		else return writer.getValidationState(errMsg);
	}
}
