package org.cytoscape.internal.view;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;

public class SubNetworkPanelModel extends AbstractNetworkPanelModel<CySubNetwork> {

	public SubNetworkPanelModel(final CySubNetwork subNetwork, final CyServiceRegistrar serviceRegistrar) {
		super(subNetwork, serviceRegistrar);
	}
	
	@Override
	public boolean isCurrent() {
		return getNetwork().equals(serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork());
	}
	
	@Override
	public int getSubNetworkCount() {
		return 0;
	}
}
