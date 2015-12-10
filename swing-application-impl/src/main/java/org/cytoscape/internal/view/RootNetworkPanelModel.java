package org.cytoscape.internal.view;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;

public class RootNetworkPanelModel extends AbstractNetworkPanelModel<CyRootNetwork> {
	
	public RootNetworkPanelModel(final CyRootNetwork rootNetwork, final CyServiceRegistrar serviceRegistrar) {
		super(rootNetwork, serviceRegistrar);
	}
	
	@Override
	public int getSubNetworkCount() {
		return getNetwork().getSubNetworkList().size();
	}

	@Override
	public boolean isCurrent() {
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		final CyNetwork currentNetwork = applicationManager.getCurrentNetwork();
		
		for (CySubNetwork sn : getNetwork().getSubNetworkList()) {
			if (sn.equals(currentNetwork))
				return true;
		}
		
		return false;
	}
}
