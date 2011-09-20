package org.cytoscape.io.internal.write;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.view.model.CyNetworkView;

public abstract class AbstractCyNetworkViewWriterFactory implements CyNetworkViewWriterFactory {

	private final CyFileFilter filter;
	
	protected OutputStream outputStream;
	protected CyNetworkView view;

	public AbstractCyNetworkViewWriterFactory(CyFileFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public CyFileFilter getCyFileFilter() {
		return filter;
	}

	@Override
	public void setNetworkView(CyNetworkView view) {
		this.view = view;
	}
}
