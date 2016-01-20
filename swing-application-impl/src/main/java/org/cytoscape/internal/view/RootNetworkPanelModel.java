package org.cytoscape.internal.view;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;

public class RootNetworkPanelModel extends AbstractNetworkPanelModel<CyRootNetwork> {
	
	public RootNetworkPanelModel(final CyRootNetwork rootNetwork, final CyServiceRegistrar serviceRegistrar) {
		super(rootNetwork, serviceRegistrar);
	}
	
	@Override
	public int getSubNetworkCount() {
		int count = 0;
		final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
		
		// Count number of public subnetworks
		for (CySubNetwork net : getNetwork().getSubNetworkList()) {
			if (netManager.networkExists(net.getSUID()))
				count++;
		}
		
		return count;
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
