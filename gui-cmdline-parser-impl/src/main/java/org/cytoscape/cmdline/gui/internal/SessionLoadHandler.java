package org.cytoscape.cmdline.gui.internal;

import java.io.File;
import java.nio.file.Path;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

import com.install4j.api.launcher.StartupNotification;


public class SessionLoadHandler {

	public SessionLoadHandler(CyServiceRegistrar registrar, OpenSessionTaskFactory openSessionTaskFactory) {
		this.registrar = registrar;
		this.openSessionTaskFactory = openSessionTaskFactory;
	}

	private final CyServiceRegistrar registrar;
	private final OpenSessionTaskFactory openSessionTaskFactory;
	
	
	public void startListeningForSessionFileOpen() {
		// Use install4j API to listen for the user double clicking on a cys file *after* cytoscape has already started.
		StartupNotification.registerStartupListener(new StartupNotification.Listener() {
			public void startupPerformed(String parameters) {
				openSession(parameters);
			}
		});
	}
	
	
	private void openSession(String parameters) {
		File file;
		try {
			// Bail if not a valid file path
			file = Path.of(parameters).toFile();
		} catch(Exception e) {
			return;
		}
		
		TaskIterator iter = openSessionTaskFactory.createTaskIterator(file, true);
		
		TaskManager <?,?> taskManager = registrar.getService(TaskManager.class);
		taskManager.execute(iter);
	}
	
}
