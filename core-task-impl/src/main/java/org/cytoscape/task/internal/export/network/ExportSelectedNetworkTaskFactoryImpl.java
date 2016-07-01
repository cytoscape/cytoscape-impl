package org.cytoscape.task.internal.export.network;

import java.io.File;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.write.ExportNetworkTaskFactory;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.task.write.ExportSelectedNetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class ExportSelectedNetworkTaskFactoryImpl implements ExportSelectedNetworkTaskFactory  {
	
	private final CyApplicationManager applicationManager;
	private final ExportNetworkTaskFactory exportNetworkTaskFactory;
	private final ExportNetworkViewTaskFactory exportNetworkViewTaskFactory;
	
	public ExportSelectedNetworkTaskFactoryImpl(CyApplicationManager applicationManager, 
			ExportNetworkTaskFactory exportNetworkTaskFactory, ExportNetworkViewTaskFactory exportNetworkViewTaskFactory) {
		this.applicationManager = applicationManager;
		this.exportNetworkTaskFactory = exportNetworkTaskFactory;
		this.exportNetworkViewTaskFactory = exportNetworkViewTaskFactory;
	}

	@Override
	public TaskIterator createTaskIterator() {
		CyNetworkView view = applicationManager.getCurrentNetworkView();
		if (view != null )
			return exportNetworkViewTaskFactory.createTaskIterator(view);
		else {
			CyNetwork network = applicationManager.getCurrentNetwork();
			if (network != null)
				return exportNetworkTaskFactory.createTaskIterator(network);
		}
		return null;
	}

	@Override
	public TaskIterator createTaskIterator(File file) {
		CyNetworkView view = applicationManager.getCurrentNetworkView();
		if (view != null )
			return exportNetworkViewTaskFactory.createTaskIterator(view, file);
		else {
			CyNetwork network = applicationManager.getCurrentNetwork();
			if (network != null)
				return exportNetworkTaskFactory.createTaskIterator(network, file);
		}
		return null;
	}

	@Override
	public boolean isReady() {
		return((applicationManager.getCurrentNetworkView() != null) || (applicationManager.getCurrentNetwork() != null));
	}

}
