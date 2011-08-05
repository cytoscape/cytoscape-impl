package org.cytoscape.io.internal.read.datatable;


import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.internal.read.AbstractTableReaderFactory;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.work.TaskIterator;


public class TextDataTableReaderFactory extends AbstractTableReaderFactory {
	public TextDataTableReaderFactory(CyFileFilter filter, CyTableFactory factory) {
		super(filter, factory);
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new TextDataTableReader(inputStream, tableFactory));
	}
}
