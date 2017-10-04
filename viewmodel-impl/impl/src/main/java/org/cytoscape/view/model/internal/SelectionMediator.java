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
 * @author mkucera
 */
public class SelectionMediator implements RowsSetListener, SetCurrentNetworkViewListener {

	private final CyApplicationManager applicationManager;
	private final CyEventHelper eventHelper;
	
	public SelectionMediator(CyServiceRegistrar serviceRegistrar) {
		this.applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		this.eventHelper = serviceRegistrar.getService(CyEventHelper.class);
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if(!e.containsColumn(CyNetwork.SELECTED)) {
			return;
		}
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		if(networkView != null) {
			CyNetwork network = networkView.getModel();
			if(e.getSource() == network.getDefaultEdgeTable() || e.getSource() == network.getDefaultNodeTable()) {
				eventHelper.fireEvent(new SelectedNodesAndEdgesEvent(networkView));
			}
		}
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		CyNetworkView networkView = e.getNetworkView();
		eventHelper.fireEvent(new SelectedNodesAndEdgesEvent(networkView));
	}

}
