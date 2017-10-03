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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.task.internal.utils.CoreImplDocumentationConstants;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.NodeTunable;

public class GetNodePropertiesTask extends AbstractPropertyTask implements ObservableTask {
	Map<CyNode, Map<String, VisualPropertyObjectTuple>> nodePropertiesMap;

	@ContainsTunables
	public NodeTunable nodeTunable;

	@Tunable(description="Properties to get the value for", context="nogui", longDescription=CoreImplDocumentationConstants.PROPERTY_LIST_LONG_DESCRIPTION, exampleStringValue="Paint,Visible")
	public String propertyList = null;

	public GetNodePropertiesTask(CyApplicationManager appMgr, CyNetworkViewManager viewManager,
	                             RenderingEngineManager reManager) {
		super(appMgr, viewManager, reManager);
		nodeTunable = new NodeTunable(appMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		nodePropertiesMap = new HashMap<CyNode, Map<String, VisualPropertyObjectTuple>>();

		CyNetwork network = nodeTunable.getNetwork();

		if (propertyList == null || propertyList.length() == 0) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Property list must be specified");
			return;
		}

		String[] props = propertyList.split(","); // Get the list of properties

		for (CyNode node: nodeTunable.getNodeList()) {
			taskMonitor.showMessage(TaskMonitor.Level.INFO, 
			                        "   Node property values for node "+DataUtils.getNodeName(network.getDefaultNodeTable(), node)+":");
			Map<String, VisualPropertyObjectTuple> propertyMap = new HashMap<String, VisualPropertyObjectTuple>();

			for (String property: props) {
				try {
					VisualProperty vp = getProperty(network, node, property.trim());
					Object value = getPropertyValue(network, node, vp);
					if (value != null) {
						taskMonitor.showMessage(TaskMonitor.Level.INFO, "        "+vp.getDisplayName()+"="+value.toString());
						propertyMap.put(vp.getIdString(), new VisualPropertyObjectTuple(vp,value));
					}
				} catch (Exception e) {
					taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
					return;
				}
			}
			nodePropertiesMap.put(node, propertyMap);
		}
	}

	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) {
			 Map<CyNode, Map<String, Object>> nodeObjectMap = nodePropertiesMap.entrySet()
				        .stream()
				        .collect(Collectors.toMap(Map.Entry::getKey,
				                                  e -> e.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e2 -> e2.getValue().object))));
			return DataUtils.convertMapToString(nodeObjectMap);
		} else if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				return getVisualPropertiesJSON(nodePropertiesMap);
			};
			return res;
		} else if (requestedType.equals(Map.class)) {
			return nodePropertiesMap;
		}
		return nodePropertiesMap;
	}

	public List<Class<?>> getResultClasses() {
		return Arrays.asList(Map.class, String.class, JSONResult.class);
	}
}
