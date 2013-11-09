package org.cytoscape.webservice.psicquic;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
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

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.NODE_APPS_MENU;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.webservice.psicquic.mapper.CyNetworkBuilder;
import org.cytoscape.webservice.psicquic.task.ExpandNodeContextMenuFactory;
import org.cytoscape.webservice.psicquic.ui.PSIMITagManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	
	private static final String CLIENT_DISCRIPTION = "<strong>Universal Interaction Database Client</strong>" +
			"<p>This is a web service client for <a href=\"http://code.google.com/p/psicquic/\">PSICQUIC</a>-compliant databases.</p>" +
			"<ul><li><a href=\"http://code.google.com/p/psicquic/wiki/MiqlReference\">Query language (MIQL) Syntax</a></li>" +
			"<li><a href=\"http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry?action=STATUS\">List of Supported Databases</a></li></ul>";
	
	
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		final CyProperty<Properties> cyPropertyServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		final CyAction networkMergeActionServiceRef = getService(bc,CyAction.class,"(id=networkMergeAction)");
		
		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);

		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		
		DialogTaskManager tm = getService(bc, DialogTaskManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyLayoutAlgorithmManager layoutManager = getService(bc, CyLayoutAlgorithmManager.class);

		VisualStyleFactory vsFactoryServiceRef = getService(bc, VisualStyleFactory.class);
		VisualMappingFunctionFactory passthroughMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=passthrough)");
		VisualMappingFunctionFactory discreteMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=discrete)");

		VisualMappingManager vmm = getService(bc, VisualMappingManager.class);
		CyEventHelper eh = getService(bc, CyEventHelper.class);

		CreateNetworkViewTaskFactory createViewTaskFactoryServiceRef = getService(bc,
				CreateNetworkViewTaskFactory.class);

		final PSIMITagManager tagManager = new PSIMITagManager();
		
		PSIMI25VisualStyleBuilder vsBuilder = new PSIMI25VisualStyleBuilder(vsFactoryServiceRef,
				discreteMappingFactoryRef, passthroughMappingFactoryRef);

		final CyNetworkBuilder builder = new CyNetworkBuilder(cyNetworkFactoryServiceRef);

		final PSICQUICWebServiceClient psicquicClient = new PSICQUICWebServiceClient(
				"http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry", "Interaction Database Universal Client",
				CLIENT_DISCRIPTION, cyNetworkFactoryServiceRef, cyNetworkManagerServiceRef,
				tm, createViewTaskFactoryServiceRef, openBrowser, builder, vsBuilder, vmm, tagManager, cyPropertyServiceRef, registrar, networkMergeActionServiceRef);

		registerAllServices(bc, psicquicClient, new Properties());

		final ExpandNodeContextMenuFactory expandNodeContextMenuFactory = new ExpandNodeContextMenuFactory(eh, vmm,
				psicquicClient.getRestClient(), psicquicClient.getRegistryManager(), layoutManager, builder);
		final Properties nodeProp = new Properties();
		nodeProp.setProperty("preferredTaskManager", "menu");
		nodeProp.setProperty(PREFERRED_MENU, NODE_APPS_MENU);
		nodeProp.setProperty(MENU_GRAVITY, "10.0");
		nodeProp.setProperty(TITLE, "Extend Network by public interaction database...");
		registerService(bc, expandNodeContextMenuFactory, NodeViewTaskFactory.class, nodeProp);
	}
}
