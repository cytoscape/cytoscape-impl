package org.cytoscape.session.internal;

/*
 * #%L
 * Cytoscape Session Impl (session-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
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
		RenderingEngineManager renderingEngineManagerServiceRef = getService(bc, RenderingEngineManager.class);
		CyGroupManager cyGroupManagerServiceRef = getService(bc, CyGroupManager.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc, CyServiceRegistrar.class);
		UndoSupport undo = getService(bc, UndoSupport.class);
		
		CyNetworkNamingImpl cyNetworkNaming = new CyNetworkNamingImpl(cyNetworkManagerServiceRef);
		CySessionManagerImpl cySessionManager = new CySessionManagerImpl(cyEventHelperServiceRef,
				cyApplicationManagerServiceRef, cyNetworkManagerServiceRef, cyTableManagerServiceRef,
				cyNetworkTableManagerServiceRef, visualMappingManagerServiceRef, cyNetworkViewManagerServiceRef,
				cyRootNetworkManagerServiceRef, renderingEngineManagerServiceRef, cyGroupManagerServiceRef,
				cyServiceRegistrarServiceRef, undo);

		registerService(bc, cyNetworkNaming, CyNetworkNaming.class, new Properties());
		registerAllServices(bc, cySessionManager, new Properties());
		registerServiceListener(bc, cySessionManager, "addCyProperty", "removeCyProperty", CyProperty.class);
	}
}
