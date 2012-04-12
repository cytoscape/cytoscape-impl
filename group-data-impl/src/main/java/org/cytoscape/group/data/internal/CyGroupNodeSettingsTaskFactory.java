package org.cytoscape.group.data.internal;

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
