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

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.EdgeTunable;

public class GetEdgePropertiesTask extends AbstractPropertyTask implements ObservableTask {
	Map<CyEdge, Map<String, Object>> edgePropertiesMap;

	@ContainsTunables
	public EdgeTunable edgeTunable;

	@Tunable(description="Properties to get the value for", context="nogui")
	public String propertyList = null;

	public GetEdgePropertiesTask(CyApplicationManager appMgr, CyNetworkViewManager viewManager,
	                             RenderingEngineManager reManager) {
		super(appMgr, viewManager, reManager);
		edgeTunable = new EdgeTunable(appMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		edgePropertiesMap = new HashMap<CyEdge, Map<String, Object>>();

		CyNetwork network = edgeTunable.getNetwork();

		if (propertyList == null || propertyList.length() == 0) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Property list must be specified");
			return;
		}

		String[] props = propertyList.split(","); // Get the list of properties

		for (CyEdge edge: edgeTunable.getEdgeList()) {
			taskMonitor.showMessage(TaskMonitor.Level.INFO, 
			                        "   Edge property values for edge "+DataUtils.getEdgeName(network.getDefaultEdgeTable(), edge)+":");
			Map<String, Object> propertyMap = new HashMap<String, Object>();

			for (String property: props) {
				try {
					VisualProperty vp = getProperty(network, edge, property.trim());
					Object value = getPropertyValue(network, edge, vp);
					if (value != null) {
						taskMonitor.showMessage(TaskMonitor.Level.INFO, "        "+vp.getDisplayName()+"="+value.toString());
						propertyMap.put(vp.getIdString(), value);
					}
				} catch (Exception e) {
					taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
					return;
				}
			}
			edgePropertiesMap.put(edge, propertyMap);
		}
	}

	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) {
			return DataUtils.convertMapToString(edgePropertiesMap);
		}
		return edgePropertiesMap;
	}
	
}
