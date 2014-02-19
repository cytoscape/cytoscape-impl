package org.cytoscape.task.internal.group;

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
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

import org.cytoscape.view.model.CyNetworkView;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import org.cytoscape.task.internal.utils.DataUtils;

public class RenameGroupTask extends AbstractGroupTask {

	@Tunable (description="Network", context="nogui")
	public CyNetwork network;

	@Tunable (description="Group to rename", context="nogui")
	public String groupName;

	@Tunable (description="New name", context="nogui")
	public String newName;

	public RenameGroupTask(CyApplicationManager appMgr, CyGroupManager manager) {
		this.net = appMgr.getCurrentNetwork();
		this.groupMgr = manager;
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

}
