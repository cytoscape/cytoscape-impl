package org.cytoscape.io.internal.read.json;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;

public abstract class AbstractReaderFactory implements InputStreamTaskFactory {

	private final CyFileFilter fileFilter;

	protected final CyNetworkViewFactory cyNetworkViewFactory;
	protected final CyNetworkFactory cyNetworkFactory;

	public AbstractReaderFactory(final CyFileFilter filter,
			final CyNetworkViewFactory cyNetworkViewFactory, final CyNetworkFactory cyNetworkFactory) {
		this.fileFilter = filter;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
	}

	@Override
	public CyFileFilter getFileFilter() {
		return this.fileFilter;
	}

	@Override
	public boolean isReady(final InputStream is, final String inputName) {
		return true;
	}

}
