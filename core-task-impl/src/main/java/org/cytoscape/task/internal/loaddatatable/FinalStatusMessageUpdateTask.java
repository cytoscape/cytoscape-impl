package org.cytoscape.task.internal.loaddatatable;


import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


class FinalStatusMessageUpdateTask extends AbstractTask {
	private final CyTableReader reader;
	
	FinalStatusMessageUpdateTask(final CyTableReader reader) {
		this.reader = reader;
	}

	public void run(final TaskMonitor taskMonitor) throws Exception {
		for (CyTable table : reader.getTables())
			taskMonitor.setStatusMessage("Successfully loaded attribute table: " + table.getTitle());

		taskMonitor.setProgress(1.0);
	}
}
