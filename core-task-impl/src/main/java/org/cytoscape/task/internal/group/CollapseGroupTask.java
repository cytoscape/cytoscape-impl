package org.cytoscape.task.internal.group;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
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
 * Copyright (C) 2012 - 2019 The Cytoscape Consortium
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

public class CollapseGroupTask extends AbstractGroupTask implements ObservableTask {
	
	private List<CyGroup> groups;
	private boolean collapse;

	@Tunable (description="Network", context="nogui", longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network;

	@Tunable (description="List of groups", context="nogui", longDescription=StringToModel.GROUP_LIST_LONG_DESCRIPTION, exampleStringValue=StringToModel.CY_NODE_LIST_EXAMPLE_STRING)
	public String groupList;

	public CollapseGroupTask(CyNetwork net, List<CyGroup> groups, boolean collapse, CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		this.net = net;
		this.groups = groups;
		this.collapse = collapse;
	}

	public CollapseGroupTask(CyGroupManager manager, boolean collapse, CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		this.net = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
		this.collapse = collapse;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (network != null) {
			net = network;
			if (network == null) {
				tm.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
				return;
			}
		}
		
		if (groups == null && groupList == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "List of groups must be specified");
			return;
		}

		if (groups == null)
			groups = getGroupList(tm, groupList);

		if (groups == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Can't find group "+groupList);
			return;
		}

		tm.setProgress(0.0);
		int collapsed = 0;
		for (CyGroup group: groups) {
			if (collapse) {
				if (group.isInNetwork(net) && !group.isCollapsed(net)) {
					collapsed++;
					group.collapse(net);
				}
			} else {
				if (group.isInNetwork(net) && group.isCollapsed(net)) {
					collapsed++;
					group.expand(net);
				}
			}
			tm.setProgress(1.0d/groups.size());
		}

		if (collapse)
			tm.showMessage(TaskMonitor.Level.INFO, "Collapsed "+collapsed+" groups");
		else
			tm.showMessage(TaskMonitor.Level.INFO, "Expanded "+collapsed+" groups");

		tm.setProgress(1.0d);
	}
	public List<Class<?>> getResultClasses() {	return Arrays.asList(String.class, JSONResult.class, List.class);	}
	public Object getResults(Class requestedType) {
		if (requestedType.equals(List.class))			return groups;
		if (requestedType.equals(String.class)) {
			if (collapse) {
				return "Collapsed groups: "+DataUtils.convertData(groups);
			} else {
				return "Expanded groups: "+DataUtils.convertData(groups);
			}
		}
		if (requestedType.equals(JSONResult.class))  
		{ 
			JSONResult res = () -> { 
				return "{\"groups\": ["+getGroupSetString(groups)+"]}"; 
			};
			return res;
		}
		return null;
	}

}
