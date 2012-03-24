package org.cytoscape.task.internal.export.table;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.export.table.EdgeTableExporter;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class ExportEdgeTableTaskFactory extends AbstractNetworkViewTaskFactory implements EdgeTableExporter{

	private final CyTableWriterManager writerManager;

	private final TunableSetter tunableSetter; 

	public ExportEdgeTableTaskFactory(CyTableWriterManager writerManager, TunableSetter tunableSetter) {
		this.writerManager = writerManager;
		this.tunableSetter = tunableSetter;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view) {
		CyTable table = view.getModel().getDefaultEdgeTable();
		return new TaskIterator(2, new CyTableWriter(writerManager, table));
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView view, File outputFile) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("OutputFile", outputFile);

		return tunableSetter.createTaskIterator(this.createTaskIterator(view), m); 
	}

}
