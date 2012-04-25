package org.cytoscape.task.internal.export.table;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.write.ExportCurrentTableTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListSingleSelection;

public class ExportCurrentTableTaskFactoryImpl extends AbstractTableTaskFactory implements ExportCurrentTableTaskFactory{

	private final CyTableWriterManager writerManager;
	private final CyTableManager cyTableManagerServiceRef;
	private final CyNetworkManager cyNetworkManagerServiceRef;

	private final TunableSetter tunableSetter; 

	
	public ExportCurrentTableTaskFactoryImpl(CyTableWriterManager writerManager, CyTableManager cyTableManagerServiceRef, CyNetworkManager cyNetworkManagerServiceRef
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
	public TaskIterator createTaskIterator(CyTable table, String format) {
		final Map<String, Object> m = new HashMap<String, Object>();

		ListSingleSelection<String> formatSelection = new ListSingleSelection<String>(format);
		formatSelection.setSelectedValue(format);

		m.put("options", formatSelection);

		return tunableSetter.createTaskIterator(new TaskIterator(2, new CyTableWriter(writerManager, table)), m);

	}
}
