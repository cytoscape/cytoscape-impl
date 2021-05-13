package org.cytoscape.filter.internal;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.filter.internal.filters.FilterApp;
import org.cytoscape.filter.internal.filters.FilterMenuItemAction;
import org.cytoscape.filter.internal.filters.model.FilterModelLocator;
import org.cytoscape.filter.internal.filters.util.ServicesUtil;
import org.cytoscape.filter.internal.filters.view.FilterMainPanel;
import org.cytoscape.filter.internal.gui.FilterCytoPanelComponent;
import org.cytoscape.filter.internal.quickfind.app.QuickFindApp;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.filter.internal.quickfind.util.QuickFindImpl;
import org.cytoscape.filter.internal.read.filter.FilterReader;
import org.cytoscape.filter.internal.write.filter.FilterWriter;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	
	public void start(BundleContext bc) {

		CySwingApplication cySwingApplicationServiceRef = getService(bc,CySwingApplication.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		TaskManager taskManagerServiceRef = getService(bc,TaskManager.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,CyApplicationConfiguration.class);
		CyVersion cytoscapeVersionService = getService(bc,CyVersion.class);
		
		// Singleton of QuickFind
		final QuickFind quickFind = new QuickFindImpl();

		ServicesUtil.cySwingApplicationServiceRef = cySwingApplicationServiceRef;
		ServicesUtil.cyApplicationManagerServiceRef = cyApplicationManagerServiceRef;
		ServicesUtil.cyNetworkViewManagerServiceRef = cyNetworkViewManagerServiceRef;
		ServicesUtil.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		ServicesUtil.cyServiceRegistrarServiceRef = cyServiceRegistrarServiceRef;
		ServicesUtil.cyEventHelperServiceRef = cyEventHelperServiceRef;
		ServicesUtil.taskManagerServiceRef = taskManagerServiceRef;
		ServicesUtil.cytoscapeVersionService = cytoscapeVersionService;
		ServicesUtil.cyApplicationConfigurationServiceRef = cyApplicationConfigurationServiceRef;
		
		final FilterReader filterReader = new FilterReader(quickFind);
		final FilterWriter filterWriter = new FilterWriter();
		FilterModelLocator filtersModelLocator = new FilterModelLocator();
		
		FilterApp filterApp = new FilterApp(filterReader, filterWriter, filtersModelLocator);
		QuickFindApp quickFindApp = new QuickFindApp(quickFind, cyApplicationManagerServiceRef, cyNetworkManagerServiceRef);
		
		FilterMainPanel filterMainPanel = new FilterMainPanel(quickFind, filtersModelLocator,cyApplicationManagerServiceRef,cyNetworkManagerServiceRef,cyEventHelperServiceRef,taskManagerServiceRef);
		FilterCytoPanelComponent filterCytoPanelComponent = new FilterCytoPanelComponent(filterMainPanel);
		FilterMenuItemAction filterAction = new FilterMenuItemAction(cySwingApplicationServiceRef,filterMainPanel);
				
		registerAllServices(bc,filterCytoPanelComponent, new Properties());
		registerAllServices(bc,filterMainPanel, new Properties());
		registerService(bc,filterAction,CyAction.class, new Properties());
		registerAllServices(bc,quickFindApp, new Properties());
		registerAllServices(bc,filterApp, new Properties());
	}
}

