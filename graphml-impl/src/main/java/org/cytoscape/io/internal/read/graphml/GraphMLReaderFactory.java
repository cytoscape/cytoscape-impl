package org.cytoscape.io.internal.read.graphml;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class GraphMLReaderFactory implements InputStreamTaskFactory {

	private final CyFileFilter filter;

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyRootNetworkManager cyRootNetworkFactory;

	private InputStream inputStream;
	private String inputName;

	private final CyLayoutAlgorithmManager layouts;

	public GraphMLReaderFactory(CyFileFilter filter, final CyLayoutAlgorithmManager layouts,
			CyNetworkViewFactory cyNetworkViewFactory, CyNetworkFactory cyNetworkFactory,
			final CyRootNetworkManager cyRootNetworkFactory) {
		this.filter = filter;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyRootNetworkFactory = cyRootNetworkFactory;
		this.layouts = layouts;
	}

	@Override
	public void setInputStream(InputStream is, String in) {
		if (is == null)
			throw new NullPointerException("Input stream is null");
		inputStream = is;
		inputName = in;
	}

	@Override
	public CyFileFilter getCyFileFilter() {
		return filter;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new GraphMLReader(inputStream, layouts, cyNetworkFactory, cyNetworkViewFactory,
				cyRootNetworkFactory));
	}
}
