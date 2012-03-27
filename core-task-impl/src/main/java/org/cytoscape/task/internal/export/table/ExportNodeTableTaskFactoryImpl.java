package org.cytoscape.task.internal.export.table;

import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class ExportNodeTableTaskFactoryImpl extends AbstractNetworkViewTaskFactory {

	private final CyTableWriterManager writerManager;

	public ExportNodeTableTaskFactoryImpl(CyTableWriterManager writerManager) {
		this.writerManager = writerManager;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view) {
		CyTable table = view.getModel().getDefaultNodeTable();
		return new TaskIterator(2,new CyTableWriter(writerManager, table));
	}

}
