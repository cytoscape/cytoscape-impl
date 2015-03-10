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

import org.cytoscape.model.CyColumn;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListSingleSelection;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.group.internal.data.CyGroupSettingsImpl.DoubleClickAction;
import org.cytoscape.group.internal.data.aggregators.*;
import org.cytoscape.group.events.GroupAddedEvent;
import org.cytoscape.group.events.GroupAddedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyGroupViewSettings {
	CyGroupSettingsImpl settings = null;
	CyGroup group = null;

  public ListSingleSelection<DoubleClickAction> doubleClickAction = 
		new ListSingleSelection<DoubleClickAction>(DoubleClickAction.None,
		                                           DoubleClickAction.ExpandContract,
		                                           DoubleClickAction.Select);

	// We need to use getters and setters so we can update
	// our settings object
	@Tunable(description="Double-Click action", 
	         groups={"Group View Settings","User Action Settings"}, gravity=1.0)
	public ListSingleSelection<DoubleClickAction> getDCAction() {
		return doubleClickAction;
	}
	public void setDCAction(ListSingleSelection<DoubleClickAction> input) {
	}

	private boolean useNestedNetworks = false;
	@Tunable(description="Show collapsed node as a Nested Network",
	         groups={"Group View Settings"}, gravity=2.0)
	public boolean getUseNestedNetwork() {
		return useNestedNetworks;
	}
	public void setUseNestedNetwork(boolean nestedNetworks) {
		if (nestedNetworks) {
			showCompoundNode = false;
		}
		useNestedNetworks = nestedNetworks;
	}

	private boolean hideGroupNode = true;
	@Tunable(description="Hide group node on expand",
	         groups={"Group View Settings"}, gravity=3.0)
	public boolean getHideGroupNode() {
		return hideGroupNode;
	}
	public void setHideGroupNode(boolean groupNode) {
		if (groupNode) {
			showCompoundNode = false;
		}
		hideGroupNode = groupNode;
	}

	private boolean showCompoundNode = false;
	@Tunable(description="Show group as a compound node",
	         groups={"Group View Settings"}, gravity=4.0)
	public boolean getShowCompoundNode() {
		return showCompoundNode;
	}
	public void setShowCompoundNode(boolean compoundNode) {
		if (compoundNode) {
			useNestedNetworks = false;
			hideGroupNode = false;
		}
		showCompoundNode = compoundNode;
	}

/*
	@Tunable(description="Opacity of the group node", params="slider=true",
	         groups={"Group View Settings"})
	public BoundedDouble groupNodeOpacity = new BoundedDouble(0.0, 100.0, 100.0, false, false);
*/

	public CyGroupViewSettings(CyGroupSettingsImpl settings, CyGroup group) {
		this.settings = settings;
		this.group = group;

		if (group == null) {
			if (settings.getDoubleClickAction() == null) {
				doubleClickAction.setSelectedValue(DoubleClickAction.ExpandContract);
			} else {
				doubleClickAction.setSelectedValue(settings.getDoubleClickAction());
			}
	
			useNestedNetworks = settings.getUseNestedNetworks();
			hideGroupNode = settings.getHideGroupNode();
			showCompoundNode = settings.getShowCompoundNode();
		} else {
			if (settings.getDoubleClickAction(group) == null) {
				doubleClickAction.setSelectedValue(DoubleClickAction.ExpandContract);
			} else {
				doubleClickAction.setSelectedValue(settings.getDoubleClickAction(group));
			}
	
			useNestedNetworks = settings.getUseNestedNetworks(group);
			hideGroupNode = settings.getHideGroupNode(group);
			showCompoundNode = settings.getShowCompoundNode(group);
		}
	}

	public DoubleClickAction getDoubleClickAction() {
		return doubleClickAction.getSelectedValue();
	}

	public boolean useNestedNetworks() {
		return useNestedNetworks;
	}

	public boolean hideGroupNode() {
		return hideGroupNode;
	}

	public boolean showCompoundNode() {
		return showCompoundNode;
	}

/*
	public double getGroupNodeOpacity() {
		return groupNodeOpacity.getValue();
	}
*/
}
