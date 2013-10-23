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
	private FilterPanel panel;
	
	private volatile boolean isCancelled;
	private boolean isInteractive;
	
	public ViewUpdater(CyApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
	}

	public void setView(FilterPanel panel) {
		this.panel = panel;
	}
	
	@Override
	public void handleSettingsChanged() {
		if (!isInteractive) {
			return;
		}
		requestWork();
	}

	public void handleFilterStructureChanged() {
		if (!isInteractive) {
			return;
		}
		requestWork();
	}
	
	public void cancel() {
		isCancelled = true;
	}
	
	public void setInteractive(boolean isInteractive) {
		this.isInteractive = isInteractive;
	}
	
	@Override
	protected void doWork() {
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
		
		controller.setUpdating(true, panel);
		try {
			Filter<CyNetwork, CyIdentifiable> filter = controller.getFilter();
			
			for (CyNode node : network.getNodeList()) {
				if (isCancelled) {
					return;
				}
				CyRow row = network.getRow(node);
				row.set(CyNetwork.SELECTED, filter.accepts(network, node));
			}
			for (CyEdge edge : network.getEdgeList()) {
				if (isCancelled) {
					return;
				}
				CyRow row = network.getRow(edge);
				row.set(CyNetwork.SELECTED, filter.accepts(network, edge));
			}
			networkView.updateView();
		} finally {
			controller.setUpdating(false, panel);
			isCancelled = false;
		}
	}

	public void setController(FilterPanelController controller) {
		this.controller = controller;
	}
}
