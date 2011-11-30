package org.cytoscape.cpath2.internal;

import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class ViewNetworkNeighborhoodTaskFactory implements NodeViewTaskFactory {
	// TODO: Wire this up
	
	// TODO: This should be a service property
    private static final String CONTEXT_MENU_TITLE = "View network neighborhood map";

	private View<CyNode> nodeView;
	private CyNetworkView networkView;

	public ViewNetworkNeighborhoodTaskFactory() {
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ViewNetworkNeighborhoodTask(nodeView, networkView));
	}

	@Override
	public void setNodeView(View<CyNode> nodeView, CyNetworkView networkView) {
		this.nodeView = nodeView;
		this.networkView = networkView;
	}
}
