package org.cytoscape.model.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.service.util.CyServiceRegistrar;


/**
 * Node and Edge selection causes separate RowsSetEvents because they have different
 * event sources. This class works to coalesce nodes and edges into a single
 * selection event.
 * 
 */
public class SelectionMediator implements RowsSetListener {

	private final CyServiceRegistrar serviceRegistrar;
	
	private CyNetworkTableManager networkTableManager;
	private CyApplicationManager applicationManager;
	private CyEventHelper eventHelper;
	
	public SelectionMediator(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if(!e.containsColumn(CyNetwork.SELECTED))
			return;
		
		CyTable source = e.getSource();
		CyNetwork network = getNetworkTableManager().getNetworkForTable(source);
		if(network == null)
			return;
		
		CyNetwork currentNetwork = getApplicationManager().getCurrentNetwork();
		boolean isCurrent = network.equals(currentNetwork);
		
		if(source == network.getDefaultNodeTable())
			getEventHelper().fireEvent(new SelectedNodesAndEdgesEvent(network, isCurrent, true, false));
		else if(source == network.getDefaultEdgeTable())
			getEventHelper().fireEvent(new SelectedNodesAndEdgesEvent(network, isCurrent, false, true));
	}
	
	// save the service references for a minor performance boost, it takes a bit of time to look up a service
	private CyNetworkTableManager getNetworkTableManager() {
		if(networkTableManager == null) {
			networkTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
		}
		return networkTableManager;
	}
	
	private CyApplicationManager getApplicationManager() {
		if(applicationManager == null) {
			applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		}
		return applicationManager;
	}
	
	private CyEventHelper getEventHelper() {
		if(eventHelper == null) {
			eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		}
		return eventHelper;
	}
	

}
