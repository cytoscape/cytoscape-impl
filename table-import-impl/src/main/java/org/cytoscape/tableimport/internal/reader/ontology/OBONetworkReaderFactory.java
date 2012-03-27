package org.cytoscape.tableimport.internal.reader.ontology;

import java.io.InputStream;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class OBONetworkReaderFactory extends AbstractInputStreamTaskFactory {

	protected final CyNetworkViewFactory cyNetworkViewFactory;
	protected final CyNetworkFactory cyNetworkFactory;

	private final CyEventHelper eventHelper;

	public OBONetworkReaderFactory(CyFileFilter filter) {
		super(filter);
		this.cyNetworkViewFactory = CytoscapeServices.cyNetworkViewFactory;
		this.cyNetworkFactory = CytoscapeServices.cyNetworkFactory;
		this.eventHelper = CytoscapeServices.cyEventHelper;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new OBOReader(inputName, inputStream, cyNetworkViewFactory, cyNetworkFactory, eventHelper));
	}
}
