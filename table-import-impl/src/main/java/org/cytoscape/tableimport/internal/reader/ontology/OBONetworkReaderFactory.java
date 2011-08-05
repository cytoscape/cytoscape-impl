package org.cytoscape.tableimport.internal.reader.ontology;

import java.io.InputStream;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class OBONetworkReaderFactory implements InputStreamTaskFactory {

	private final CyFileFilter filter;

	protected final CyNetworkViewFactory cyNetworkViewFactory;
	protected final CyNetworkFactory cyNetworkFactory;

	protected InputStream inputStream;
	protected String inputName;

	private final CyEventHelper eventHelper;

	public OBONetworkReaderFactory(CyFileFilter filter, CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory, final CyEventHelper eventHelper) {
		this.filter = filter;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
		this.eventHelper = eventHelper;
	}

	public void setInputStream(InputStream is, String in) {
		if (is == null)
			throw new NullPointerException("Input stream is null");
		if (in == null)
			throw new NullPointerException("Input stream name is null");
		inputStream = is;
		inputName = in;
	}

	public CyFileFilter getCyFileFilter() {
		return filter;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new OBOReader(inputName, inputStream, cyNetworkViewFactory, cyNetworkFactory, eventHelper));
	}
}