package org.cytoscape.io.internal.read.json;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class CytoscapeJsNetworkReaderFactory extends AbstractReaderFactory {

	public CytoscapeJsNetworkReaderFactory(final CyFileFilter filter,
			final CyNetworkViewFactory cyNetworkViewFactory, final CyNetworkFactory cyNetworkFactory) {
		super(filter, cyNetworkViewFactory, cyNetworkFactory);
	}

	@Override
	public TaskIterator createTaskIterator(InputStream is, String inputName) {
		return new TaskIterator(new CytoscapeJsNetworkReader(is, cyNetworkViewFactory, cyNetworkFactory));
	}

}
