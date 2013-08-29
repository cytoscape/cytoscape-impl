package org.cytoscape.cpath2.internal;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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

import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;

import org.cytoscape.cpath2.internal.web_service.CytoscapeCPathWebService;
import org.cytoscape.cpath2.internal.CPath2Factory;



import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;


/* Note: despite historically called "cpath2-impl" and having .cpath2. in its package names, 
 * this plugin has nothing to do with Pathway Commons's new cPath2 server software
 * (for which there is now new CyPath2 app available in the Apps Store)! 
 * This one would be called "cpath1-impl" or "cpath1v2-impl" instead, for it 
 * still connects to the cPath-based PathwayCommons service (pathwaycommons.org/pc/, 
 * BioPAX Level2 data, not updated since 11/2011)
 * 
 * TODO remove from cytoscape-impl (core), move to the Apps Store or merge with CyPath2 app.
 */
public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CySwingApplication cySwingApplicationRef = getService(bc,CySwingApplication.class);
		TaskManager taskManagerRef = getService(bc,DialogTaskManager.class);
		OpenBrowser openBrowserRef = getService(bc,OpenBrowser.class);
		CyNetworkManager cyNetworkManagerRef = getService(bc,CyNetworkManager.class);
		CyApplicationManager cyApplicationManagerRef = getService(bc,CyApplicationManager.class);
		CyNetworkViewManager cyNetworkViewManagerRef = getService(bc,CyNetworkViewManager.class);
		CyNetworkReaderManager cyNetworkReaderManagerRef = getService(bc,CyNetworkReaderManager.class);
		CyNetworkNaming cyNetworkNamingRef = getService(bc,CyNetworkNaming.class);
		CyNetworkFactory cyNetworkFactoryRef = getService(bc,CyNetworkFactory.class);
		CyLayoutAlgorithmManager cyLayoutsRef = getService(bc,CyLayoutAlgorithmManager.class);
		UndoSupport undoSupportRef = getService(bc,UndoSupport.class);
		VisualMappingManager visualMappingManagerRef = getService(bc,VisualMappingManager.class);
		VisualStyleFactory visualStyleFactoryRef = getService(bc,VisualStyleFactory.class);
		VisualMappingFunctionFactory discreteMappingFactoryRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=discrete)");
		VisualMappingFunctionFactory passthroughMappingFactoryRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=passthrough)");
		
		CPath2Factory cPath2Factory = new CPath2Factory(cySwingApplicationRef,taskManagerRef, openBrowserRef,cyNetworkManagerRef,cyApplicationManagerRef,cyNetworkViewManagerRef,cyNetworkReaderManagerRef,cyNetworkNamingRef,cyNetworkFactoryRef,cyLayoutsRef,undoSupportRef,visualMappingManagerRef);
		CytoscapeCPathWebService cPathWebService = new CytoscapeCPathWebService(cPath2Factory);
		
		registerAllServices(bc,cPathWebService, new Properties());
	}
}

