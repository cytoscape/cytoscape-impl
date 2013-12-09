package org.cytoscape.filter.internal.view;

import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;

public class FilterWorker extends AbstractWorker<FilterPanel, FilterPanelController> {
	public FilterWorker(LazyWorkQueue queue, CyApplicationManager applicationManager) {
		super(queue, applicationManager);
	}

	@Override
	public void doWork() {
		if (controller == null) {
			return;
		}
		
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		if (networkView == null) {
			return;
		}
		
		final CyNetwork network = networkView.getModel(); 
		if (network == null) {
			return;
		}
		
		controller.setProgress(0, view);
		controller.setStatus(view, null);
		int nodeCount = 0;
		int edgeCount = 0;
		int counter = 0;
		long startTime = System.currentTimeMillis();
		try {
			Filter<CyNetwork, CyIdentifiable> filter = controller.getFilter();
			if (filter instanceof CompositeFilter) {
				// If we have an empty CompositeFilter, bail out. 
				CompositeFilter<CyNetwork, CyIdentifiable> composite = (CompositeFilter<CyNetwork, CyIdentifiable>) filter;
				if (composite.getLength() == 0) {
					return;
				}
			}
			
			List<CyNode> nodeList = network.getNodeList();
			List<CyEdge> edgeList = network.getEdgeList();
			double total = nodeList.size() + edgeList.size();
			for (CyNode node : nodeList) {
				if (isCancelled) {
					return;
				}
				CyRow row = network.getRow(node);
				boolean accepted = filter.accepts(network, node);
				if (accepted) {
					nodeCount++;
				}
				if (row.get(CyNetwork.SELECTED, Boolean.class) != accepted) {
					row.set(CyNetwork.SELECTED, accepted);
				}
				controller.setProgress(++counter / total, view);
			}
			for (CyEdge edge : edgeList) {
				if (isCancelled) {
					return;
				}
				CyRow row = network.getRow(edge);
				boolean accepted = filter.accepts(network, edge);
				if (accepted) {
					edgeCount++;
				}
				if (row.get(CyNetwork.SELECTED, Boolean.class) != accepted) {
					row.set(CyNetwork.SELECTED, accepted);
				}
				controller.setProgress(++counter / total, view);
			}
			networkView.updateView();
		} finally {
			long duration = System.currentTimeMillis() - startTime;
			controller.setProgress(1.0, view);
			controller.setStatus(view, String.format("Selected %d %s and %d %s in %dms",
					nodeCount,
					nodeCount == 1 ? "node" : "nodes",
					edgeCount,
					edgeCount == 1 ? "edge" : "edges",
					duration));
			isCancelled = false;
		}
	}
}
