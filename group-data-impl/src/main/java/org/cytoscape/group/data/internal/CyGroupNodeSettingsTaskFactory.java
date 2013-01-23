package org.cytoscape.group.data.internal;

/*
 * #%L
 * Cytoscape Group Data Impl (group-data-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

class CyGroupNodeSettingsTaskFactory extends AbstractNodeViewTaskFactory {
	CyGroupAggregationManager cyAggManager;
	CyGroupManager cyGroupManager;
	CyApplicationManager cyApplicationManager;
	CyGroupSettingsImpl settings;

	public CyGroupNodeSettingsTaskFactory(CyGroupManager groupManager,
	                                      CyGroupAggregationManager aggMgr, 
	                                      CyApplicationManager appManager,
	                                      CyGroupSettingsImpl settings) {
		this.settings = settings;
		this.cyAggManager = aggMgr;
		this.cyGroupManager = groupManager;
		this.cyApplicationManager = appManager;
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		CyNode node = nodeView.getModel();
		CyNetwork network = netView.getModel();
		if (cyGroupManager.isGroup(node, network))
			return true;
		else if (cyGroupManager.getGroupsForNode(node, network) != null)
			return true;
		return false;
	}

	public CyGroupSettingsImpl getSettings() { return settings; }

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, 
	                                       CyNetworkView netView) {
		CyGroup group = 
			cyGroupManager.getGroup(nodeView.getModel(), netView.getModel());

		CyGroupSettingsTask task = new CyGroupSettingsTask(cyAggManager, 
		                                                   cyApplicationManager, 
		                                                   settings, 
		                                                   group);
		return new TaskIterator(task);
	}
}
