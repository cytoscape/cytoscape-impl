package org.cytoscape.ding;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingGraphLODAll;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.task.NetworkViewTaskFactory;

public class ShowGraphicsDetailsTaskFactory implements NetworkViewTaskFactory {

	private CyApplicationManager applicationManagerServiceRef;
	private final DingGraphLOD dingGraphLOD;
	private final DingGraphLODAll dingGraphLODAll;
	
	public ShowGraphicsDetailsTaskFactory(CyApplicationManager applicationManagerServiceRef, DingGraphLOD dingGraphLOD, DingGraphLODAll dingGraphLODAll){
		this.applicationManagerServiceRef = applicationManagerServiceRef;
		this.dingGraphLOD = dingGraphLOD;
		this.dingGraphLODAll = dingGraphLODAll;
	}
	
	
	public TaskIterator createTaskIterator(CyNetworkView networkView){
		return new TaskIterator(new ShowGraphicsDetailsTask(applicationManagerServiceRef, dingGraphLOD, dingGraphLODAll));
	}
	
	/**
	 * Returns true if this task factory is ready to produce a TaskIterator.
	 * @param networkView
	 * @return true if this task factory is ready to produce a TaskIterator.
	 */
	public boolean isReady(CyNetworkView networkView){
		return true;
	}
}
