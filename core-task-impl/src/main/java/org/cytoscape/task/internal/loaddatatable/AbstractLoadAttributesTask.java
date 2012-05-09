package org.cytoscape.task.internal.loaddatatable;


import java.net.URI;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.internal.table.MapTableToNetworkTablesTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;


abstract class AbstractLoadAttributesTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Import Table";
	}
	
	private final CyTableReaderManager mgr;
	private final CyNetworkManager netMgr;
	private final CyTableManager tableMgr;

	public AbstractLoadAttributesTask(final CyTableReaderManager mgr, final CyNetworkManager netMgr, final CyTableManager tabelMgr) {
		this.mgr = mgr;
		this.netMgr = netMgr;
		this.tableMgr = tabelMgr;
	}

	void loadTable(final String name, final URI uri, final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Finding Attribute Data Reader...");

		CyTableReader reader = mgr.getReader(uri,uri.toString());

		
		if (reader == null)
			throw new NullPointerException("Failed to find reader for specified file!");

		taskMonitor.setStatusMessage("Importing Data Table...");
		
		insertTasksAfterCurrentTask(reader ,new AddImportedTableTask(tableMgr, reader), new MapTableToNetworkTablesTask(netMgr, reader), new FinalStatusMessageUpdateTask(reader));
	}
}

