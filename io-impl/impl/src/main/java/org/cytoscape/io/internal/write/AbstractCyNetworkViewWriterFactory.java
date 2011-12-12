package org.cytoscape.io.internal.write;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public abstract class AbstractCyNetworkViewWriterFactory implements CyNetworkViewWriterFactory {

	private final CyFileFilter filter;
	
	protected OutputStream outputStream;
	protected CyNetwork network;
	protected CyNetworkView view;

	public AbstractCyNetworkViewWriterFactory(CyFileFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}

	@Override
	public void setNetwork(CyNetwork network) {
		this.network = network;
		
		// Let's keep it consistent!
		if (network != null && view != null && !network.equals(view.getModel())) {
			view = null;
		}
	}
	
	@Override
	public void setNetworkView(CyNetworkView view) {
		this.view = view;
		
		if (view != null) {
			this.network = view.getModel();
		}
	}
}
