package org.cytoscape.psi_mi.internal.plugin;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.psi_mi.internal.plugin.PsiMiCyFileFilter.PSIMIVersion;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class PsiMiNetworkViewTaskFactory extends AbstractInputStreamTaskFactory {	
	private final CyNetworkViewFactory networkViewFactory;
	private final CyNetworkFactory networkFactory;
	private final CyLayoutAlgorithmManager layouts;
	private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	private final PSIMIVersion version;

	public PsiMiNetworkViewTaskFactory(final PSIMIVersion version, CyFileFilter filter, CyNetworkFactory networkFactory, 
			CyNetworkViewFactory networkViewFactory, CyLayoutAlgorithmManager layouts,
			final CyNetworkManager cyNetworkManager, CyRootNetworkManager cyRootNetworkManager) {
		super(filter);
		this.networkFactory = networkFactory;
		this.networkViewFactory = networkViewFactory;
		this.layouts = layouts;
		this.version = version;
		this.cyNetworkManager= cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
		
	}
	
	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		// Usually 3 tasks: load, visualize, and layout.
		
		if(version == PSIMIVersion.PSIMI25)
			return new TaskIterator(3, new PSIMI25XMLNetworkViewReader(inputStream, networkFactory, networkViewFactory, layouts, cyNetworkManager, cyRootNetworkManager));
		else
			return new TaskIterator(3, new PSIMI10XMLNetworkViewReader(inputStream, networkFactory, networkViewFactory, layouts, cyNetworkManager, cyRootNetworkManager));
	}

}
