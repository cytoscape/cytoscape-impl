
package org.cytoscape.search.internal;

import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.swing.DialogTaskManager;

import org.cytoscape.search.internal.EnhancedSearchPlugin;
import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CySwingApplication cytoscapeDesktopService = getService(bc,CySwingApplication.class);
		CyApplicationManager cyApplicationManagerRef = getService(bc,CyApplicationManager.class);
		DialogTaskManager guiTaskManagerServiceRef = getService(bc,DialogTaskManager.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class);
		
		EnhancedSearchPlugin enhancedSearchPlugin = new EnhancedSearchPlugin(cytoscapeDesktopService,cyApplicationManagerRef,guiTaskManagerServiceRef,cyEventHelperServiceRef,cyNetworkViewManagerServiceRef);
		
		registerAllServices(bc,enhancedSearchPlugin, new Properties());
	}
}

