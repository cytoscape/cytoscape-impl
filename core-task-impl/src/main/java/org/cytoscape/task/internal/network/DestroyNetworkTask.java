package org.cytoscape.task.internal.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkCollectionTask;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class DestroyNetworkTask extends AbstractNetworkCollectionTask implements ObservableTask {
	
	@Tunable(description = "<html>The selected networks will be lost.<br />Do you want to continue?</html>", params = "ForceSetDirectly=true", context = "gui")
	public boolean destroyCurrentNetwork = true;

	@Tunable(description = "Network to destroy", 
	         longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, 
					 exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING,
	         context = "nogui")
	public CyNetwork network;
	
	private final CyServiceRegistrar serviceRegistrar;
	private List<CyNetwork> localNets;

	public DestroyNetworkTask(Collection<CyNetwork> nets, CyServiceRegistrar serviceRegistrar) {
		super(nets);
		
		if (nets != null && !nets.isEmpty())
			localNets = new ArrayList<>(nets);
		
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		if (destroyCurrentNetwork) {
			tm.setTitle("Destroy Network(s)");
			tm.setProgress(0.0);
			
			var netManager = serviceRegistrar.getService(CyNetworkManager.class);
			var appManager = serviceRegistrar.getService(CyApplicationManager.class);
			
			if (localNets == null || localNets.isEmpty()) {
				if (network == null) {
					tm.showMessage(TaskMonitor.Level.ERROR, "Need to specify network to destroy");
					return;
				} else {
					localNets = Collections.singletonList(network);
				}
			}

			int i = 0;
			int networkCount = localNets.size();
				
			for (CyNetwork n : localNets) {
				if (cancelled)
					return;
				
				if (n.equals(appManager.getCurrentNetwork())) {
					appManager.setCurrentNetwork(null);
					appManager.setCurrentNetworkView(null);
				}
				
				destroyNetwork(n, netManager);
				i++;
				tm.setProgress((i / (double) networkCount));
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

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class type) {
		if (localNets == null) return null;
		if (type.equals(String.class)) {
			String res = "Destroyed network";
			if (localNets.size() > 1) res += "s";
			res += ":\n";
			for (CyNetwork net: localNets)
				res += "    "+net.toString()+"\n";
			return res;
		} else if (type.equals(CyNetwork.class)) {
			return localNets.get(0);
		} else if (type.equals(List.class)) {
			return localNets;
		} else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {if (localNets == null) 
				return "{}";
			else {
				return "{\"network\":"+localNets.get(0).getSUID()+"}";
			}};
			return res;
		}
		return null;
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(CyNetwork.class, List.class, String.class, JSONResult.class);
	}
}
