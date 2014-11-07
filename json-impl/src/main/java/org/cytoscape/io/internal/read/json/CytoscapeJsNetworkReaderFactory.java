package org.cytoscape.io.internal.read.json;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class CytoscapeJsNetworkReaderFactory extends AbstractReaderFactory {

	private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager;

	public CytoscapeJsNetworkReaderFactory(final CyFileFilter filter, final CyNetworkViewFactory cyNetworkViewFactory,
			final CyNetworkFactory cyNetworkFactory, final CyNetworkManager cyNetworkManager,
			final CyRootNetworkManager cyRootNetworkManager) {
		super(filter, cyNetworkViewFactory, cyNetworkFactory);
		
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream is, String collectionName) {
		return new TaskIterator(new CytoscapeJsNetworkReader(collectionName, is, cyNetworkViewFactory, cyNetworkFactory,
				cyNetworkManager, cyRootNetworkManager));
	}
}