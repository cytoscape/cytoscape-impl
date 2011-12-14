package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.JDialog;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.datasource.DataSourceManager;
import org.cytoscape.internal.actions.welcomescreen.WelcomeScreenDialog;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.creation.ImportNetworksTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;

public class WelcomeScreenAction extends AbstractCyAction {

	private static final long serialVersionUID = 2584201062371825221L;

	private static final String MENU_NAME = "Welcome Screen...";
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

	public WelcomeScreenAction(final CySwingApplication app, 
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

	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		final JDialog welcomeScreen = new WelcomeScreenDialog(openBrowser, fileTracker, openSessionTaskFactory, guiTaskManager, config,
				importNetworkFileTF, importNetworksTaskFactory, networkTaskFactory, dsManager, cyProps);
		welcomeScreen.setLocationRelativeTo(app.getJFrame());
		welcomeScreen.setVisible(true);
	}

}
