package org.cytoscape.sbml.internal;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class SBMLNetworkViewTaskFactory extends AbstractInputStreamTaskFactory {

	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory viewFactory;
	
	public SBMLNetworkViewTaskFactory(CyFileFilter filter, CyNetworkFactory networkFactory, CyNetworkViewFactory viewFactory) {
		super(filter);
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
	}
	
	public TaskIterator createTaskIterator(InputStream stream, String inputName) {
		return new TaskIterator(new SBMLNetworkViewReader(stream, networkFactory, viewFactory));
	}

}
