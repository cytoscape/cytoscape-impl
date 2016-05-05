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
import org.cytoscape.group.CyGroupSettingsManager.DoubleClickAction;
import org.cytoscape.group.CyGroupSettingsManager.GroupViewType;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class CyGroupViewSettings {
	
	CyGroupSettingsImpl settings = null;
	CyGroup group = null;

  public ListSingleSelection<DoubleClickAction> doubleClickAction =
		  new ListSingleSelection<>(DoubleClickAction.NONE,
				  DoubleClickAction.EXPANDCONTRACT,
				  DoubleClickAction.SELECT);

	// We need to use getters and setters so we can update
	// our settings object
	@Tunable(description="Double-Click action:", 
	         groups={"Group View Settings"}, gravity=1.0)
	public ListSingleSelection<DoubleClickAction> getDCAction() {
		return doubleClickAction;
	}
	public void setDCAction(ListSingleSelection<DoubleClickAction> input) {
	}

  public ListSingleSelection<GroupViewType> groupViewType =
		  new ListSingleSelection<>(GroupViewType.NONE,
				  GroupViewType.COMPOUND,
				  GroupViewType.SHOWGROUPNODE,
				  GroupViewType.SINGLENODE);

	// We need to use getters and setters so we can update
	// our settings object
	@Tunable(description="Visualization for group:", 
	         groups={"Group View Settings"}, gravity=2.0, dependsOn="useNestedNetworks=false")
	public ListSingleSelection<GroupViewType> getGVtype() {
		return groupViewType;
	}
	public void setGVtype(ListSingleSelection<GroupViewType> input) {
	}

	private boolean useNestedNetworks = false;
	@Tunable(description="Show collapsed node as a Nested Network:",
	         groups={"Group View Settings"}, gravity=3.0)
	public boolean getUseNestedNetwork() {
		return useNestedNetworks;
	}
	public void setUseNestedNetwork(boolean nestedNetworks) {
		useNestedNetworks = nestedNetworks;
		if (useNestedNetworks)
			groupViewType.setSelectedValue(GroupViewType.NONE);
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
				doubleClickAction.setSelectedValue(DoubleClickAction.EXPANDCONTRACT);
			} else {
				doubleClickAction.setSelectedValue(settings.getDoubleClickAction());
			}

			if (settings.getGroupViewType() == null) {
				groupViewType.setSelectedValue(GroupViewType.NONE);
			} else {
				groupViewType.setSelectedValue(settings.getGroupViewType());
			}
	
			useNestedNetworks = settings.getUseNestedNetworks();
		} else {
			if (settings.getDoubleClickAction(group) == null) {
				doubleClickAction.setSelectedValue(DoubleClickAction.EXPANDCONTRACT);
			} else {
				doubleClickAction.setSelectedValue(settings.getDoubleClickAction(group));
			}

			if (settings.getGroupViewType(group) == null) {
				groupViewType.setSelectedValue(GroupViewType.NONE);
			} else {
				groupViewType.setSelectedValue(settings.getGroupViewType(group));
			}
	
			useNestedNetworks = settings.getUseNestedNetworks(group);
		}
	}

	public DoubleClickAction getDoubleClickAction() {
		return doubleClickAction.getSelectedValue();
	}

	public GroupViewType getGroupViewType() {
		return groupViewType.getSelectedValue();
	}

	public boolean useNestedNetworks() {
		return useNestedNetworks;
	}

/*
	public double getGroupNodeOpacity() {
		return groupNodeOpacity.getValue();
	}
*/
}
