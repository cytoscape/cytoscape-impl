package org.cytoscape.task.internal.export.table;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.write.ExportTableTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class ExportTableTaskFactoryImpl extends org.cytoscape.task.AbstractTableTaskFactory
		implements ExportTableTaskFactory {
	private final CyTableWriterManager writerManager;
	private final TunableSetter tunableSetter;
	
	public ExportTableTaskFactoryImpl(CyTableWriterManager writerManager, TunableSetter tunableSetter) {
		super();
		this.writerManager = writerManager;
		this.tunableSetter = tunableSetter;
	}

	@Override
	public TaskIterator createTaskIterator(CyTable table) {
		return new TaskIterator(2, new CyTableWriter(writerManager, table));
	}

	@Override
	public TaskIterator createTaskIterator(CyTable table, File file) {
	
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("OutputFile", file);

		return tunableSetter.createTaskIterator(new TaskIterator(2, new CyTableWriter(writerManager, table)), m);
	}

}
