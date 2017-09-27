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
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.undo.UndoSupport;

import org.cytoscape.task.internal.utils.NodeTunable;

public class UnGroupNodesTask extends AbstractGroupTask {
	private CyApplicationManager appMgr = null;
	private CyGroupFactory factory = null;
	private	Set<CyGroup>groupSet = null;
	private UndoSupport undoSupport = null;
	private CyNetworkView netView = null;

	@ContainsTunables
	public NodeTunable nodeTunable = null;

	public UnGroupNodesTask(UndoSupport undoSupport, CyNetwork net, CyGroupFactory factory,
	                        Set<CyGroup>groups, CyGroupManager mgr, CyNetworkView netView) {
		if (net == null)
			throw new NullPointerException("network is null");
		this.net = net;
		this.netView = netView;
		this.groupMgr = mgr;
		this.factory = factory;
		this.groupSet = groups;
		this.undoSupport = undoSupport;
	}

	public UnGroupNodesTask(CyApplicationManager appMgr, CyGroupManager mgr) {
		nodeTunable = new NodeTunable(appMgr);
		this.groupMgr = mgr;
	}

	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		// Are we operating in a command-mode?
		if (nodeTunable != null) {
			net = nodeTunable.getNetwork();
			groupSet = getGroups(net, nodeTunable.getNodeList());
		}

		GroupEdit edit = null;
		if (undoSupport != null)
			edit = new GroupEdit(net, groupMgr, factory, groupSet);

		for (CyGroup group: groupSet) {
			groupMgr.destroyGroup(group);
			tm.setProgress(1.0d/(double)groupSet.size());
		}
		if (undoSupport != null)
			undoSupport.postEdit(edit);

		if (netView != null)
			netView.updateView();

		tm.showMessage(TaskMonitor.Level.INFO, "Ungrouped "+groupSet.size()+" groups");
		tm.setProgress(1.0d);
	}
	public List<Class<?>> getResultClasses() {	return Arrays.asList(String.class, JSONResult.class);	}
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class))			return getGroupSetString();
		if (requestedType.equals(JSONResult.class))   {
			JSONResult res = () -> {	 return "{"  + getGroupSetString() + "}"; };
			return res;
		}
		return null;
	}
	
	String getGroupSetString()
	{
		StringBuilder buffer = new StringBuilder();
		for (CyGroup group : groupSet)
			buffer.append(group.getGroupNode().getSUID()).append(" ");
		String out = buffer.toString();
		return out.substring(0, out.length()-1);
	}

}
