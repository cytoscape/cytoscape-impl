package org.cytoscape.ding.impl;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ShowGraphicsDetailTask extends AbstractTask {

    private final CyApplicationManager appManager;
    private final CyProperty<Properties> defaultProps;

    ShowGraphicsDetailTask(final CyProperty<Properties> defaultProps, final CyApplicationManager appManager) {
	this.appManager = appManager;
	this.defaultProps = defaultProps;
    }


    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
	final RenderingEngine<CyNetwork> engine = appManager.getCurrentRenderingEngine();
	
	if(engine instanceof DGraphView == false)
	    return;
	
	final GraphLOD lod = ((DGraphView) engine).getGraphLOD();
	
	if (lod instanceof DingGraphLODAll)
	    ((DGraphView) engine).setGraphLOD(new DingGraphLOD(this.defaultProps,this.appManager));
	else
	    ((DGraphView) engine).setGraphLOD(new DingGraphLODAll());
	
	((CyNetworkView) engine.getViewModel()).updateView();

    }

}
