package org.cytoscape.group.data.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.data.CyGroupSettings;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

class CyGroupNodeSettingsTaskFactory extends AbstractNodeViewTaskFactory {
	CyGroupAggregationManager cyAggManager;
	CyGroupManager cyGroupManager;
	CyApplicationManager cyApplicationManager;
	CyGroupSettings settings;

	public CyGroupNodeSettingsTaskFactory(CyGroupManager groupManager,
	                                      CyGroupAggregationManager aggMgr, 
	                                      CyApplicationManager appManager,
	                                      CyGroupSettings settings) {
		this.settings = settings;
		this.cyAggManager = aggMgr;
		this.cyGroupManager = groupManager;
		this.cyApplicationManager = appManager;
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		if (cyGroupManager.isGroup(nodeView.getModel(), netView.getModel()))
			return true;
		return false;
	}

	public CyGroupSettings getSettings() { return settings; }

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
