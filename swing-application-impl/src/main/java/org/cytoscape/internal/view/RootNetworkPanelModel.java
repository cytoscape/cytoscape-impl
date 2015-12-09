package org.cytoscape.internal.view;

import java.beans.PropertyChangeSupport;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;

public class RootNetworkPanelModel {
	
	private final CyRootNetwork rootNetwork;
	
	private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	
	public RootNetworkPanelModel(final CyRootNetwork rootNetwork) {
		if (rootNetwork == null)
			throw new IllegalArgumentException("'rootNetwork' must not be null.");
		
		this.rootNetwork = rootNetwork;
	}
	
	public CyRootNetwork getRootNetwork() {
		return rootNetwork;
	}
	
	public String getRootNetworkName() {
		return getRootNetwork().getRow(getRootNetwork()).get(CyNetwork.NAME, String.class);
	}
	
	public int getSubNetworkCount() {
		return getRootNetwork().getSubNetworkList().size();
	}
}
