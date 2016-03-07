package org.cytoscape.internal.view;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


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
