package org.cytoscape.view.vizmap.gui.internal.bypass;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class ResetEdgeBypassTaskFactory extends AbstractEdgeViewTaskFactory {

	private final CyApplicationManager applicationManager;

	public ResetEdgeBypassTaskFactory(final CyApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyEdge> edgeView, CyNetworkView networkView) {
		return new TaskIterator(new ResetBypassTask(applicationManager.getCurrentRenderingEngine().getVisualLexicon(),
				edgeView, networkView));
	}

}
