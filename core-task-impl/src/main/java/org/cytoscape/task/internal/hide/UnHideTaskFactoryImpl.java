package org.cytoscape.task.internal.hide;

import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class UnHideTaskFactoryImpl implements UnHideTaskFactory {

	final private CyServiceRegistrar serviceRegistrar;
	
	public UnHideTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view, Collection<CyNode> nodes, Collection<CyEdge> edges) {
		return new TaskIterator(new UnHideTask("UnHide Nodes and Edges", nodes, edges, view, serviceRegistrar));
	}

}
