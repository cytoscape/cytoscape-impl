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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.task.internal.utils.DataUtils;

public class GetNetworkPropertiesTask extends AbstractPropertyTask implements ObservableTask {
	Map<String, Object> propertyMap;
	@Tunable(description="Network to get properties for", context="nogui")
	public CyNetwork network = null;

	@Tunable(description="Properties to get the value for", context="nogui")
	public String propertyList = null;

	public GetNetworkPropertiesTask(CyApplicationManager appMgr, CyNetworkViewManager viewManager,
	                                RenderingEngineManager reManager) {
		super(appMgr, viewManager, reManager);
		propertyMap = new HashMap<String, Object>();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) {
			network = appManager.getCurrentNetwork();
		}

		if (propertyList == null || propertyList.length() == 0) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Property list must be specified");
			return;
		}

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "   Property values for network "+DataUtils.getNetworkTitle(network)+":");
		String[] props = propertyList.split(",");
		for (String property: props) {
			try {
				VisualProperty vp = getProperty(network, network, property.trim());
				Object value = getPropertyValue(network, network, vp);
				if (value != null) {
					taskMonitor.showMessage(TaskMonitor.Level.INFO, "        "+vp.getDisplayName()+"="+value.toString());
					propertyMap.put(vp.getIdString(), value);
				}
			} catch (Exception e) {
				taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
				return;
			}
		}
	
	}

	public Object getResults(Class type) {
		if (type.equals(String.class)) 
			return DataUtils.convertMapToString(propertyMap);
		return propertyMap;
	}
}
