package org.cytoscape.task.internal.export.table;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.export.table.CurrentTableExporter;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListSingleSelection;

public class ExportCurrentTableTaskFactory extends AbstractTableTaskFactory implements CurrentTableExporter{

	private final CyTableWriterManager writerManager;
	private final CyTableManager cyTableManagerServiceRef;
	private final CyNetworkManager cyNetworkManagerServiceRef;

	private final TunableSetter tunableSetter; 

	
	public ExportCurrentTableTaskFactory(CyTableWriterManager writerManager, CyTableManager cyTableManagerServiceRef, CyNetworkManager cyNetworkManagerServiceRef
			,TunableSetter tunableSetter) {
		this.writerManager = writerManager;
		this.cyTableManagerServiceRef = cyTableManagerServiceRef;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		this.tunableSetter = tunableSetter;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyTable table) {
		return new TaskIterator(new SelectExportTableTask(this.writerManager, this.cyTableManagerServiceRef, this.cyNetworkManagerServiceRef));
	}

	@Override
	public TaskIterator createTaskIterator(CyTable table,
			ListSingleSelection<String> selectTable) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("selectTable", selectTable);

		return tunableSetter.createTaskIterator(this.createTaskIterator(table), m); 
	}
}
