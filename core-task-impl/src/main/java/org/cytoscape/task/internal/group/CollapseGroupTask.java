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

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;

import org.cytoscape.view.model.CyNetworkView;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CollapseGroupTask extends AbstractTask {
	private CyNetwork net;
	private CyGroupManager mgr;
	private List<CyGroup> groups;
	private boolean collapse;

	public CollapseGroupTask(CyNetwork net, List<CyGroup> groups, CyGroupManager manager, boolean collapse) {
		if (net == null)
			throw new NullPointerException("network is null");
		if (groups == null || groups.size() == 0)
			throw new NullPointerException("group list is null");
		this.net = net;
		this.mgr = manager;
		this.groups = groups;
		this.collapse = collapse;
	}

	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		for (CyGroup group: groups) {
			if (collapse)
				group.collapse(net);
			else
				group.expand(net);
			tm.setProgress(1.0d/groups.size());
		}
		tm.setProgress(1.0d);
	}
}
