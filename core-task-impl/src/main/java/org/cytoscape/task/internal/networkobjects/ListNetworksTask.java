package org.cytoscape.task.internal.networkobjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class ListNetworksTask extends AbstractTask implements ObservableTask{
	
	Set<CyNetwork> networks;
	private final CyServiceRegistrar serviceRegistrar;

	public ListNetworksTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor tm) {
		networks = serviceRegistrar.getService(CyNetworkManager.class).getNetworkSet();
		tm.showMessage(TaskMonitor.Level.INFO, "Found " + networks.size() + " networks.");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getResults(Class type) {
		if (type.equals(List.class)) {
			return  new ArrayList<CyNetwork>(networks);
		} else if (type.equals(Set.class)) {
			return  networks;
		} else if (type.equals(String.class)){
			String res = "";
			for (CyNetwork network: networks) {
				res += network.toString()+"\n";
			}
			return res.substring(0, res.length()-1);
		} else if (type.equals(JSONResult.class)){
			JSONResult res = () -> {if (networks == null || networks.size() == 0) 
				return "{}";
			else {
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				return "{\"networks\":"+cyJSONUtil.cyIdentifiablesToJson(networks)+"}";
			}};
			return res;
		}
		return networks;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(List.class, Set.class, String.class, JSONResult.class);
	}
}
