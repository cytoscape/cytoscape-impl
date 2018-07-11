package org.cytoscape.task.internal.hide;

import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;


public class UnHideAllTask extends AbstractNetworkViewTask {

	private final String description;
	private final boolean unhideNodes;
	private final boolean unhideEdges;
	private final CyServiceRegistrar serviceRegistrar;

	public UnHideAllTask(
			final String description,
			final boolean unhideNodes,
			final boolean unhideEdges,
			final CyNetworkView view,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(view);
		this.description = description;
		this.unhideNodes = unhideNodes;
		this.unhideEdges = unhideEdges;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor e) {
		e.setProgress(0.0);
		final CyNetwork network = view.getModel();
		List<CyNode> nodes = null;
		List<CyEdge> edges = null;

		if (unhideNodes) {
			nodes = network.getNodeList();
		}
		
		e.setProgress(0.1);

		if (unhideEdges) {
			edges = network.getEdgeList();
		}

		UnHideTask unHideTask = new UnHideTask(description, nodes, edges, view, serviceRegistrar);
		insertTasksAfterCurrentTask(unHideTask);
	}

}
