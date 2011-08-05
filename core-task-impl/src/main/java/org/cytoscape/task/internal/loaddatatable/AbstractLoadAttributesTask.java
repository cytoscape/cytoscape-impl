package org.cytoscape.task.internal.loaddatatable;


import java.net.URI;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


abstract class AbstractLoadAttributesTask extends AbstractTask {

	private final CyTableReaderManager mgr;
	private CyTableManager tableMgr;

	public AbstractLoadAttributesTask(final CyTableReaderManager mgr, CyTableManager tableMgr) {
		this.mgr = mgr;
		this.tableMgr = tableMgr;
	}

	void loadTable(final String name, final URI uri, final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Finding Attribute Data Reader...");
		taskMonitor.setProgress(-1.0);
		CyTableReader reader = mgr.getReader(uri,uri.toString());

		if (reader == null)
			throw new NullPointerException("Failed to find reader for specified file!");

		taskMonitor.setStatusMessage("Importing Data Table...");

		insertTasksAfterCurrentTask(reader, new FinalStatusMessageUpdateTask(reader, tableMgr, name));
	}
}

