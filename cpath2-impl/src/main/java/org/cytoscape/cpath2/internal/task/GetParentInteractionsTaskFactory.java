package org.cytoscape.cpath2.internal.task;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.work.TaskIterator;

// TODO: Wire this in OSGi; this class is never called... (remove?)
public class GetParentInteractionsTaskFactory extends AbstractNodeViewTaskFactory {
	private final CPath2Factory factory;

	public GetParentInteractionsTaskFactory(CPath2Factory factory) {
		this.factory = factory;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		CyNetwork network = netView.getModel();
		CyNode node = nodeView.getModel();
		return new TaskIterator(new GetParentInteractions(network, node, factory));
	}	
}
