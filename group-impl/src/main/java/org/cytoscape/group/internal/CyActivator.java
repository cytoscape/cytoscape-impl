
package org.cytoscape.group.internal;

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyRootNetworkManager cyRootNetworkManagerServiceRef = getService(bc,CyRootNetworkManager.class);
		
		CyGroupManagerImpl cyGroupManager = new CyGroupManagerImpl(cyEventHelperServiceRef);
		CyGroupFactoryImpl cyGroupFactory = new CyGroupFactoryImpl(cyEventHelperServiceRef, 
		                                                           cyGroupManager, 
		                                                           cyServiceRegistrarServiceRef);
		registerService(bc,cyGroupManager,CyGroupManager.class, new Properties());
		registerService(bc,cyGroupFactory,CyGroupFactory.class, new Properties());

/*
		// Move this to a separate module
		SessionEventsListener sessListener = new SessionEventsListener(cyGroupFactory,
				                                                       cyGroupManager,
				                                                       cyNetworkManagerServiceRef,
				                                                       cyRootNetworkManagerServiceRef);
		
		registerService(bc, sessListener, SessionLoadedListener.class, new Properties());
		registerService(bc, sessListener, SessionAboutToBeSavedListener.class, new Properties());
*/
	}
}

