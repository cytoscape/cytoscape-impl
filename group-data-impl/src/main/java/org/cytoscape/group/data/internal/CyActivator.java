
package org.cytoscape.group.data.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupAddedListener;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.work.TaskFactory;

import org.osgi.framework.BundleContext;
import java.util.Properties;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		// Get the generally useful handlers
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyGroupManager cyGroupManager = getService(bc,CyGroupManager.class);
		CyNetworkManager cyNetworkManager = getService(bc,CyNetworkManager.class);
		CyApplicationManager cyApplicationManager = getService(bc,CyApplicationManager.class);

		// Create the aggregation manager
		CyGroupAggregationManager cyAggMgr = 
			new CyGroupAggregationManagerImpl(cyGroupManager);

		// Get our Settings object
		CyGroupSettingsImpl cyGroupSettings = 
			new CyGroupSettingsImpl(cyGroupManager, cyAggMgr, cyApplicationManager);

		// Register our settings menu
    CyGroupSettingsTaskFactory settingsFactory = 
			new CyGroupSettingsTaskFactory(cyAggMgr, 
			                               cyApplicationManager, 
			                               cyGroupSettings);

    Properties settingsProps = new Properties();
    settingsProps.setProperty("id","settingsFactory");
    settingsProps.setProperty("preferredMenu","Edit.Preferences");
    settingsProps.setProperty("title", "Group Preferences...");
    settingsProps.setProperty("menuGravity","4.0");
    settingsProps.setProperty("toolBarGravity","4");
    settingsProps.setProperty("inToolBar","false");
    registerService(bc,settingsFactory,TaskFactory.class, settingsProps);

		// Now register our node-specific settings menu
    CyGroupNodeSettingsTaskFactory nodeSettingsFactory = 
			new CyGroupNodeSettingsTaskFactory(cyGroupManager,
			                                   cyAggMgr, 
			                                   cyApplicationManager, 
			                                   cyGroupSettings);
    settingsProps = new Properties();
    settingsProps.setProperty("id","groupNodeSettingsFactory");
    settingsProps.setProperty("preferredMenu","Preferences");
    settingsProps.setProperty("title", "Group Preferences...");
		settingsProps.setProperty("preferredAction", "NEW");
		settingsProps.setProperty("command", "group-node-settings");
		settingsProps.setProperty("commandNamespace", "network-view");
    settingsProps.setProperty("menuGravity","4.0");
    registerService(bc,nodeSettingsFactory,
		                NodeViewTaskFactory.class, settingsProps);

		// Make the settings available to consumers
    registerService(bc,cyGroupSettings, 
		                GroupAddedListener.class, new Properties());
    registerService(bc,cyGroupSettings, 
		                CyGroupSettingsImpl.class, new Properties());

		GroupDataCollapseHandler gdcHandler =
      new GroupDataCollapseHandler(cyGroupManager, cyGroupSettings);

    registerService(bc,gdcHandler,
		                GroupAboutToCollapseListener.class, new Properties());
	}
}

