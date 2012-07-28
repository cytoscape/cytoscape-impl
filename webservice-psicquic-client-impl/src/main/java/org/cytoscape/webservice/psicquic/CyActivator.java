package org.cytoscape.webservice.psicquic;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.NODE_APPS_MENU;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
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

		VisualStyleFactory vsFactoryServiceRef = getService(bc, VisualStyleFactory.class);
		VisualMappingFunctionFactory passthroughMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=passthrough)");
		VisualMappingFunctionFactory discreteMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=discrete)");

		VisualMappingManager vmm = getService(bc, VisualMappingManager.class);
		CyEventHelper eh = getService(bc, CyEventHelper.class);

		CreateNetworkViewTaskFactory createViewTaskFactoryServiceRef = getService(bc,
				CreateNetworkViewTaskFactory.class);

		PSIMI25VisualStyleBuilder vsBuilder = new PSIMI25VisualStyleBuilder(vsFactoryServiceRef,
				discreteMappingFactoryRef, passthroughMappingFactoryRef);

		final MergedNetworkBuilder builder = new MergedNetworkBuilder(cyNetworkFactoryServiceRef);

		final PSICQUICWebServiceClient psicquicClient = new PSICQUICWebServiceClient(
				"http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry", "PSICQUIC Universal Client",
				"REST version of PSICQUIC web service client.", cyNetworkFactoryServiceRef, cyNetworkManagerServiceRef,
				tm, createViewTaskFactoryServiceRef, openBrowser, builder, vsBuilder, vmm);

		registerAllServices(bc, psicquicClient, new Properties());

		final ExpandNodeContextMenuFactory expandNodeContextMenuFactory = new ExpandNodeContextMenuFactory(eh, vmm,
				psicquicClient.getRestClient(), psicquicClient.getRegistryManager(), layoutManager, builder);
		final Properties nodeProp = new Properties();
		nodeProp.setProperty("preferredTaskManager", "menu");
		nodeProp.setProperty(PREFERRED_MENU, NODE_APPS_MENU);
		nodeProp.setProperty(MENU_GRAVITY, "10.0");
		nodeProp.setProperty(TITLE, "Extend Network by PSICQUIC...");
		registerService(bc, expandNodeContextMenuFactory, NodeViewTaskFactory.class, nodeProp);
	}
}
