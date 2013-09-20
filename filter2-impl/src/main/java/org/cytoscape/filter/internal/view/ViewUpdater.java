package org.cytoscape.filter.internal.view;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.TransformerListener;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;

public class ViewUpdater extends LazyWorker implements TransformerListener {
	private CyApplicationManager applicationManager;
	private FilterPanelController controller;
	
	public ViewUpdater(CyApplicationManager applicationManager, FilterPanelController controller) {
		this.applicationManager = applicationManager;
		this.controller = controller;
	}

	@Override
	public void handleSettingsChanged() {
		requestWork();
	}

	public void handleFilterStructureChanged() {
		requestWork();
	}
	
	@Override
	protected void doWork() {
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		final CyNetwork network = networkView.getModel(); 
		if (network == null) {
			return;
		}
		
		Filter<CyNetwork, CyIdentifiable> filter = controller.getFilter();
		
		for (CyNode node : network.getNodeList()) {
			CyRow row = network.getRow(node);
			row.set(CyNetwork.SELECTED, filter.accepts(network, node));
		}
		for (CyEdge edge : network.getEdgeList()) {
			CyRow row = network.getRow(edge);
			row.set(CyNetwork.SELECTED, filter.accepts(network, edge));
		}
		networkView.updateView();
	}
}
