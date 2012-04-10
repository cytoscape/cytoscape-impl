package org.cytoscape.group.data.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.data.CyGroupSettings;

class CyGroupSettingsTaskFactory extends AbstractTaskFactory {
	CyGroupAggregationManager cyAggManager;
	CyApplicationManager cyApplicationManager;
	CyGroupSettings settings;

	public CyGroupSettingsTaskFactory(CyGroupAggregationManager aggMgr, 
	                                  CyApplicationManager appManager,
	                                  CyGroupSettings settings) {
		this.settings = settings;
		this.cyAggManager = aggMgr;
		this.cyApplicationManager = appManager;
	}

	public CyGroupSettings getSettings() { return settings; }

	public TaskIterator createTaskIterator() {
		CyGroupSettingsTask task = new CyGroupSettingsTask(cyAggManager, 
		                                                   cyApplicationManager, 
		                                                   settings, null);
		return new TaskIterator(task);
	}
}
