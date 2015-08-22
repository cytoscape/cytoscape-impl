package org.cytoscape.io.internal.write.graphml;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class GraphMLNetworkWriterFactory implements CyNetworkViewWriterFactory {

	private final CyFileFilter filter;

	public GraphMLNetworkWriterFactory(CyFileFilter filter) {
		this.filter = filter;
	}

	@Override
	public CyWriter createWriter(OutputStream outputStream, CyNetworkView view) {
		return new GraphMLWriter(outputStream, view.getModel());
	}

	@Override
	public CyWriter createWriter(OutputStream outputStream, CyNetwork network) {
		return new GraphMLWriter(outputStream, network);
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}
}
