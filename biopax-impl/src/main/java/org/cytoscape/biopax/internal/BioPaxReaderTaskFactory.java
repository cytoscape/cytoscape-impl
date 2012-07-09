package org.cytoscape.biopax.internal;

import java.io.InputStream;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class BioPaxReaderTaskFactory extends AbstractInputStreamTaskFactory {

	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkNaming naming;
	private final CyGroupFactory cyGroupFactory;

	public BioPaxReaderTaskFactory(CyFileFilter filter, CyNetworkFactory networkFactory, 
			CyNetworkViewFactory viewFactory, CyNetworkNaming naming, CyGroupFactory cyGroupFactory)
	{
		super(filter);
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
		this.naming = naming;
		this.cyGroupFactory = cyGroupFactory;
	}
	

	@Override
	public TaskIterator createTaskIterator(InputStream is, String inputName) {
		if(inputName == null)
			inputName = "BioPAX_Network"; //default name fallback
		
		return new TaskIterator(
			new BioPaxReaderTask(is, inputName, networkFactory, viewFactory, naming, cyGroupFactory)
			);
	}

}
