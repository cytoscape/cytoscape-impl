package org.cytoscape.io.internal.write;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriterFactory;

public abstract class AbstractCyWriterFactory implements CyWriterFactory {

	private final CyFileFilter filter;
	
	public AbstractCyWriterFactory(CyFileFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}
}
