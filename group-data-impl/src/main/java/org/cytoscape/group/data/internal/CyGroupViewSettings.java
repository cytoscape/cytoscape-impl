
package org.cytoscape.group.data.internal;

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
import org.cytoscape.group.data.CyGroupSettings;
import org.cytoscape.group.data.CyGroupSettings.DoubleClickAction;
import org.cytoscape.group.data.internal.aggregators.*;
import org.cytoscape.group.events.GroupAddedEvent;
import org.cytoscape.group.events.GroupAddedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyGroupViewSettings {
	CyGroupSettings settings = null;

  public ListSingleSelection<DoubleClickAction> doubleClickAction = 
		new ListSingleSelection<DoubleClickAction>(DoubleClickAction.None,
		                                           DoubleClickAction.ExpandContract,
		                                           DoubleClickAction.Select);

	// We need to use getters and setters so we can update
	// our settings object
	@Tunable(description="Double-Click action", 
	         groups={"Group View Settings","User Action Settings"})
	public ListSingleSelection<DoubleClickAction> getDCAction() {
		return doubleClickAction;
	}
	public void setDCAction(ListSingleSelection<DoubleClickAction> input) {
	}

	@Tunable(description="Show collapsed node as a Nested Network",
	         groups={"Group View Settings"})
	public boolean useNestedNetworks = false;

	@Tunable(description="Hide group node on expand",
	         groups={"Group View Settings"})
	public boolean hideGroupNode = true;

	@Tunable(description="Opacity of the group node", params="slider=true",
	         groups={"Group View Settings"})
	public BoundedDouble groupNodeOpacity = new BoundedDouble(0.0, 100.0, 100.0, false, false);

	public CyGroupViewSettings(CyGroupSettings settings) {
		this.settings = settings;

		if (settings.getDoubleClickAction() == null) {
			doubleClickAction.setSelectedValue(DoubleClickAction.ExpandContract);
		} else {
			doubleClickAction.setSelectedValue(settings.getDoubleClickAction());
		}
	}

	public DoubleClickAction getDoubleClickAction() {
		return doubleClickAction.getSelectedValue();
	}

	public boolean getUseNestedNetworks() {
		return useNestedNetworks;
	}

	public boolean getHideGroupNode() {
		return hideGroupNode;
	}

	public double getGroupNodeOpacity() {
		return groupNodeOpacity.getValue();
	}
}
