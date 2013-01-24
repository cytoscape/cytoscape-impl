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
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

public class UnGroupNodesTask extends AbstractTask {
	private CyNetwork net;
	private CyGroupManager mgr;
	private CyGroupFactory factory;
	private	Set<CyGroup>groupSet = null;
	private UndoSupport undoSupport;

	public UnGroupNodesTask(UndoSupport undoSupport, CyNetwork net, CyGroupFactory factory,
	                        Set<CyGroup>groups, CyGroupManager mgr) {
		if (net == null)
			throw new NullPointerException("network is null");
		this.net = net;
		this.mgr = mgr;
		this.factory = factory;
		this.groupSet = groups;
		this.undoSupport = undoSupport;
	}

	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);

		GroupEdit edit = new GroupEdit(net, mgr, factory, groupSet);
		for (CyGroup group: groupSet) {
			mgr.destroyGroup(group);
			tm.setProgress(1.0d/(double)groupSet.size());
		}
		undoSupport.postEdit(edit);
		tm.setProgress(1.0d);
	}
}
