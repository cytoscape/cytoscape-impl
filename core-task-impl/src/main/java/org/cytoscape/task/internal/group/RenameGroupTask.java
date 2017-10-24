package org.cytoscape.task.internal.group;

import java.util.Arrays;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 - 2013 The Cytoscape Consortium
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

import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class RenameGroupTask extends AbstractGroupTask {

	private CyServiceRegistrar serviceRegistrar;

	@Tunable (description="Network", context="nogui", longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network;

	@Tunable (description="Group to rename", context="nogui", longDescription=StringToModel.GROUP_NAME_LONG_DESCRIPTION, exampleStringValue=StringToModel.GROUP_NAME_EXAMPLE_STRING)
	public String groupName;

	@Tunable (description="New name", context="nogui", longDescription="Specifies the NEW name used to identify the group. ", exampleStringValue=StringToModel.GROUP_NAME_EXAMPLE_STRING2)
	public String newName;

	public RenameGroupTask(CyApplicationManager appMgr, CyGroupManager manager, CyServiceRegistrar reg) {
		this.net = appMgr.getCurrentNetwork();
		this.groupMgr = manager;
		serviceRegistrar = reg;
	}

	public void run(TaskMonitor tm) throws Exception {
		if (network != null)
			net = network;

		if (groupName == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Group must be specified");
			return;
		}

		if (newName == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "New name must be specified");
			return;
		}

		CyGroup grp = getGroup(groupName);
		if (grp == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Can't find group '"+groupName+"' in network: "+net.toString());
			return;
		}
		CyRow groupRow = ((CySubNetwork)net).getRootNetwork().getRow(grp.getGroupNode(), CyRootNetwork.SHARED_ATTRS);
		String oldName = groupRow.get(CyRootNetwork.SHARED_NAME, String.class);
		groupRow.set(CyRootNetwork.SHARED_NAME, newName);

		tm.showMessage(TaskMonitor.Level.INFO, "Renamed group from "+oldName+" to "+newName);
	}
	public List<Class<?>> getResultClasses() {	return Arrays.asList(String.class, CyGroup.class, JSONResult.class);	}
	public Object getResults(Class requestedType) {
		if (requestedType.equals(CyGroup.class))		return getGroup(groupName);
		if (requestedType.equals(String.class))			return groupName;
		if (requestedType.equals(JSONResult.class))  	
		{	JSONResult res = () -> {return groupName; };
			return res;
		}		
		return null;
	}

}
