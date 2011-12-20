package org.cytoscape.biopax.internal;

import java.io.InputStream;

import org.cytoscape.biopax.internal.action.BioPaxViewTracker;
import org.cytoscape.biopax.internal.util.BioPaxVisualStyleUtil;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class BioPaxReaderTaskFactory implements InputStreamTaskFactory {

	private final CyFileFilter filter;
	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkNaming naming;
	private final BioPaxViewTracker networkTracker;

	private InputStream inputStream;
	private String inputName;
	private VisualMappingManager mappingManager;
	private BioPaxVisualStyleUtil bioPaxVisualStyleUtil;

	public BioPaxReaderTaskFactory(CyFileFilter filter, CyNetworkFactory networkFactory, 
			CyNetworkViewFactory viewFactory, CyNetworkNaming naming, BioPaxViewTracker networkTracker, 
			VisualMappingManager mappingManager, BioPaxVisualStyleUtil bioPaxVisualStyleUtil) {
		this.filter = filter;
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
		this.naming = naming;
		this.networkTracker = networkTracker;
		this.mappingManager = mappingManager;
		this.bioPaxVisualStyleUtil = bioPaxVisualStyleUtil;
		this.inputName = "BioPAX_Network"; //default name fallback
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		BioPaxReaderTask task = new BioPaxReaderTask(
				inputStream, inputName, networkFactory, viewFactory, naming, 
				networkTracker, mappingManager, bioPaxVisualStyleUtil);
		return new TaskIterator(task);
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}

	@Override
	public void setInputStream(InputStream inputStream, String inputName) {
		this.inputStream = inputStream;
		this.inputName = inputName;
	}

}
