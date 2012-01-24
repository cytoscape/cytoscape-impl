
package org.cytoscape.group.internal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.CyGroupFactory;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		
		CyGroupManagerImpl cyGroupManager = new CyGroupManagerImpl(cyEventHelperServiceRef);
		CyGroupFactoryImpl cyGroupFactory = new CyGroupFactoryImpl(cyEventHelperServiceRef, 
		                                                           cyGroupManager, 
		                                                           cyServiceRegistrarServiceRef);
		registerService(bc,cyGroupManager,CyGroupManager.class, new Properties());
		registerService(bc,cyGroupFactory,CyGroupFactory.class, new Properties());
	}
}

