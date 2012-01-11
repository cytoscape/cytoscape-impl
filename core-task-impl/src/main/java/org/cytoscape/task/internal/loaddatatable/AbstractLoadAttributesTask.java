package org.cytoscape.task.internal.loaddatatable;


import java.net.URI;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;


abstract class AbstractLoadAttributesTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Import Table";
	}
	
	private final CyTableReaderManager mgr;

	public AbstractLoadAttributesTask(final CyTableReaderManager mgr) {
		this.mgr = mgr;
	}

	void loadTable(final String name, final URI uri, final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Finding Attribute Data Reader...");

		CyTableReader reader = mgr.getReader(uri,uri.toString());

		if (reader == null)
			throw new NullPointerException("Failed to find reader for specified file!");

		taskMonitor.setStatusMessage("Importing Data Table...");

		insertTasksAfterCurrentTask(reader, new FinalStatusMessageUpdateTask(reader));
	}
}

