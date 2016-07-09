package org.cytoscape.task.internal.destruction;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.task.AbstractNetworkCollectionTask;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


public class DestroyNetworkTask extends AbstractNetworkCollectionTask {
	private final CyNetworkManager netmgr;
	
	@Tunable(description="<html>The selected networks will be lost.<br />Do you want to continue?</html>", params="ForceSetDirectly=true",context="gui")
	public boolean destroyCurrentNetwork = true;

	@Tunable(description="Network to destroy", context="nogui")
	public CyNetwork network = null;

	public DestroyNetworkTask(final Collection<CyNetwork> nets, final CyNetworkManager netmgr) {
		super(nets);
		this.netmgr = netmgr;
	}

	@Override
	public void run(TaskMonitor tm) {
		int i = 0;
		int networkCount;
		
		if (destroyCurrentNetwork) {
			tm.setProgress(0.0);
			
			if (networks == null || networks.isEmpty()) {
				if (network == null) {
					tm.showMessage(TaskMonitor.Level.ERROR, "Need to specify network to destroy");
					return;
				}
				
				destroyNetwork(network);
			} else {
				networkCount = networks.size();
				
				for (CyNetwork n : networks) {
					destroyNetwork(n);
					i++;
					tm.setProgress((i / (double) networkCount));
				}
			}
			
			tm.setProgress(1.0);
		}
	}

	private void destroyNetwork(final CyNetwork net) {
		CyRootNetwork rootNet = null;
		CySubNetwork parentNet = null;
		
		if (net instanceof CySubNetwork) {
			rootNet = ((CySubNetwork) net).getRootNetwork();
			final Long suid = DataUtils.getParentNetworkSUID((CySubNetwork) net);
			
			if (suid != null && netmgr.getNetwork(suid) instanceof CySubNetwork)
				parentNet = (CySubNetwork) netmgr.getNetwork(suid);
		}
		
		netmgr.destroyNetwork(net);
		
		if (net instanceof CySubNetwork)
			updateParentNetworkData(net.getSUID(), rootNet, (parentNet != null ? parentNet.getSUID() : null));
	}
	
	private void updateParentNetworkData(final Long destroyedSUID, final CyRootNetwork rootNet,
			final Long newParentSUID) {
		for (CySubNetwork sn : rootNet.getSubNetworkList()) {
			if (destroyedSUID.equals(DataUtils.getParentNetworkSUID(sn)))
				DataUtils.saveParentNetworkSUID(sn, newParentSUID);
		}
	}
}
