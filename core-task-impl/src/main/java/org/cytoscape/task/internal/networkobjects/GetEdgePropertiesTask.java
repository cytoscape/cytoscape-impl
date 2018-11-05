package org.cytoscape.task.internal.networkobjects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.CoreImplDocumentationConstants;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.EdgeTunable;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.work.ContainsTunables;
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

public class GetEdgePropertiesTask extends AbstractPropertyTask implements ObservableTask {
	
	Map<CyEdge, Map<String, VisualPropertyObjectTuple>> edgePropertiesMap;

	@ContainsTunables
	public EdgeTunable edgeTunable;

	@Tunable(description = "Properties to get the value for", context = "nogui", longDescription = CoreImplDocumentationConstants.PROPERTY_LIST_LONG_DESCRIPTION, exampleStringValue = "Paint,Visible")
	public String propertyList = null;

	public GetEdgePropertiesTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		edgeTunable = new EdgeTunable(serviceRegistrar);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		edgePropertiesMap = new HashMap<CyEdge, Map<String, VisualPropertyObjectTuple>>();

		CyNetwork network = edgeTunable.getNetwork();

		if (propertyList == null || propertyList.length() == 0) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Property list must be specified");
			return;
		}

		String[] props = propertyList.split(","); // Get the list of properties

		for (CyEdge edge: edgeTunable.getEdgeList()) {
			taskMonitor.showMessage(TaskMonitor.Level.INFO, 
			                        "   Edge property values for edge "+DataUtils.getEdgeName(network.getDefaultEdgeTable(), edge)+":");
			Map<String, VisualPropertyObjectTuple> propertyMap = new HashMap<String, VisualPropertyObjectTuple>();

			for (String property: props) {
				try {
					VisualProperty vp = getProperty(network, edge, property.trim());
					Object value = getPropertyValue(network, edge, vp);
					if (value != null) {
						taskMonitor.showMessage(TaskMonitor.Level.INFO, "        "+vp.getDisplayName()+"="+value.toString());
						propertyMap.put(vp.getIdString(), new VisualPropertyObjectTuple(vp, value));
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
			 Map<CyEdge, Map<String, Object>> edgeObjectMap = edgePropertiesMap.entrySet()
				        .stream()
				        .collect(Collectors.toMap(Map.Entry::getKey,
				                                  e -> e.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e2 -> e2.getValue().object))));
			return DataUtils.convertMapToString(edgeObjectMap);
		}
		else if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				return getVisualPropertiesJSON(edgePropertiesMap);
			};
			return res;
		} else if (requestedType.equals(Map.class)) {
			return edgePropertiesMap;
		}
		return edgePropertiesMap;
	}
	
	public List<Class<?>> getResultClasses() {
	return Arrays.asList(Map.class, String.class, JSONResult.class);
	}
}
