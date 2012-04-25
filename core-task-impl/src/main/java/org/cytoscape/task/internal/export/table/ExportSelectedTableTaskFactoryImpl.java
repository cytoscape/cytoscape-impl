package org.cytoscape.task.internal.export.table;


import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.write.ExportSelectedTableTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.AbstractTaskFactory;

public class ExportSelectedTableTaskFactoryImpl extends AbstractTaskFactory implements ExportSelectedTableTaskFactory{

	private final CyTableWriterManager writerManager;
	private final CyTableManager cyTableManagerServiceRef;
	private final CyNetworkManager cyNetworkManagerServiceRef;


	
	public ExportSelectedTableTaskFactoryImpl(CyTableWriterManager writerManager, CyTableManager cyTableManagerServiceRef, CyNetworkManager cyNetworkManagerServiceRef) {
		this.writerManager = writerManager;
		this.cyTableManagerServiceRef = cyTableManagerServiceRef;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SelectExportTableTask(this.writerManager, this.cyTableManagerServiceRef, this.cyNetworkManagerServiceRef));
	}


}
