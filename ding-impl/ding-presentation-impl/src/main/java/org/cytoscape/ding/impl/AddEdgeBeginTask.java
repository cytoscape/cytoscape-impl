package org.cytoscape.ding.impl;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task for the "Add > Edge" context menu item.
 */
public class AddEdgeBeginTask extends AbstractNodeViewTask {

	private final CyServiceRegistrar serviceRegistrar;
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public AddEdgeBeginTask(final View<CyNode> nv, final CyNetworkView view, final CyServiceRegistrar serviceRegistrar) {
		super(nv, view);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Add Edge (Begin)");
		
		DRenderingEngine re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(netView);
		InputHandlerGlassPane glassPane = re.getInputHandlerGlassPane();
		glassPane.beginAddingEdge(nodeView);
	}
}
