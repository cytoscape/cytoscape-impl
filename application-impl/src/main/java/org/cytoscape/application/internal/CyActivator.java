



package org.cytoscape.application.internal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.model.CyNetworkManager;

import org.cytoscape.application.internal.CyApplicationManagerImpl;
import org.cytoscape.application.internal.CyApplicationConfigurationImpl;
import org.cytoscape.application.internal.ShutdownHandler;
import org.cytoscape.application.internal.CyVersionImpl;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.property.CyProperty;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class);
		
		CyApplicationManagerImpl cyApplicationManager = new CyApplicationManagerImpl(cyEventHelperServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef);
		ShutdownHandler cytoscapeShutdown = new ShutdownHandler(cyEventHelperServiceRef);
		CyApplicationConfigurationImpl cyApplicationConfiguration = new CyApplicationConfigurationImpl();
		CyProperty cyApplicationCoreProperty = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		CyVersionImpl cytoscapeVersion = new CyVersionImpl(cyApplicationCoreProperty);
		
		registerService(bc,cyApplicationManager,CyApplicationManager.class, new Properties());
		registerService(bc,cyApplicationManager,NetworkAboutToBeDestroyedListener.class, new Properties());
		registerService(bc,cyApplicationManager,NetworkViewAboutToBeDestroyedListener.class, new Properties());
		registerAllServices(bc,cytoscapeShutdown, new Properties());
		registerAllServices(bc,cytoscapeVersion, new Properties());
		registerAllServices(bc,cyApplicationConfiguration, new Properties());

	}
}

