package org.cytoscape.io.internal.write;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyPropertyWriterFactory;

public abstract class AbstractPropertyWriterFactory implements CyPropertyWriterFactory {
	
	private final CyFileFilter thisFilter;

	protected OutputStream outputStream;
	protected Object props;

	public AbstractPropertyWriterFactory(CyFileFilter thisFilter) {
		this.thisFilter = thisFilter;
	}
	
	@Override
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public CyFileFilter getFileFilter() {
		return thisFilter;
	}

	@Override
	public void setProperty(Object props) {
		this.props = props;
	}
}
