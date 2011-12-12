package org.cytoscape.biopax.internal.action;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExportAsBioPAXTaskFactory implements CyNetworkViewWriterFactory, TaskFactory {

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
	public TaskIterator createTaskIterator() {
		return new TaskIterator(getWriterTask());
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
