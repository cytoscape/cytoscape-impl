
package org.cytoscape.view.model.internal;

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);		
		CyNetworkViewManagerImpl cyNetworkViewManager = new CyNetworkViewManagerImpl(cyEventHelperServiceRef);
		registerAllServices(bc,cyNetworkViewManager, new Properties());
	}
}

