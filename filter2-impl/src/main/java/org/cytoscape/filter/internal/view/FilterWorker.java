package org.cytoscape.filter.internal.view;

import java.util.List;

import org.cytoscape.application.CyApplicationManager;
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
		try {
			Filter<CyNetwork, CyIdentifiable> filter = controller.getFilter();
			
			List<CyNode> nodeList = network.getNodeList();
			List<CyEdge> edgeList = network.getEdgeList();
			double total = nodeList.size() + edgeList.size();
			int counter = 0;
			for (CyNode node : nodeList) {
				if (isCancelled) {
					return;
				}
				CyRow row = network.getRow(node);
				row.set(CyNetwork.SELECTED, filter.accepts(network, node));
				controller.setProgress(++counter / total, view);
			}
			for (CyEdge edge : edgeList) {
				if (isCancelled) {
					return;
				}
				CyRow row = network.getRow(edge);
				row.set(CyNetwork.SELECTED, filter.accepts(network, edge));
				controller.setProgress(++counter / total, view);
			}
			networkView.updateView();
		} finally {
			controller.setProgress(1.0, view);
			isCancelled = false;
		}
	}
}
