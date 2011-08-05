package org.cytoscape.tableimport.internal;

import java.io.InputStream;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyTableFactory;

// Copy from io-impl
public abstract class AbstractTableReaderFactory implements InputStreamTaskFactory {
	private final CyFileFilter filter;

	protected InputStream inputStream;
	protected final CyTableFactory tableFactory;
	protected String streamName = null;

	public AbstractTableReaderFactory(CyFileFilter filter, CyTableFactory tableFactory) {
		if (filter == null)
			throw new NullPointerException("filter is null");
		this.filter = filter;

		if (tableFactory == null)
			throw new NullPointerException("tableFactory is null");
		this.tableFactory = tableFactory;
	}

	public void setInputStream(InputStream is, String name) {
		if (is == null)
			throw new NullPointerException("Input stream is null");
		inputStream = is;
		this.streamName = name;
	}

	public CyFileFilter getCyFileFilter() {
		return filter;
	}
}

