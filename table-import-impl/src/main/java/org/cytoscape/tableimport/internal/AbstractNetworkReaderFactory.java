package org.cytoscape.tableimport.internal;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.SimpleInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;

// Copy from io-impl
public abstract class AbstractNetworkReaderFactory extends SimpleInputStreamTaskFactory {

	protected final CyNetworkViewFactory cyNetworkViewFactory;
	protected final CyNetworkFactory cyNetworkFactory;

	public AbstractNetworkReaderFactory(CyFileFilter filter, CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory) {
		super(filter);
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
	}
}
