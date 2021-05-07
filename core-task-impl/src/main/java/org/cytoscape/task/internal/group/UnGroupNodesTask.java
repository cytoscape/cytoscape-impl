package org.cytoscape.task.internal.group;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.NodeTunable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 - 2021 The Cytoscape Consortium
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

public class UnGroupNodesTask extends AbstractGroupTask implements ObservableTask {
	
	@ContainsTunables
	public NodeTunable nodeTunable;
	
	private Set<CyGroup> groupSet;
	private CyNetworkView netView;

	public UnGroupNodesTask(
			CyNetwork net,
			Set<CyGroup> groups,
			CyNetworkView netView,
			CyServiceRegistrar serviceRegistrar
	) {
		super(serviceRegistrar);
		
		if (net == null)
			throw new NullPointerException("network is null");

		this.net = net;
		this.netView = netView;
		this.groupSet = groups;
	}

	public UnGroupNodesTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		nodeTunable = new NodeTunable(serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		
		// Are we operating in a command-mode?
		if (nodeTunable != null) {
			net = nodeTunable.getNetwork();
			groupSet = getGroups(net, nodeTunable.getNodeList());
		}

		UndoSupport undoSupport = serviceRegistrar.getService(UndoSupport.class);
		GroupEdit edit = new GroupEdit(groupSet, serviceRegistrar);
		CyGroupManager groupMgr = serviceRegistrar.getService(CyGroupManager.class);

		for (CyGroup group : groupSet) {
			groupMgr.destroyGroup(group);
			tm.setProgress(1.0d / (double) groupSet.size());
		}
		
		undoSupport.postEdit(edit);

		if (netView != null)
			netView.updateView();

		tm.showMessage(TaskMonitor.Level.INFO, "Ungrouped " + groupSet.size() + " groups");
		tm.setProgress(1.0d);
	}
	
	@Override
	public List<Class<?>> getResultClasses() {	return Arrays.asList(String.class, JSONResult.class);	}
	
	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class))			return "Ungrouped: "+DataUtils.convertData(groupSet);
		if (requestedType.equals(JSONResult.class))   {
			JSONResult res = () -> {	 return "{\"groups\": ["  + getGroupSetString(groupSet) + "]}"; };
			return res;
		}
		return null;
	}
}
