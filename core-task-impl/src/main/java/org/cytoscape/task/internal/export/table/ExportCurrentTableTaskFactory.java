package org.cytoscape.task.internal.export.table;

import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExportCurrentTableTaskFactory extends AbstractTableTaskFactory {

	private final CyTableWriterManager writerManager;
	private final CyTableManager cyTableManagerServiceRef;
	private final CyNetworkManager cyNetworkManagerServiceRef;
	
	public ExportCurrentTableTaskFactory(CyTableWriterManager writerManager, CyTableManager cyTableManagerServiceRef, CyNetworkManager cyNetworkManagerServiceRef) {
		this.writerManager = writerManager;
		this.cyTableManagerServiceRef = cyTableManagerServiceRef;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SelectExportTableTask(this.writerManager, this.cyTableManagerServiceRef, this.cyNetworkManagerServiceRef));
	}
}
