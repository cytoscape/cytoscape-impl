package org.cytoscape.biopax.internal.action;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class ExportAsBioPAXTaskFactory implements CyNetworkViewWriterFactory {

	private final CyFileFilter filter;
	private CyNetwork network;
	private OutputStream outputStream;
	private final String fileName;

	public ExportAsBioPAXTaskFactory(String fileName, CyFileFilter filter) {
		this.filter = filter;
		this.fileName = fileName;
	}
	
	@Override
	public CyWriter getWriterTask() {
		return new ExportAsBioPAXTask(fileName, outputStream, network);
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
	}

	@Override
	public void setNetworkView(CyNetworkView view) {
		this.network = view.getModel();
	}

}
