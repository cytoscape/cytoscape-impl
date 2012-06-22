package org.cytoscape.webservice.psicquic;

import java.util.Properties;

import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.webservice.psicquic.mapper.MergedNetworkBuilder;
import org.cytoscape.webservice.psicquic.task.ExpandNodeContextMenuFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);

		DialogTaskManager tm = getService(bc, DialogTaskManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyLayoutAlgorithmManager layoutManager = getService(bc, CyLayoutAlgorithmManager.class);

		VisualMappingManager vmm = getService(bc, VisualMappingManager.class);
		CyEventHelper eh = getService(bc, CyEventHelper.class);

		DialogTaskManager taskManager = getService(bc, DialogTaskManager.class);

		CreateNetworkViewTaskFactory createViewTaskFactoryServiceRef = getService(bc,
				CreateNetworkViewTaskFactory.class);
		
		final MergedNetworkBuilder builder = new MergedNetworkBuilder(cyNetworkFactoryServiceRef);

		final PSICQUICWebServiceClient psicquicClient = new PSICQUICWebServiceClient(
				"http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry", "PSICQUIC Universal Client",
				"REST version of PSICQUIC web service client.", cyNetworkFactoryServiceRef, cyNetworkManagerServiceRef,
				tm, createViewTaskFactoryServiceRef, openBrowser, builder);

		registerAllServices(bc, psicquicClient, new Properties());

		final ExpandNodeContextMenuFactory expandNodeContextMenuFactory = new ExpandNodeContextMenuFactory(eh, vmm,
				psicquicClient.getRestClient(), psicquicClient.getRegistryManager(), taskManager, layoutManager, builder);
		final Properties nodeProp = new Properties();
		nodeProp.setProperty("preferredTaskManager", "menu");
		registerService(bc, expandNodeContextMenuFactory, CyNodeViewContextMenuFactory.class, nodeProp);
	}
}
