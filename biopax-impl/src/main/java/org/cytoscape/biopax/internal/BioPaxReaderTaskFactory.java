package org.cytoscape.biopax.internal;

import java.io.InputStream;

import org.cytoscape.biopax.internal.util.BioPaxVisualStyleUtil;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class BioPaxReaderTaskFactory implements InputStreamTaskFactory<Object> {

	private final CyFileFilter filter;
	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkNaming naming;

	private VisualMappingManager mappingManager;
	private BioPaxVisualStyleUtil bioPaxVisualStyleUtil;

	public BioPaxReaderTaskFactory(CyFileFilter filter, CyNetworkFactory networkFactory, 
			CyNetworkViewFactory viewFactory, CyNetworkNaming naming,
			VisualMappingManager mappingManager, BioPaxVisualStyleUtil bioPaxVisualStyleUtil) {
		this.filter = filter;
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
		this.naming = naming;
		this.mappingManager = mappingManager;
		this.bioPaxVisualStyleUtil = bioPaxVisualStyleUtil;
	}
	

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}


	@Override
	public TaskIterator createTaskIterator(Object tunableContext,
			InputStream is, String inputName) {
		if(inputName == null)
			inputName = "BioPAX_Network"; //default name fallback
		
		BioPaxReaderTask task = new BioPaxReaderTask(
				is, inputName, networkFactory, viewFactory, naming, 
				mappingManager, bioPaxVisualStyleUtil);
		return new TaskIterator(task);
	}

	@Override
	public boolean isReady(Object tunableContext, InputStream is,
			String inputName) {
		return true;
	}

	@Override
	public Object createTunableContext() {
		return null;
	}

}
