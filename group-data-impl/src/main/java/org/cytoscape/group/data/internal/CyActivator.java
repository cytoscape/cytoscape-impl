
package org.cytoscape.group.data.internal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.CyGroupSettings;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupAddedListener;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
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

		// Get our Settings object
		CyGroupSettingsImpl cyGroupSettings = new CyGroupSettingsImpl(cyGroupManager);

		// Register our settings menu
    CyGroupSettingsTaskFactory settingsFactory = new CyGroupSettingsTaskFactory(cyGroupSettings);

    Properties settingsProps = new Properties();
    settingsProps.setProperty("id","settingsFactory");
    settingsProps.setProperty("preferredMenu","Edit.Preferences");
    settingsProps.setProperty("title", "Group Preferences...");
    settingsProps.setProperty("menuGravity","1.0");
    settingsProps.setProperty("toolBarGravity","4");
    settingsProps.setProperty("inToolBar","false");
    registerService(bc,settingsFactory,TaskFactory.class, settingsProps);

		// Make the settings available to consumers
		registerService(bc,cyGroupSettings, CyGroupSettings.class, new Properties());
    registerService(bc,cyGroupSettings, GroupAddedListener.class, new Properties());

		GroupDataCollapseHandler gdcHandler =
      new GroupDataCollapseHandler(cyGroupManager, cyGroupSettings);

    registerService(bc,gdcHandler,GroupAboutToCollapseListener.class, new Properties());

	}
}

