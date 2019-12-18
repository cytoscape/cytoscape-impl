package org.cytoscape.model.internal;

import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
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
	
	private boolean fireSelectionEvents = false;
	
	/**
	 * The reason to debounce is because if a large number of RowSetEvents are coming
	 * in and we fire a selection event in the middle of that it causes 
	 * flushPayloadEvents() start firing a huge amount of events with one payload each. 
	 * Instead fire the selection event after a short delay
	 * to give some time for more payload events to accumulate before the next selection event.
	 */
	private DebounceTimer debounceTimer;
	
	
	public SelectionMediator(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if(!fireSelectionEvents)
			return;
		if(!e.containsColumn(CyNetwork.SELECTED))
			return;
		
		if(debounceTimer == null) {
			debounceTimer = new DebounceTimer(100);
		}
		
		CyTable source = e.getSource();
		CyNetwork network = getNetworkTableManager().getNetworkForTable(source);
		if(network == null)
			return;
		
		CyNetwork currentNetwork = getApplicationManager().getCurrentNetwork();
		boolean isCurrent = network.equals(currentNetwork);
		CyEventHelper eventHelper = getEventHelper();
		
		if(source == network.getDefaultNodeTable()) {
			debounceTimer.debounce("nodes", () -> {
				eventHelper.fireEvent(new SelectedNodesAndEdgesEvent(network, isCurrent, true, false));
			});
		} 
		else if(source == network.getDefaultEdgeTable()) {
			debounceTimer.debounce("edges", () -> {
				eventHelper.fireEvent(new SelectedNodesAndEdgesEvent(network, isCurrent, false, true));
			});
		}
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
	
	
	public void listenerAdded(SelectedNodesAndEdgesListener listener, Map<String,String> args) {
		fireSelectionEvents = true;
	}
	
	public void listenerRemoved(SelectedNodesAndEdgesListener listener, Map<String,String> args) {
	}
	

}
