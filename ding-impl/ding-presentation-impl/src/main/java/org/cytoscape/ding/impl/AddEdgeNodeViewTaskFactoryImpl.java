package org.cytoscape.ding.impl; 


import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;


public class AddEdgeNodeViewTaskFactoryImpl implements NodeViewTaskFactory {
	private final CyNetworkManager netMgr;

	public AddEdgeNodeViewTaskFactoryImpl(CyNetworkManager netMgr) {
		this.netMgr = netMgr;
	}
	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView,
			CyNetworkView networkView) {
		return new TaskIterator(new AddEdgeTask(nodeView, networkView));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		// TODO Auto-generated method stub
		return true;
	}
}
