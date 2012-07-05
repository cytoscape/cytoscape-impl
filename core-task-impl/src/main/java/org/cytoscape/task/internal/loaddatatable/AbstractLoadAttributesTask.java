package org.cytoscape.task.internal.loaddatatable;


import java.net.URI;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.internal.table.MapTableToNetworkTablesTask;
import org.cytoscape.task.internal.table.MapTableToNetworkTablesTaskFactoryImpl;
import org.cytoscape.task.internal.table.UpdateAddedNetworkAttributes;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;


abstract class AbstractLoadAttributesTask extends AbstractTask {

	
	private final CyTableReaderManager mgr;
	private final CyNetworkManager netMgr;
	private final CyTableManager tableMgr;
	private final UpdateAddedNetworkAttributes updateAddedNetworkAttributes;

	public AbstractLoadAttributesTask(final CyTableReaderManager mgr, final CyNetworkManager netMgr, final CyTableManager tabelMgr, 
			final UpdateAddedNetworkAttributes updateAddedNetworkAttributes) {
		this.mgr = mgr;
		this.netMgr = netMgr;
		this.tableMgr = tabelMgr;
		this.updateAddedNetworkAttributes = updateAddedNetworkAttributes;
	}

	void loadTable(final String name, final URI uri, final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Finding Attribute Data Reader...");

		CyTableReader reader = mgr.getReader(uri,uri.toString());

		
		if (reader == null)
			throw new NullPointerException("Failed to find reader for specified file.");

		taskMonitor.setStatusMessage("Importing Data Table...");
		
		insertTasksAfterCurrentTask(new CombineReaderAndMappingTask( reader, netMgr, updateAddedNetworkAttributes) , new AddImportedTableTask(tableMgr, reader));
		
	}
}

