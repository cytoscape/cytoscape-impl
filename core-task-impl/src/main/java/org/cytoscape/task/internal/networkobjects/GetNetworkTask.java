package org.cytoscape.task.internal.networkobjects;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class GetNetworkTask extends AbstractTask implements ObservableTask{
	CyApplicationManager appMgr;
	CyServiceRegistrar serviceRegistrar;

	@Tunable(description="Network to return", 
	         longDescription = StringToModel.CY_NETWORK_LONG_DESCRIPTION,
					 exampleStringValue = StringToModel.CY_NETWORK_EXAMPLE_STRING,
	         context="nogui")
	public CyNetwork network;

	public GetNetworkTask(CyApplicationManager appMgr, CyServiceRegistrar serviceRegistrar) {
		this.appMgr = appMgr;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) network = appMgr.getCurrentNetwork();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Object getResults(Class type) {
		if (type.equals(CyNetwork.class)) {
			return network;
		} else if (type.equals(String.class)){
			if (network == null)
				return "<none>";
			return network.toString();
		} else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {if (network == null)
				return "{}";
			else {
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				return cyJSONUtil.toJson(network);
			}};
			return res;
		}
		return network;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, JSONResult.class);
	}
}
