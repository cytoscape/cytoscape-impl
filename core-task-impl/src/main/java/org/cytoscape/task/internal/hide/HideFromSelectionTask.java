package org.cytoscape.task.internal.hide;

import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;


public class HideFromSelectionTask extends AbstractNetworkViewTask {
	
	private final String description;
	private final boolean hideNodes;
	private final boolean hideEdges;
	private final boolean selectionValue;
	private final CyServiceRegistrar serviceRegistrar;

	public HideFromSelectionTask(
			final String description,
			final boolean hideNodes,
			final boolean hideEdges,
			final boolean selectionValue,
			final CyNetworkView view,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(view);
		this.description = description;
		this.hideNodes = hideNodes;
		this.hideEdges = hideEdges;
		this.selectionValue = selectionValue;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor tm) {
		tm.setProgress(0.0);
		final CyNetwork network = view.getModel();
		List<CyNode> nodes = null;
		List<CyEdge> edges = null;
		
		if (hideNodes) {
			nodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, selectionValue);
		}
		
		tm.setProgress(0.1);
		
		if (hideEdges) {
			edges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, selectionValue);
		}
		
		HideTask hideTask = new HideTask(description, nodes, edges, view, serviceRegistrar);
		insertTasksAfterCurrentTask(hideTask);
	}
}
