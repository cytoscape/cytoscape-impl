package org.cytoscape.internal;

import javax.swing.SwingUtilities;

import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * This class listens CytoscapeStartEvent and pop up QuickStart dialog
 */
public class QuickStartStartup implements CyStartListener {

	private final TaskFactory quickStartTaskFactory;
	private final DialogTaskManager guiTaskManager;
	private final CySwingApplication swingApp;

	public QuickStartStartup(final TaskFactory quickStartTaskFactory, final DialogTaskManager guiTaskManager,
			final CySwingApplication swingApp) {
		this.quickStartTaskFactory = quickStartTaskFactory;
		this.guiTaskManager = guiTaskManager;
		this.swingApp = swingApp;
	}

	
	@Override
	public void handleEvent(CyStartEvent e) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				guiTaskManager.setExecutionContext(swingApp.getJFrame());
				guiTaskManager.execute(quickStartTaskFactory.createTaskIterator());
			}
		});
	}
}
