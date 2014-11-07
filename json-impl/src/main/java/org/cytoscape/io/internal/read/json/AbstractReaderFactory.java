package org.cytoscape.io.internal.read.json;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;

public abstract class AbstractReaderFactory extends AbstractInputStreamTaskFactory {


	protected final CyNetworkViewFactory cyNetworkViewFactory;
	protected final CyNetworkFactory cyNetworkFactory;

	public AbstractReaderFactory(final CyFileFilter filter,
			final CyNetworkViewFactory cyNetworkViewFactory, final CyNetworkFactory cyNetworkFactory) {
		super(filter);
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
	}
}
