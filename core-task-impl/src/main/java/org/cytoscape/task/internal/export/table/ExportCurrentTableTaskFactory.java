package org.cytoscape.task.internal.export.table;

import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExportCurrentTableTaskFactory extends AbstractTableTaskFactory {

	private final CyTableWriterManager writerManager;

	public ExportCurrentTableTaskFactory(CyTableWriterManager writerManager) {
		this.writerManager = writerManager;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(2,new CyTableWriter(writerManager, table));
	}
}
