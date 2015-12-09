package org.cytoscape.internal.view;

import java.beans.PropertyChangeSupport;
import java.util.Collection;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

public class SubNetworkPanelModel {

	private final CySubNetwork subNetwork;
	private int depth;
	
	private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	private final CyServiceRegistrar serviceRegistrar;
	
	public SubNetworkPanelModel(final CySubNetwork subNetwork, final CyServiceRegistrar serviceRegistrar) {
		if (subNetwork == null)
			throw new IllegalArgumentException("'subNetwork' must not be null.");
		if (serviceRegistrar == null)
			throw new IllegalArgumentException("'serviceRegistrar' must not be null.");
		
		this.subNetwork = subNetwork;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	public CySubNetwork getSubNetwork() {
		return subNetwork;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(final int newValue) {
		if (newValue != depth) {
			final int oldValue = depth;
			depth = newValue;
			changeSupport.firePropertyChange("depth", oldValue, newValue);
		}
	}
	
	public boolean isCurrent() {
		return subNetwork.equals(serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork());
	}
	
	public String getNetworkName() {
		return getSubNetwork().getRow(getSubNetwork()).get(CyNetwork.NAME, String.class);
	}
	
	public Collection<CyNetworkView> getNetworkViews() {
		return serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViews(subNetwork);
	}
	
	public int getViewCount() {
		return getNetworkViews().size();
	}
	
	public int getNodeCount() {
		return subNetwork.getNodeCount();
	}
	
	public int getEdgeCount() {
		return subNetwork.getEdgeCount();
	}
}
