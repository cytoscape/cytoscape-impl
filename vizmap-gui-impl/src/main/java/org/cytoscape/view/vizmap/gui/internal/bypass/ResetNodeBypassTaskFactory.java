package org.cytoscape.view.vizmap.gui.internal.bypass;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class ResetNodeBypassTaskFactory extends AbstractNodeViewTaskFactory {

	private final CyApplicationManager applicationManager;

	public ResetNodeBypassTaskFactory(final CyApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new ResetBypassTask(applicationManager.getCurrentRenderingEngine().getVisualLexicon(),
				nodeView, networkView));
	}

}