
package org.cytoscape.group.data.internal;

import org.cytoscape.application.CyApplicationManager;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.ContainsTunables;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.data.CyGroupSettings;

import org.cytoscape.model.CyColumn;

import java.util.Map;

public class CyGroupSettingsTask extends AbstractTask {
	CyGroupAggregationManager cyAggManager;
	CyApplicationManager cyApplicationManager;
	CyGroupSettings settings;
	CyGroup group = null;

	@ContainsTunables
	public CyGroupViewSettings viewSettings;

	@ContainsTunables
	public CyGroupAggregationSettings aggregationSettings;

	public CyGroupSettingsTask(CyGroupAggregationManager aggMgr,
	                           CyApplicationManager appManager,
	                           CyGroupSettings settings,
	                           CyGroup group) {
		this.cyAggManager = aggMgr;
		this.cyApplicationManager = appManager;
		this.settings = settings;
		this.group = group;

		aggregationSettings = 
			new CyGroupAggregationSettings(cyApplicationManager, aggMgr, settings);

		viewSettings = new CyGroupViewSettings(settings);
	}

	// Our "Task" is to read the information from our settings sub-objects
	// and set the context object
	public void run (TaskMonitor taskMonitor) {
		// Update view settings
		settings.setDoubleClickAction(group, viewSettings.getDoubleClickAction());

		// Update aggregation settings (more complicated)
		boolean enabled = aggregationSettings.getAttributeAggregationEnabled();
		settings.setEnableAttributeAggregation(group, enabled);
		if (!enabled) return;
		
		// Get the default values for each class we've registered
		for (Class c: cyAggManager.getSupportedClasses()) {
			Aggregator a = aggregationSettings.getDefaultAggregator(c);
			if (group != null)
				settings.setDefaultAggregation(group, c,a);
			else
				settings.setDefaultAggregation(c,a);
		}

		// Get any overrides
		Map<CyColumn, Aggregator> overrides = aggregationSettings.getOverrideMap();
		for (CyColumn column: overrides.keySet()) {
			if (group != null)
				settings.setOverrideAggregation(group, column, overrides.get(column));
			else
				settings.setOverrideAggregation(column, overrides.get(column));
		}
	}
}
