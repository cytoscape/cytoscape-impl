package org.cytoscape.welcome.internal;

import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.analyze.AnalyzeNetworkCollectionTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.welcome.internal.panel.CreateNewNetworkPanel;
import org.cytoscape.welcome.internal.panel.NewsAndLinkPanel;
import org.cytoscape.welcome.internal.panel.OpenPanel;
import org.cytoscape.welcome.internal.panel.StatusPanel;
import org.cytoscape.welcome.internal.task.GenerateCustomStyleTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		CyVersion cyVersion = getService(bc, CyVersion.class);
		final ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory = getService(bc,
				ApplyPreferredLayoutTaskFactory.class);
		BendFactory bendFactory = getService(bc, BendFactory.class);
		VisualMappingManager vmm = getService(bc, VisualMappingManager.class);
		VisualStyleFactory vsFactoryServiceRef = getService(bc, VisualStyleFactory.class);
		VisualMappingFunctionFactory continupousMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=continuous)");
		VisualMappingFunctionFactory passthroughMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=passthrough)");
		VisualMappingFunctionFactory discreteMappingFactoryRef = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=discrete)");
		VisualStyleBuilder vsBuilder = new VisualStyleBuilder(vsFactoryServiceRef, continupousMappingFactoryRef,
				discreteMappingFactoryRef, passthroughMappingFactoryRef, bendFactory);

		AnalyzeNetworkCollectionTaskFactory analyzeNetworkCollectionTaskFactory = getService(bc,
				AnalyzeNetworkCollectionTaskFactory.class);
		CySwingApplication cytoscapeDesktop = getService(bc, CySwingApplication.class);
		OpenBrowser openBrowserServiceRef = getService(bc, OpenBrowser.class);
		RecentlyOpenedTracker recentlyOpenedTrackerServiceRef = getService(bc, RecentlyOpenedTracker.class);
		OpenSessionTaskFactory openSessionTaskFactory = getService(bc, OpenSessionTaskFactory.class);
		DialogTaskManager dialogTaskManagerServiceRef = getService(bc, DialogTaskManager.class);
		TaskFactory importNetworkFileTF = getService(bc, TaskFactory.class, "(id=loadNetworkFileTaskFactory)");
		LoadNetworkURLTaskFactory importNetworkTF = getService(bc, LoadNetworkURLTaskFactory.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,
				CyApplicationConfiguration.class);
		DataSourceManager dsManagerServiceRef = getService(bc, DataSourceManager.class);
		@SuppressWarnings("unchecked")
		CyProperty<Properties> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class,
				"(cyPropertyName=cytoscape3.props)");

		// Build Child Panels
		final OpenPanel openPanel = new OpenPanel(recentlyOpenedTrackerServiceRef, dialogTaskManagerServiceRef,
				openSessionTaskFactory);

		final CreateNewNetworkPanel createNewNetworkPanel = new CreateNewNetworkPanel(bc, dialogTaskManagerServiceRef,
				importNetworkFileTF, importNetworkTF, dsManagerServiceRef);
		registerAllServices(bc, createNewNetworkPanel, new Properties());

		// TODO: implement contents
		final StatusPanel statusPanel = new StatusPanel(cyVersion);
		final NewsAndLinkPanel helpPanel = new NewsAndLinkPanel(statusPanel, openBrowserServiceRef, cytoscapePropertiesServiceRef);

		// Show Welcome Screen
		final WelcomeScreenAction welcomeScreenAction = new WelcomeScreenAction(createNewNetworkPanel, openPanel,
				helpPanel, cytoscapePropertiesServiceRef, cytoscapeDesktop);
		registerAllServices(bc, welcomeScreenAction, new Properties());

		// Export preset tasks
		final GenerateCustomStyleTaskFactory generateCustomStyleTaskFactory = new GenerateCustomStyleTaskFactory(
				analyzeNetworkCollectionTaskFactory, applicationManager, vsBuilder, vmm);
		Properties generateCustomStyleTaskFactoryProps = new Properties();
		generateCustomStyleTaskFactoryProps.setProperty(PREFERRED_MENU, "Tools.Workflow");
		generateCustomStyleTaskFactoryProps.setProperty(MENU_GRAVITY, "20.0");
		generateCustomStyleTaskFactoryProps.setProperty(TITLE,
				"Analyze selected networks and create custom Visual Styles");
		generateCustomStyleTaskFactoryProps.setProperty(IN_TOOL_BAR, "false");
		registerAllServices(bc, generateCustomStyleTaskFactory, generateCustomStyleTaskFactoryProps);

		// This is a preset task, so register it first.
		final Map<String, String> propMap = new HashMap<String, String>();
		propMap.put(CreateNewNetworkPanel.WORKFLOW_ID, "generateCustomStyleTaskFactory");
		propMap.put(CreateNewNetworkPanel.WORKFLOW_NAME, "Analyze network and create custom Visual Style");
		propMap.put(CreateNewNetworkPanel.WORKFLOW_DESCRIPTION,
				"Analyze current/selected networks and create custom Visual Style for each network.");
		createNewNetworkPanel.addTaskFactory(generateCustomStyleTaskFactory, propMap);

		// Define listener
		registerServiceListener(bc, createNewNetworkPanel, "addTaskFactory", "removeTaskFactory", TaskFactory.class);
	}
}
