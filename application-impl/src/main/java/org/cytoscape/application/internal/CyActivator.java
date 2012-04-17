
package org.cytoscape.application.internal;

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class);
		
		CyApplicationManagerImpl cyApplicationManager = new CyApplicationManagerImpl(cyEventHelperServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef);
		Bundle rootBundle = bc.getBundle(0);
		ShutdownHandler cytoscapeShutdown = new ShutdownHandler(cyEventHelperServiceRef, rootBundle);
		CyApplicationConfigurationImpl cyApplicationConfiguration = new CyApplicationConfigurationImpl();
		CyProperty<Properties> cyApplicationCoreProperty = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		CyVersionImpl cytoscapeVersion = new CyVersionImpl(cyApplicationCoreProperty);
		
		registerAllServices(bc,cyApplicationManager, new Properties());
		registerAllServices(bc,cytoscapeShutdown, new Properties());
		registerAllServices(bc,cytoscapeVersion, new Properties());
		registerAllServices(bc,cyApplicationConfiguration, new Properties());
	}
}
