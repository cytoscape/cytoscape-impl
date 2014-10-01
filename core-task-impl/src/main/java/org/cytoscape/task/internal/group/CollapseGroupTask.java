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
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CollapseGroupTask extends AbstractGroupTask {
	private List<CyGroup> groups;
	private boolean collapse;
	private CyNetworkViewManager viewMgr;
	private VisualMappingManager styleManager;

	@Tunable (description="Network", context="nogui")
	public CyNetwork network;

	@Tunable (description="List of groups", context="nogui")
	public String groupList;

	public CollapseGroupTask(CyNetwork net, List<CyGroup> groups, CyNetworkViewManager viewManager,
							 VisualMappingManager styleManager, CyGroupManager manager, boolean collapse) {
		this.net = net;
		this.groupMgr = manager;
		this.viewMgr = viewManager;
		this.styleManager = styleManager;
		this.groups = groups;
		this.collapse = collapse;
	}

	public CollapseGroupTask(CyApplicationManager appMgr, CyNetworkViewManager viewManager, 
							 VisualMappingManager styleManager, CyGroupManager manager, boolean collapse) {
		this.net = appMgr.getCurrentNetwork();
		this.groupMgr = manager;
		this.viewMgr = viewManager;
		this.styleManager = styleManager;
		this.collapse = collapse;
	}

	public void run(TaskMonitor tm) throws Exception {
		if (network != null)
			net = network;

		if (groups == null && groupList == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "List of groups must be specified");
			return;
		}

		if (groups == null)
			groups = getGroupList(tm, groupList);

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

		for (CyNetworkView view: viewMgr.getNetworkViews(net)) {
			VisualStyle style = styleManager.getVisualStyle(view);
			style.apply(view);
			view.updateView();
		}
		tm.setProgress(1.0d);
	}

}
