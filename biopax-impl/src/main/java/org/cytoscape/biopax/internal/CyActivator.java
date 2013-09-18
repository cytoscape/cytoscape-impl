package org.cytoscape.biopax.internal;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
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

//import org.biopax.paxtools.io.SimpleIOHandler;
//import org.biopax.paxtools.model.Model;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;

import org.cytoscape.biopax.internal.BioPaxFilter;
import org.cytoscape.biopax.internal.BioPaxReader;
import org.cytoscape.biopax.internal.util.VisualStyleUtil;

import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.io.read.CyNetworkReaderManager;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CySwingApplication cySwingApplication = getService(bc,CySwingApplication.class);
		OpenBrowser openBrowser = getService(bc,OpenBrowser.class);
		CyApplicationManager cyApplicationManager = getService(bc,CyApplicationManager.class);
		CyNetworkViewManager cyNetworkViewManager = getService(bc,CyNetworkViewManager.class);
		CyNetworkManager cyNetworkManager = getService(bc,CyNetworkManager.class);
		CyNetworkNaming cyNetworkNaming = getService(bc,CyNetworkNaming.class);
		CyNetworkFactory cyNetworkFactory = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactory = getService(bc,CyNetworkViewFactory.class);
		StreamUtil streamUtil = getService(bc,StreamUtil.class);
		VisualMappingManager visualMappingManager = getService(bc,VisualMappingManager.class);
		VisualStyleFactory visualStyleFactory = getService(bc,VisualStyleFactory.class);
		VisualMappingFunctionFactory discreteMappingFunctionFactory = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=discrete)");
		VisualMappingFunctionFactory passthroughMappingFunctionFactory = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=passthrough)");
		CyLayoutAlgorithmManager cyLayoutAlgorithmManager = getService(bc,CyLayoutAlgorithmManager.class);	
		TaskManager taskManager = getService(bc, DialogTaskManager.class);
		CyProperty<Properties> cyProperties = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		CyRootNetworkManager cyRootNetworkManager = getService(bc,CyRootNetworkManager.class);
		CyNetworkReaderManager cyNetworkReaderManager = getService(bc,CyNetworkReaderManager.class);
		UndoSupport undoSupport = getService(bc,UndoSupport.class);
		CyNetworkViewFactory networkViewFactory = getService(bc, CyNetworkViewFactory.class);
		
		
		// keep all the service references in one place -
		final CyServices cyServices = new CyServices(cySwingApplication, taskManager, openBrowser, 
				cyNetworkManager, cyApplicationManager, cyNetworkViewManager, cyNetworkReaderManager, 
				cyNetworkNaming, cyNetworkFactory, cyLayoutAlgorithmManager, undoSupport, visualMappingManager, 
				cyProperties, networkViewFactory, cyRootNetworkManager);
				
		BioPaxFilter bioPaxFilter = new BioPaxFilter(streamUtil);
				
		VisualStyleUtil visualStyleUtil = new VisualStyleUtil(visualStyleFactory,
				visualMappingManager, discreteMappingFunctionFactory, passthroughMappingFunctionFactory);
		visualStyleUtil.getBioPaxVisualStyle(); //initialize
		visualStyleUtil.getBinarySifVisualStyle(); //initialize
			
		// create the biopax reader object
		BioPaxReader biopaxReaderFactory = new BioPaxReader(bioPaxFilter, cyServices, visualStyleUtil);		
		// register/export osgi services
		Properties props = new Properties();
		props.setProperty("readerDescription","BioPAX reader");
		props.setProperty("readerId","biopaxNetworkReader");
		registerAllServices(bc, biopaxReaderFactory, props);		
	}
}

