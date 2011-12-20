package org.cytoscape.biopax.internal.action;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class DisplayBioPaxXmlTaskFactory implements NodeViewTaskFactory {

	private View<CyNode> nodeView;
	private CyNetworkView networkView;
	private CySwingApplication cySwingApplication;

	public DisplayBioPaxXmlTaskFactory(CySwingApplication cySwingApplication) {
		this.cySwingApplication = cySwingApplication;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DisplayBioPaxXmlTask(nodeView, networkView, cySwingApplication));
	}

	@Override
	public void setNodeView(View<CyNode> nodeView, CyNetworkView networkView) {
		this.nodeView = nodeView;
		this.networkView = networkView;
	}

}
