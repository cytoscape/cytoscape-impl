



package org.cytoscape.session.internal;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;

import org.cytoscape.session.internal.CySessionManagerImpl;
import org.cytoscape.session.internal.CyNetworkNamingImpl;

import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.CyNetworkNaming;

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
		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		VisualMappingManager visualMappingManagerServiceRef = getService(bc,VisualMappingManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class);
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc,CyNetworkTableManager.class);
		
		CyNetworkNamingImpl cyNetworkNaming = new CyNetworkNamingImpl(cyNetworkManagerServiceRef);
		CySessionManagerImpl cySessionManager = new CySessionManagerImpl(cyEventHelperServiceRef,cyNetworkManagerServiceRef,cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,visualMappingManagerServiceRef,cyNetworkViewManagerServiceRef);
		
		registerService(bc,cyNetworkNaming,CyNetworkNaming.class, new Properties());
		registerService(bc,cySessionManager,CySessionManager.class, new Properties());

		registerServiceListener(bc,cySessionManager,"addCyProperty","removeCyProperty",CyProperty.class);
	}
}

