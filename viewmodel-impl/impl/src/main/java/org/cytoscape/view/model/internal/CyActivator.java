
package org.cytoscape.view.model.internal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.cytoscape.view.model.internal.CyNetworkViewManagerImpl;

import org.cytoscape.view.model.CyNetworkViewFactory;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyServiceRegistrar cyServiceRegistrarRef = getService(bc,CyServiceRegistrar.class);
		
		CyNetworkViewManagerImpl cyNetworkViewManager = new CyNetworkViewManagerImpl(cyEventHelperServiceRef);
		
		registerAllServices(bc,cyNetworkViewManager, new Properties());
	}
}

