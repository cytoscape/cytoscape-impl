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
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.NodeAndEdgeTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class RemoveFromGroupTask extends AbstractGroupTask {

	@Tunable (description="Group", context="nogui")
	public String groupName;

	@ContainsTunables
	public NodeAndEdgeTunable nodesAndEdges;

	public RemoveFromGroupTask(final CyServiceRegistrar serviceRegistrar) {
		groupMgr = serviceRegistrar.getService(CyGroupManager.class);
		nodesAndEdges = new NodeAndEdgeTunable(serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		net = nodesAndEdges.getNetwork();

		if (groupName == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Group must be specified");
			return;
		}

		CyGroup grp = getGroup(groupName);
		if (grp == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Can't find group '"+groupName+"' in network: "+net.toString());
			return;
		}

		List<CyEdge> edgeList = nodesAndEdges.getEdgeList(false);
		List<CyNode> nodeList = nodesAndEdges.getNodeList(false);
		if (edgeList == null && nodeList == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Nothing to remove");
			return;
		}

		int edges = 0;
		if (edgeList != null)
			edges = edgeList.size();

		int nodes = 0;
		if (nodeList != null)
			nodes = nodeList.size();

		tm.showMessage(TaskMonitor.Level.INFO, "Removing "+nodes+" nodes and "+edges+" edges from group "+getGroupDesc(grp));

		if (edgeList != null && !edgeList.isEmpty()) {
			grp.removeEdges(edgeList);
		}
		if (nodeList != null && !nodeList.isEmpty()) {
			grp.removeNodes(nodeList);
		}

		tm.showMessage(TaskMonitor.Level.INFO, "Removed "+nodes+" nodes and "+edges+" edges from group "+getGroupDesc(grp));
	}

}
