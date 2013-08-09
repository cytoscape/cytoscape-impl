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
import org.cytoscape.group.CyGroupManager;

import org.cytoscape.model.CyNetwork;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import org.cytoscape.task.internal.utils.DataUtils;

public class ListGroupsTask extends AbstractGroupTask implements ObservableTask {
	private List<CyGroup> groups;
	CyApplicationManager appMgr;

	@Tunable (description="Network", context="nogui")
	public CyNetwork network;

	public ListGroupsTask(CyApplicationManager appMgr, CyGroupManager manager) {
		this.groupMgr = manager;
		this.appMgr = appMgr;
	}

	public void run(TaskMonitor tm) throws Exception {
		if (network == null) {
			network = appMgr.getCurrentNetwork();
		}

		net = network;

		groups = getGroupList(tm, "all"); 
		if (groups == null)
			return;

		tm.showMessage(TaskMonitor.Level.INFO, "Groups in network: "+network);
		for (CyGroup group: groups) {
			tm.showMessage(TaskMonitor.Level.INFO, "    "+getGroupDesc(group));
		}
		
		tm.setProgress(1.0d);
	}
	
	public Object getResults(Class requestedType) {
		if (groups == null) return null;
		if (requestedType.equals(String.class)) {
			return DataUtils.convertData(groups);
		}
		return groups;
	}
}
