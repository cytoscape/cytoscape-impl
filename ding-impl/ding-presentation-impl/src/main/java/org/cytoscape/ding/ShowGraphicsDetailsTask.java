package org.cytoscape.ding;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingGraphLODAll;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.model.CyNetwork;

public class ShowGraphicsDetailsTask extends AbstractTask {

	private CyApplicationManager applicationManagerServiceRef;
	private final DingGraphLOD dingGraphLOD;
	private final DingGraphLODAll dingGraphLODAll;

	
	public ShowGraphicsDetailsTask(CyApplicationManager applicationManagerServiceRef, DingGraphLOD dingGraphLOD, DingGraphLODAll dingGraphLODAll){
		this.applicationManagerServiceRef = applicationManagerServiceRef;
		this.dingGraphLOD = dingGraphLOD;
		this.dingGraphLODAll = dingGraphLODAll;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		
		final RenderingEngine<CyNetwork> engine = applicationManagerServiceRef.getCurrentRenderingEngine();

		if (engine instanceof DGraphView == false)
			return;

		final GraphLOD lod = ((DGraphView) engine).getGraphLOD();

		if (lod instanceof DingGraphLODAll) {
			((DGraphView) engine).setGraphLOD(dingGraphLOD);
		} else {
			((DGraphView) engine).setGraphLOD(dingGraphLODAll);
		}
		((CyNetworkView) engine.getViewModel()).updateView();
	}
}
