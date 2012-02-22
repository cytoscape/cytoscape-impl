package org.cytoscape.welcome.internal;

import java.awt.event.ActionEvent;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.datasource.DataSourceManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.creation.ImportNetworksTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

public class WelcomeScreenAction extends AbstractCyAction {

	private static final long serialVersionUID = 2584201062371825221L;
	
	public static final String DO_NOT_DISPLAY_PROP_NAME = "hideWelcomScreen";

	private static final String MENU_NAME = "Show Welcome Screen...";
	private static final String PARENT_NAME = "Help";

	private final OpenBrowser openBrowser;
	private final RecentlyOpenedTracker fileTracker;
	private final TaskManager guiTaskManager;
	private final ImportNetworksTaskFactory importNetworksTaskFactory;
	private final CyApplicationConfiguration config;
	private final DataSourceManager dsManager;
	
	private final NetworkTaskFactory networkTaskFactory;
	private final TaskFactory openSessionTaskFactory;
	private final TaskFactory importNetworkFileTF;

	private final CySwingApplication app;
	private final CyProperty<Properties> cyProps;
	
	private final BundleContext bc;
	
	private boolean hide = false;

	public WelcomeScreenAction(final BundleContext bc, final CySwingApplication app, 
			OpenBrowser openBrowserServiceRef, RecentlyOpenedTracker fileTracker, final TaskFactory openSessionTaskFactory, TaskManager guiTaskManager,
			final TaskFactory importNetworkFileTF, final ImportNetworksTaskFactory importNetworksTaskFactory, final NetworkTaskFactory networkTaskFactory,
			final CyApplicationConfiguration config, final DataSourceManager dsManager, final CyProperty<Properties> cyProps) {
		super(MENU_NAME);
		setPreferredMenu(PARENT_NAME);

		this.openBrowser = openBrowserServiceRef;
		this.fileTracker = fileTracker;
		this.guiTaskManager = guiTaskManager;
		this.importNetworksTaskFactory = importNetworksTaskFactory;
		this.networkTaskFactory = networkTaskFactory;
		this.config = config;
		this.dsManager = dsManager;
		this.app = app;
		this.cyProps = cyProps;
		this.openSessionTaskFactory = openSessionTaskFactory;
		this.importNetworkFileTF = importNetworkFileTF;
		this.bc = bc;
		
		// Show it if necessary
		startup();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		final WelcomeScreenDialog welcomeScreen = new WelcomeScreenDialog(bc,openBrowser, fileTracker, openSessionTaskFactory, guiTaskManager, config,
				importNetworkFileTF, importNetworksTaskFactory, networkTaskFactory, dsManager, cyProps, hide);
		welcomeScreen.setLocationRelativeTo(app.getJFrame());
		welcomeScreen.setVisible(true);
		this.hide = welcomeScreen.getHideStatus();
		this.cyProps.getProperties().setProperty(DO_NOT_DISPLAY_PROP_NAME, ((Boolean)hide).toString());
	}
	
	public void startup() {
		// Simply displays the dialog after startup.
		final String hideString = this.cyProps.getProperties().getProperty(DO_NOT_DISPLAY_PROP_NAME);
		
		if (hideString == null)
			hide = false;
		else {
			try {
				hide = Boolean.parseBoolean(hideString);
			} catch (Exception ex) {
				hide = false;
			}
		}
		
		if(hide == false)
			actionPerformed(null);
	}
}
