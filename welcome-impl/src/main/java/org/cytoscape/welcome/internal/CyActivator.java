package org.cytoscape.welcome.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.datasource.DataSourceManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.creation.ImportNetworksTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CySwingApplication cytoscapeDesktop = getService(bc, CySwingApplication.class);
		OpenBrowser openBrowserServiceRef = getService(bc, OpenBrowser.class);
		RecentlyOpenedTracker recentlyOpenedTrackerServiceRef = getService(bc, RecentlyOpenedTracker.class);
		TaskFactory openSessionTaskFactory = getService(bc, TaskFactory.class, "(id=openSessionTaskFactory)");
		SubmenuTaskManager submenuTaskManagerServiceRef = getService(bc, SubmenuTaskManager.class);
		TaskFactory importNetworkFileTF = getService(bc, TaskFactory.class, "(id=loadNetworkFileTaskFactory)");
		ImportNetworksTaskFactory importNetworkTF = getService(bc, ImportNetworksTaskFactory.class,
				"(id=loadNetworkURLTaskFactory)");
		NetworkTaskFactory createNetworkViewTaskFactory = getService(bc, NetworkTaskFactory.class,
				"(id=createNetworkViewTaskFactory)");
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,
				CyApplicationConfiguration.class);
		DataSourceManager dsManagerServiceRef = getService(bc, DataSourceManager.class);
		CyProperty<Properties> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class,
				"(cyPropertyName=cytoscape3.props)");

		// Show Welcome Screen
		final WelcomeScreenAction welcomeScreenAction = new WelcomeScreenAction(bc, cytoscapeDesktop,
				openBrowserServiceRef, recentlyOpenedTrackerServiceRef, openSessionTaskFactory,
				submenuTaskManagerServiceRef, importNetworkFileTF, importNetworkTF, createNetworkViewTaskFactory,
				cyApplicationConfigurationServiceRef, dsManagerServiceRef, cytoscapePropertiesServiceRef);
		
		registerAllServices(bc, welcomeScreenAction, new Properties());
	}
}
