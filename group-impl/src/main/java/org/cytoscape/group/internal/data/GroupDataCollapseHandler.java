package org.cytoscape.group.internal.data;

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

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.events.GroupAboutToCollapseEvent;
import org.cytoscape.group.events.GroupAboutToCollapseListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;

import org.cytoscape.group.internal.CyGroupImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the view portion of group collapse/expand
 */
public class GroupDataCollapseHandler implements GroupAboutToCollapseListener
{

	private final CyGroupManager cyGroupManager;
	private final CyGroupSettingsImpl cyGroupSettings;
	private static final Logger logger = 
		LoggerFactory.getLogger(GroupDataCollapseHandler.class);

	/**
	 * 
	 * @param cyEventHelper
	 */
	public GroupDataCollapseHandler(final CyGroupManager groupManager, 
	                                final CyGroupSettingsImpl groupSettings) {
		this.cyGroupManager = groupManager;
		this.cyGroupSettings = groupSettings;
	}

	public void handleEvent(GroupAboutToCollapseEvent e) {
		CyNetwork network = e.getNetwork();
		CyGroup group = e.getSource();
		
		if (e.collapsing()) {
			// Are we aggregating
			if (!cyGroupSettings.getEnableAttributeAggregation(group))
				return;

			// Yup -- all of our information is in the settings...
			CyTable nodeTable = network.getDefaultNodeTable();
			for (CyColumn column: nodeTable.getColumns()) {
				Class type = column.getType();
				if (column.isPrimaryKey()) continue;

				// Skip over our own columns
				if (CyGroupSettingsImpl.AGGREGATION_SETTINGS.equals(column.getName()))
					continue;
				if (CyGroupSettingsImpl.AGGREGATION_OVERRIDE_SETTINGS.equals(column.getName()))
					continue;
				if (CyGroupSettingsImpl.VIEW_SETTINGS.equals(column.getName()))
					continue;
				if (CyGroupImpl.CHILDREN_ATTR.equals(column.getName()))
					continue;
				if (CyGroupImpl.DESCENDENTS_ATTR.equals(column.getName()))
					continue;

				// Don't aggregate the name or shared name columns by default
				if (CyNetwork.NAME.equals(column.getName()) && 
				    cyGroupSettings.getOverrideAggregation(group, column) == null) {
					continue;
				}
				if (CyRootNetwork.SHARED_NAME.equals(column.getName()) && 
				    cyGroupSettings.getOverrideAggregation(group, column) == null) {
					continue;
				}

				// Do we have an override for this group and column?
				Aggregator agg = cyGroupSettings.getAggregator(group, column);
				if (agg == null) continue;

				// OK, aggregate
				agg.aggregate(nodeTable, group, column);
			}
		}
	}
}
