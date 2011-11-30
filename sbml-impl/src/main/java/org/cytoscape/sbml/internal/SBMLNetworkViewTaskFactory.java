package org.cytoscape.sbml.internal;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class SBMLNetworkViewTaskFactory implements InputStreamTaskFactory {

	private final CyFileFilter filter;
	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory viewFactory;
	
	private InputStream stream;
	private String inputName;

	public SBMLNetworkViewTaskFactory(CyFileFilter filter, CyNetworkFactory networkFactory, CyNetworkViewFactory viewFactory) {
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
		this.filter = filter;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SBMLNetworkViewReader(stream, networkFactory, viewFactory));
	}

	public CyFileFilter getCyFileFilter() {
		return filter;
	}

	public void setInputStream(InputStream is, String in) {
		stream = is;
		inputName = in;
	}

}
