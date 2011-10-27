package org.cytoscape.internal;

import org.cytoscape.application.events.CytoscapeStartEvent;
import org.cytoscape.application.events.CytoscapeStartListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * This class listens CytoscapeStartEvent and pop up QuickStart dialog
 */
public class QuickStartStartup implements CytoscapeStartListener {

	private TaskFactory quickStartTaskFactory;
	private DialogTaskManager guiTaskManager;
	private CySwingApplication swingApp;
	
	public QuickStartStartup(TaskFactory quickStartTaskFactory, DialogTaskManager guiTaskManager, CySwingApplication swingApp){	
		this.quickStartTaskFactory = quickStartTaskFactory;
		this.guiTaskManager = guiTaskManager;
		this.swingApp = swingApp;
	}
	
	public void handleEvent(CytoscapeStartEvent e){
		guiTaskManager.setExecutionContext(swingApp.getJFrame());
		guiTaskManager.execute(quickStartTaskFactory);
	}	
}
