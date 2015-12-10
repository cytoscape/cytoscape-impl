package org.cytoscape.internal.view;

import java.beans.PropertyChangeSupport;
import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

public abstract class AbstractNetworkPanelModel<T extends CyNetwork> {

	private T network;
	
	protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	protected final CyServiceRegistrar serviceRegistrar;

	protected AbstractNetworkPanelModel(final T network, final CyServiceRegistrar serviceRegistrar) {
		if (network == null)
			throw new IllegalArgumentException("'subNetwork' must not be null.");
		if (serviceRegistrar == null)
			throw new IllegalArgumentException("'serviceRegistrar' must not be null.");
		
		this.network = network;
		this.serviceRegistrar = serviceRegistrar;
	}

	public T getNetwork() {
		return network;
	}
	
	public Collection<CyNetworkView> getNetworkViews() {
		return serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViews(getNetwork());
	}
	
	public int getViewCount() {
		return getNetworkViews().size();
	}
	
	public int getNodeCount() {
		return getNetwork().getNodeCount();
	}
	
	public int getEdgeCount() {
		return getNetwork().getEdgeCount();
	}
	
	public String getNetworkName() {
		return getNetwork().getRow(getNetwork()).get(CyNetwork.NAME, String.class);
	}

	public abstract int getSubNetworkCount();

	public abstract boolean isCurrent();
}
