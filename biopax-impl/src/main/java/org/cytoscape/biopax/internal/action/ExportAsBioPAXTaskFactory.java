package org.cytoscape.biopax.internal.action;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class ExportAsBioPAXTaskFactory implements CyNetworkViewWriterFactory {

	private final CyFileFilter filter;
	private OutputStream outputStream;
	private final String fileName;

	public ExportAsBioPAXTaskFactory(String fileName, CyFileFilter filter) {
		this.filter = filter;
		this.fileName = fileName;
	}
	
	@Override
	public CyWriter createWriter(OutputStream outputStream, CyNetwork network) {
		return new ExportAsBioPAXTask(fileName, outputStream, network);
	}

	@Override
	public CyWriter createWriter(OutputStream outputStream, CyNetworkView view) {
		return new ExportAsBioPAXTask(fileName, outputStream, view.getModel());
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}
	
}
