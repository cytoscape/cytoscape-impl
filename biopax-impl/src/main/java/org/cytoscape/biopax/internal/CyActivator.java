package org.cytoscape.biopax.internal;

import org.cytoscape.work.TaskManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkViewFactory;

import org.cytoscape.biopax.internal.BioPaxFilter;
import org.cytoscape.biopax.internal.action.BioPaxViewTrackerImpl;
import org.cytoscape.biopax.internal.BioPaxNetworkViewTaskFactory;
import org.cytoscape.biopax.internal.BioPaxFactory;
import org.cytoscape.biopax.internal.util.BioPaxVisualStyleUtil;
import org.cytoscape.biopax.internal.view.BioPaxContainerImpl;
import org.cytoscape.biopax.internal.BioPaxMapperFactoryImpl;
import org.cytoscape.biopax.internal.view.BioPaxDetailsPanel;
import org.cytoscape.biopax.internal.view.BioPaxCytoPanelComponent;
import org.cytoscape.biopax.internal.action.LaunchExternalBrowser;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.biopax.BioPaxMapperFactory;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.biopax.BioPaxContainer;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.biopax.BioPaxViewTracker;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CySwingApplication cySwingApplicationRef = getService(bc,CySwingApplication.class);
		TaskManager<?,?> taskManagerRef = getService(bc,TaskManager.class);
		OpenBrowser openBrowserRef = getService(bc,OpenBrowser.class);
		CyApplicationManager cyApplicationManagerRef = getService(bc,CyApplicationManager.class);
		CyNetworkViewManager cyNetworkViewManagerRef = getService(bc,CyNetworkViewManager.class);
		CyNetworkNaming cyNetworkNamingRef = getService(bc,CyNetworkNaming.class);
		CyNetworkFactory cyNetworkFactoryRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryRef = getService(bc,CyNetworkViewFactory.class);
		FileUtil fileUtilRef = getService(bc,FileUtil.class);
		StreamUtil streamUtilRef = getService(bc,StreamUtil.class);
		VisualMappingManager visualMappingManagerRef = getService(bc,VisualMappingManager.class);
		VisualStyleFactory visualStyleFactoryRef = getService(bc,VisualStyleFactory.class);
		VisualMappingFunctionFactory discreteMappingFunctionFactoryRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=discrete)");
		VisualMappingFunctionFactory passthroughMappingFunctionFactoryRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=passthrough)");
		
		BioPaxMapperFactory bioPaxMapperFactory = new BioPaxMapperFactoryImpl(cyNetworkFactoryRef);
		BioPaxFilter bioPaxFilter = new BioPaxFilter(streamUtilRef);
		LaunchExternalBrowser launchExternalBrowser = new LaunchExternalBrowser(openBrowserRef);
		BioPaxFactory bioPaxFactory = new BioPaxFactory(cyNetworkViewManagerRef,fileUtilRef,cyApplicationManagerRef,bioPaxFilter,taskManagerRef,launchExternalBrowser,cySwingApplicationRef);
		BioPaxDetailsPanel bioPaxDetailsPanel = new BioPaxDetailsPanel(launchExternalBrowser);
		BioPaxContainerImpl bioPaxContainer = new BioPaxContainerImpl(launchExternalBrowser,cyApplicationManagerRef,cyNetworkViewManagerRef,bioPaxDetailsPanel,cySwingApplicationRef);
		BioPaxViewTrackerImpl bioPaxViewTracker = new BioPaxViewTrackerImpl(bioPaxDetailsPanel,bioPaxContainer,bioPaxMapperFactory,cyNetworkViewManagerRef);
		BioPaxCytoPanelComponent bioPaxCytoPanelComponent = new BioPaxCytoPanelComponent(bioPaxContainer);
		BioPaxVisualStyleUtil bioPaxVisualStyleUtil = new BioPaxVisualStyleUtil(visualStyleFactoryRef,visualMappingManagerRef,discreteMappingFunctionFactoryRef,passthroughMappingFunctionFactoryRef);
		BioPaxNetworkViewTaskFactory bioPaxNetworkViewTaskFactory = new BioPaxNetworkViewTaskFactory(bioPaxFilter,cyNetworkFactoryRef,cyNetworkViewFactoryRef,cyNetworkNamingRef,bioPaxViewTracker,visualMappingManagerRef,bioPaxVisualStyleUtil);
		
		registerService(bc,bioPaxMapperFactory,BioPaxMapperFactory.class, new Properties());
		registerService(bc,bioPaxNetworkViewTaskFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,bioPaxContainer,BioPaxContainer.class, new Properties());
		registerService(bc,bioPaxCytoPanelComponent,CytoPanelComponent.class, new Properties());
		registerService(bc,bioPaxViewTracker,BioPaxViewTracker.class, new Properties());
		registerService(bc,bioPaxViewTracker,RowsSetListener.class, new Properties());
		registerService(bc,bioPaxFactory,BioPaxFactory.class, new Properties());
	}
}

