package org.cytoscape.ding.impl; 


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;


public class AddEdgeNodeViewTaskFactoryImpl implements NodeViewTaskFactory {

	private final VisualMappingManager vmm;
	private final CyEventHelper eh;

	public AddEdgeNodeViewTaskFactoryImpl(final VisualMappingManager vmm, final CyEventHelper eh) {
		this.vmm = vmm;
		this.eh = eh;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new AddEdgeTask(nodeView, networkView, vmm, eh));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return true;
	}
}
