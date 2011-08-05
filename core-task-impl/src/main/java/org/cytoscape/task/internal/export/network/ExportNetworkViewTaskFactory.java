package org.cytoscape.task.internal.export.network;

import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExportNetworkViewTaskFactory extends AbstractNetworkViewTaskFactory {

	private CyNetworkViewWriterManager writerManager;

	public ExportNetworkViewTaskFactory(CyNetworkViewWriterManager writerManager) {
		this.writerManager = writerManager;
	}
	
	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new CyNetworkViewWriter(writerManager, view));
	}

}
