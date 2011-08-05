package org.cytoscape.biopax.internal.action;

import org.cytoscape.biopax.internal.BioPaxFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class DisplayBioPaxXmlActionNodeViewTaskFactory implements NodeViewTaskFactory {

	private View<CyNode> nodeView;
	private CyNetworkView networkView;
	private final BioPaxFactory factory;

	public DisplayBioPaxXmlActionNodeViewTaskFactory(BioPaxFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new DisplayBioPaxXmlTask(nodeView, networkView, factory));
	}

	@Override
	public void setNodeView(View<CyNode> nodeView, CyNetworkView networkView) {
		this.nodeView = nodeView;
		this.networkView = networkView;
	}

}
