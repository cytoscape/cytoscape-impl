package org.cytoscape.task.internal.network;

import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkCollectionTask;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class DestroyNetworkTask extends AbstractNetworkCollectionTask {
	
	@Tunable(description = "<html>The selected networks will be lost.<br />Do you want to continue?</html>", params = "ForceSetDirectly=true", context = "gui")
	public boolean destroyCurrentNetwork = true;

	@Tunable(description = "Network to destroy", context = "nogui")
	public CyNetwork network;
	
	private final CyServiceRegistrar serviceRegistrar;

	public DestroyNetworkTask(Collection<CyNetwork> nets, CyServiceRegistrar serviceRegistrar) {
		super(nets);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		int i = 0;
		int networkCount;
		
		if (destroyCurrentNetwork) {
			tm.setProgress(0.0);
			CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
			
			if (networks == null || networks.isEmpty()) {
				if (network == null) {
					tm.showMessage(TaskMonitor.Level.ERROR, "Need to specify network to destroy");
					return;
				}
				
				destroyNetwork(network, netManager);
			} else {
				networkCount = networks.size();
				
				for (CyNetwork n : networks) {
					destroyNetwork(n, netManager);
					i++;
					tm.setProgress((i / (double) networkCount));
				}
			}
			
			tm.setProgress(1.0);
		}
	}

	private void destroyNetwork(final CyNetwork net, CyNetworkManager netManager) {
		CyRootNetwork rootNet = null;
		CySubNetwork parentNet = null;

		if (net instanceof CySubNetwork) {
			rootNet = ((CySubNetwork) net).getRootNetwork();
			final Long suid = DataUtils.getParentNetworkSUID((CySubNetwork) net);

			if (suid != null && netManager.getNetwork(suid) instanceof CySubNetwork)
				parentNet = (CySubNetwork) netManager.getNetwork(suid);
		}

		netManager.destroyNetwork(net);

		if (net instanceof CySubNetwork)
			updateParentNetworkData(net.getSUID(), rootNet, (parentNet != null ? parentNet.getSUID() : null));
	}

	private void updateParentNetworkData(Long destroyedSUID, CyRootNetwork rootNet, Long newParentSUID) {
		for (CySubNetwork sn : rootNet.getSubNetworkList()) {
			if (destroyedSUID.equals(DataUtils.getParentNetworkSUID(sn)))
				DataUtils.saveParentNetworkSUID(sn, newParentSUID);
		}
	}
}
