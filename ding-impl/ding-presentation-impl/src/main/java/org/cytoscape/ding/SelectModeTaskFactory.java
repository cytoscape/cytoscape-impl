package org.cytoscape.ding;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SelectModeTaskFactory extends AbstractTaskFactory {

	private CyApplicationManager applicationManagerServiceRef;
	private String actionName;
	
	public SelectModeTaskFactory(String actionName, CyApplicationManager applicationManagerServiceRef){
	
		this.applicationManagerServiceRef = applicationManagerServiceRef;
		this.actionName = actionName;
	}
	
	public TaskIterator createTaskIterator(){
		return new TaskIterator(new SelectModeTask(actionName, applicationManagerServiceRef));
	}
}
