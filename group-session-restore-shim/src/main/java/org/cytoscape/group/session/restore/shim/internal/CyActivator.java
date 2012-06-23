package org.cytoscape.group.session.restore.shim.internal;

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;

import org.osgi.framework.BundleContext;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

import org.cytoscape.service.util.AbstractCyActivator;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start (BundleContext bc) {
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyGroupManager cyGroupManager = getService(bc,CyGroupManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyRootNetworkManager cyRootNetworkManagerServiceRef = getService(bc,CyRootNetworkManager.class);
		CyGroupFactory cyGroupFactoryRef = getService(bc,CyGroupFactory.class);
		SessionEventsListener sessionListener= new SessionEventsListener(cyGroupFactoryRef, cyGroupManager, 
		                                                                 cyNetworkManagerServiceRef, cyRootNetworkManagerServiceRef);

		registerService(bc, sessionListener, SessionLoadedListener.class, new Properties());
		registerService(bc, sessionListener, SessionAboutToBeSavedListener.class, new Properties());
	}
}
