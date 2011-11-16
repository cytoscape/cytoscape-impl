package org.cytoscape.task.internal.loaddatatable;


import java.io.File;

import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


public class LoadAttributesFileTask extends AbstractLoadAttributesTask {
	@Tunable(description="Attribute Table file", params="fileCategory=table;input=true")
	public File file;

	public LoadAttributesFileTask(final CyTableReaderManager mgr, CyTableManager tableMgr) {
		super(mgr, tableMgr);
	}

	/**
	 * Executes Task.
	 */
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(-1);
		loadTable(file.getName(), file.toURI(), taskMonitor);
	}
}

