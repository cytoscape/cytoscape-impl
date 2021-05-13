package org.cytoscape.task.internal.networkobjects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.view.model.VisualProperty;
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

public class GetNetworkPropertiesTask extends AbstractPropertyTask implements ObservableTask {
	
	Map<CyNetwork, Map<String, VisualPropertyObjectTuple>> propertyMap;
	@Tunable(description="Network to get properties for", 
	         longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, 
					 exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING,
	         required=true,
	         context="nogui")
	public CyNetwork network;

	@Tunable(description="Properties to get the value for", 
	         longDescription="A comma-separated list of network properties", 
					 exampleStringValue="background paint,title",
	         required=true,
	         context="nogui")
	public String propertyList;
	
	public GetNetworkPropertiesTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		propertyMap = new HashMap<>();
	}

	@Override
	public void run(final TaskMonitor tm) {
		if (network == null) {
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
			if (network == null) {
				tm.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
				return;
			}
		}

		if (propertyList == null || propertyList.length() == 0) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Property list must be specified");
			return;
		}

		tm.showMessage(TaskMonitor.Level.INFO, "   Property values for network "+DataUtils.getNetworkName(network)+":");
		String[] props = propertyList.split(",");

		Map<String, VisualPropertyObjectTuple> pMap = new HashMap<>();
		
		for (String property: props) {
			try {
				VisualProperty vp = getProperty(network, network, property.trim());
				Object value = getPropertyValue(network, network, vp);
				
				if (value != null) {
					tm.showMessage(TaskMonitor.Level.INFO, "        "+vp.getDisplayName()+"="+value.toString());
					pMap.put(vp.getIdString(), new VisualPropertyObjectTuple(vp,value));
				}
			} catch (Exception e) {
				tm.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
				return;
			}
		}
		
		propertyMap.put(network, pMap);
	}

	@Override
	public Object getResults(Class type) {
		if (type.equals(String.class)) 
			return DataUtils.convertMapToString(propertyMap);
		else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				return getVisualPropertiesJSON(propertyMap);
			};
			return res;
		} else if (type.equals(Map.class)) {
			return propertyMap;
		}
		return propertyMap;
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(Map.class, String.class, JSONResult.class);
	}
}
