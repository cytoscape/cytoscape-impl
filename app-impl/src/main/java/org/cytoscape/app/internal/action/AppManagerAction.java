package org.cytoscape.app.internal.action;

import java.awt.event.ActionEvent;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.net.server.LocalHttpServer;
import org.cytoscape.app.internal.ui.AppManagerDialog;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

public class AppManagerAction extends AbstractCyAction {

	/** Long serial version identifier required by the Serializable class */
	private static final long serialVersionUID = -9145570324785249730L;
	
	/**
	 * A reference to the main Cytoscape window used to position the App Manager dialog.
	 */
	private CySwingApplication swingApplication;
	
	/**
	 * A reference to the {@link AppManager} service.
	 */
	private AppManager appManager;
	
	/**
	 * A reference to the {@link FileUtil} OSGi service used for displaying a filechooser dialog
	 */
	private FileUtil fileUtil;
	
	/**
	 * A reference to the {@link TaskManager} service used to execute Cytoscape tasks
	 */
	private TaskManager taskManager;

	/**
	 * A reference to the {@link CyServiceRegistrar} service used to add listeners for handling shutdown-related events
	 */
	private CyServiceRegistrar serviceRegistrar;
	
	private LocalHttpServer server;
	
	private AppManagerDialog appManagerDialog = null;
	
	
	/**
	 * Creates and sets up the AbstractCyAction, placing an item into the menu.
	 */
	public AppManagerAction(AppManager appManager, 
			CySwingApplication swingApplication, FileUtil fileUtil, TaskManager taskManager, CyServiceRegistrar serviceRegistrar) {
		super("App Manager");
		
		setPreferredMenu("Apps");
		setMenuGravity(1.0f);
		
		this.appManager = appManager;
		this.swingApplication = swingApplication;
		this.fileUtil = fileUtil;
		this.taskManager = taskManager;
		this.serviceRegistrar = serviceRegistrar;
		
		CyShutdownListener shutdownListener = createShutdownListener();
		serviceRegistrar.registerAllServices(shutdownListener, new Properties());
	}

	private CyShutdownListener createShutdownListener() {
		CyShutdownListener shutdownListener = new CyShutdownListener() {
			
			@Override
			public void handleEvent(CyShutdownEvent e) {
				
				if (appManagerDialog != null) {	
					appManagerDialog.setVisible(false);
					appManagerDialog.dispose();
				}
				
				serviceRegistrar.unregisterService(this, CyShutdownListener.class);
			}
		};
		
		return shutdownListener;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
				
		// Create and display the App Manager dialog
		if (appManagerDialog == null) {
			appManagerDialog = new AppManagerDialog(appManager, fileUtil, taskManager, swingApplication.getJFrame(), false);
		} else {
			appManagerDialog.pack();
			appManagerDialog.setVisible(true);
		}
	}

}
