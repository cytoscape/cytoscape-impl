package org.cytoscape.ding;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.ding.impl.DGraphView;

public class SelectModeTask extends AbstractTask {

	private CyApplicationManager applicationManagerServiceRef;
	private String actionName;

	public SelectModeTask(String actionName, CyApplicationManager applicationManagerServiceRef){
		this.applicationManagerServiceRef = applicationManagerServiceRef;
		this.actionName = actionName;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		
		CyNetworkView view = this.applicationManagerServiceRef.getCurrentNetworkView();

		if (view == null){
			return;
		}
		
		if (view instanceof DGraphView){
			DGraphView dView = (DGraphView)view;			
			if (this.actionName.equalsIgnoreCase("Nodes only")){
				dView.enableNodeSelection();
				dView.disableEdgeSelection();				
			}
			else if (this.actionName.equalsIgnoreCase("Edges only")){
				dView.disableNodeSelection();
				dView.enableEdgeSelection();					
			}
			else if (this.actionName.equalsIgnoreCase("Nodes and Edges")){
				dView.enableNodeSelection();
				dView.enableEdgeSelection();	
			}
		}
	}
}
