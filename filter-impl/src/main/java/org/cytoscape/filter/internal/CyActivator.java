
package org.cytoscape.filter.internal;

import org.cytoscape.work.TaskManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;

import org.cytoscape.filter.internal.FilterPanelSelectedListener;
import org.cytoscape.filter.internal.filters.view.FilterMainPanel;
import org.cytoscape.filter.internal.filters.FilterPlugin;
import org.cytoscape.filter.internal.gui.FilterCytoPanelComponent;
import org.cytoscape.filter.internal.quickfind.app.QuickFindApp;
import org.cytoscape.filter.internal.filters.FilterMenuItemAction;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.application.swing.CyAction;

import org.osgi.framework.BundleContext;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.events.SessionLoadedListener;

import java.util.Properties;
import org.cytoscape.filter.internal.read.filter.FilterReader;
import org.cytoscape.filter.internal.ServicesUtil;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


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

		//
		ServicesUtil.cySwingApplicationServiceRef = cySwingApplicationServiceRef;
		ServicesUtil.cyApplicationManagerServiceRef = cyApplicationManagerServiceRef;
		ServicesUtil.cyNetworkViewManagerServiceRef = cyNetworkViewManagerServiceRef;
		ServicesUtil.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		ServicesUtil.cyServiceRegistrarServiceRef = cyServiceRegistrarServiceRef;
		ServicesUtil.cyEventHelperServiceRef = cyEventHelperServiceRef;
		ServicesUtil.taskManagerServiceRef = taskManagerServiceRef;
		//
				
		FilterReader filterReader = new FilterReader("props.filters");
		ServicesUtil.filterReader = filterReader;
		
		FilterPlugin filterPlugin = new FilterPlugin();
		QuickFindApp quickFindApp = new QuickFindApp(cyApplicationManagerServiceRef,cyNetworkViewManagerServiceRef,cySwingApplicationServiceRef,cyNetworkManagerServiceRef);
		FilterMainPanel filterMainPanel = new FilterMainPanel(cyApplicationManagerServiceRef,filterPlugin,cyNetworkManagerServiceRef,cyServiceRegistrarServiceRef,cyEventHelperServiceRef,taskManagerServiceRef);
		FilterCytoPanelComponent filterCytoPanelComponent = new FilterCytoPanelComponent(filterMainPanel);
		FilterPanelSelectedListener filterPanelSelectedListener = new FilterPanelSelectedListener(filterMainPanel);
		FilterMenuItemAction filterAction = new FilterMenuItemAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef,filterMainPanel);
		//FiltersProperty filtersProps = new FiltersProperty(cyApplicationConfigurationServiceRef);
				
		registerService(bc,filterCytoPanelComponent,CytoPanelComponent.class, new Properties());
		registerAllServices(bc,filterMainPanel, new Properties());
		registerService(bc,filterPanelSelectedListener,CytoPanelComponentSelectedListener.class, new Properties());
		registerService(bc,filterAction,CyAction.class, new Properties());
		registerAllServices(bc,quickFindApp, new Properties());
		registerAllServices(bc,filterPlugin, new Properties());
		
		//registerAllServices(bc,filtersProps, new Properties());
		Properties filterReaderProps = new Properties();
		filterReaderProps.setProperty("cyPropertyName","filters");
		filterReaderProps.setProperty("serviceType","property");

		registerAllServices(bc,filterReader, filterReaderProps);
		
	}
}

