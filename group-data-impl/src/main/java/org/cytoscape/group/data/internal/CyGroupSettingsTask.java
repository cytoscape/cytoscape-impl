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

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.ContainsTunables;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.CyGroupAggregationManager;

import org.cytoscape.model.CyColumn;

import java.util.Map;

public class CyGroupSettingsTask extends AbstractTask {
	CyGroupAggregationManager cyAggManager;
	CyApplicationManager cyApplicationManager;
	CyGroupSettingsImpl settings;
	CyGroup group = null;

	@ContainsTunables
	public CyGroupViewSettings viewSettings;

	@ContainsTunables
	public CyGroupAggregationSettings aggregationSettings;

	public CyGroupSettingsTask(CyGroupAggregationManager aggMgr,
	                           CyApplicationManager appManager,
	                           CyGroupSettingsImpl settings,
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
		settings.setUseNestedNetworks(group, viewSettings.getUseNestedNetworks());
		settings.setHideGroupNode(group, viewSettings.getHideGroupNode());
		// settings.setGroupNodeOpacity(group, viewSettings.getGroupNodeOpacity());

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
	
	@ProvidesTitle
	public String getTitle() {
		return "Group Preferences";
	}
}
