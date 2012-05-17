package org.cytoscape.session.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) {
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyTableManager cyTableManagerServiceRef = getService(bc, CyTableManager.class);
		VisualMappingManager visualMappingManagerServiceRef = getService(bc, VisualMappingManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc, CyNetworkTableManager.class);
		CyRootNetworkManager cyRootNetworkManagerServiceRef = getService(bc, CyRootNetworkManager.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc, CyServiceRegistrar.class);
		UndoSupport undo = getService(bc, UndoSupport.class);
		
		CyNetworkNamingImpl cyNetworkNaming = new CyNetworkNamingImpl(cyNetworkManagerServiceRef);
		CySessionManagerImpl cySessionManager = new CySessionManagerImpl(cyEventHelperServiceRef,
				cyApplicationManagerServiceRef, cyNetworkManagerServiceRef, cyTableManagerServiceRef,
				cyNetworkTableManagerServiceRef, visualMappingManagerServiceRef, cyNetworkViewManagerServiceRef,
				cyRootNetworkManagerServiceRef, cyServiceRegistrarServiceRef, undo);

		registerService(bc, cyNetworkNaming, CyNetworkNaming.class, new Properties());
		registerAllServices(bc, cySessionManager, new Properties());
		registerServiceListener(bc, cySessionManager, "addCyProperty", "removeCyProperty", CyProperty.class);
	}
}
