package org.cytoscape.view.model.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.SelectedNodesAndEdgesEvent;


/**
 * Fires an event whenever the selection in the current network view changes.
 */
public class SelectionMediator implements RowsSetListener, SetCurrentNetworkViewListener {

	private final CyServiceRegistrar serviceRegistrar;
	
	public SelectionMediator(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if(!e.containsColumn(CyNetwork.SELECTED)) {
			return;
		}
		CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		if(networkView != null) {
			CyNetwork network = networkView.getModel();
			if(e.getSource() == network.getDefaultEdgeTable() || e.getSource() == network.getDefaultNodeTable()) {
				CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
				eventHelper.fireEvent(new SelectedNodesAndEdgesEvent(networkView));
			}
		}
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		CyNetworkView networkView = e.getNetworkView();
		if(networkView != null) {
			CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
			eventHelper.fireEvent(new SelectedNodesAndEdgesEvent(networkView));
		}
	}

}
