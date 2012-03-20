package org.cytoscape.tableimport.internal;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.SimpleInputStreamTaskFactory;
import org.cytoscape.model.CyTableFactory;

// Copy from io-impl
public abstract class AbstractTableReaderFactory extends SimpleInputStreamTaskFactory {

	protected final CyTableFactory tableFactory;


	public AbstractTableReaderFactory(CyFileFilter filter, CyTableFactory tableFactory) {
		super(filter);
		if (filter == null)
			throw new NullPointerException("filter is null");

		if (tableFactory == null)
			throw new NullPointerException("tableFactory is null");
		this.tableFactory = tableFactory;
	}
}

