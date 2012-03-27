package org.cytoscape.io.internal.read.graphml;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class GraphMLReaderFactory extends AbstractInputStreamTaskFactory {

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyRootNetworkManager cyRootNetworkFactory;

	private final CyLayoutAlgorithmManager layouts;

	public GraphMLReaderFactory(CyFileFilter filter, final CyLayoutAlgorithmManager layouts,
			CyNetworkViewFactory cyNetworkViewFactory, CyNetworkFactory cyNetworkFactory,
			final CyRootNetworkManager cyRootNetworkFactory) {
		super(filter);
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyRootNetworkFactory = cyRootNetworkFactory;
		this.layouts = layouts;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new GraphMLReader(inputStream, layouts, cyNetworkFactory, cyNetworkViewFactory,
				cyRootNetworkFactory));
	}
}
