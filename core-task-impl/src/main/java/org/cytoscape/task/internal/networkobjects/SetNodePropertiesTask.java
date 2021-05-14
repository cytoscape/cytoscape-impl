package org.cytoscape.task.internal.networkobjects;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.CoreImplDocumentationConstants;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.NodeTunable;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.work.ContainsTunables;
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

public class SetNodePropertiesTask extends AbstractPropertyTask {

	@ContainsTunables
	public NodeTunable nodeTunable;

	@Tunable(description="Properties to get the value for", context="nogui", longDescription=CoreImplDocumentationConstants.PROPERTY_LIST_LONG_DESCRIPTION, exampleStringValue="Paint, Visible")
	public String propertyList = null;

	@Tunable(description="Values to set for the properties", context="nogui", longDescription=CoreImplDocumentationConstants.VALUE_LIST_LONG_DESCRIPTION, exampleStringValue="#808080,true")
	public String valueList = null;

	@Tunable(description="Whether or not to lock the property", context="nogui", 
	         longDescription="Locking a visual property will override any mappings.  This is the same as the ```Bypass``` column in the user interface",
	         exampleStringValue="true")
	public boolean bypass;

	public SetNodePropertiesTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		nodeTunable = new NodeTunable(serviceRegistrar);
	}

	@Override
	public void run(final TaskMonitor tm) {
		CyNetwork network = nodeTunable.getNetwork();

		if (propertyList == null || propertyList.length() == 0) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Property list must be specified");
			return;
		}

		if (valueList == null || valueList.length() == 0) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Value list must be specified");
			return;
		}

		String[] props = propertyList.split(",");
		String[] values = DataUtils.getCSV(valueList);
		if (props.length != values.length) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Property list and value list are not the same length");
			return;
		}

		for (CyNode node: nodeTunable.getNodeList()) {
			tm.showMessage(TaskMonitor.Level.INFO,
			                        "   Setting properties for node "+DataUtils.getNodeName(network.getDefaultNodeTable(), node));
			int valueIndex = 0;
			for (String property: props) {
				String value = values[valueIndex];
				valueIndex++;
				try {
					VisualProperty vp = getProperty(network, node, property.trim());
					setPropertyValue(network, node, vp, value, bypass);
					tm.showMessage(TaskMonitor.Level.INFO, "       "+vp.getDisplayName()+" set to "+value.toString());
				} catch (Exception e) {
					tm.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
					return;
				}
			}
		}
	
	}

	public Object getResults(Class type) {
		if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				return "{}";
			};
			return res;
		}
		return null;
	}

	public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class);
	}
}
