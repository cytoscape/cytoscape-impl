
package org.cytoscape.group.data.internal;

import org.cytoscape.model.CyColumn;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.ContainsTunables;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.group.data.CyGroupSettings;
import org.cytoscape.group.events.GroupAddedEvent;
import org.cytoscape.group.events.GroupAddedListener;

public class CyGroupSettingsImpl extends AbstractTask implements CyGroupSettings, GroupAddedListener {
	CyGroupManager cyGroupManager;

	@ContainsTunables
	public CyGroupViewSettings viewSettings = new CyGroupViewSettings();

	@ContainsTunables
	public CyGroupAggregationSettings aggregationSettings = new CyGroupAggregationSettings();

	public CyGroupSettingsImpl(CyGroupManager mgr) {
		this.cyGroupManager = mgr;
	}

	// This is a little funky, but we don't really have a task, so we just provide the run method
	// and do nothing
	public void run (TaskMonitor taskMonitor) {}

	@Override
	public boolean getEnableAttributeAggregation() {
		return aggregationSettings.getEnableAttributeAggregation();
	}

	@Override
	public void setEnableAttributeAggregation(boolean aggregate) {
		aggregationSettings.setEnableAttributeAggregation(aggregate);
	}

	@Override
	public DoubleClickAction getDoubleClickAction() {
		return viewSettings.getDoubleClickAction();
	}

	@Override
	public void setDoubleClickAction(DoubleClickAction action) {
		viewSettings.setDoubleClickAction(action);
	}

	@Override
	public Aggregator getAggregator(CyGroup group, CyColumn column) {
		return aggregationSettings.getAggregator(group, column);
	}

	@Override
	public void setDefaultAggregation(CyGroup group, Class ovClass, Aggregator agg) {
		aggregationSettings.setDefaultAggregation(group, ovClass, agg);
	}

	@Override
	public void setDefaultAggregation(Class ovClass, Aggregator agg) {
		aggregationSettings.setDefaultAggregation(ovClass, agg);
	}

	@Override
	public void setOverrideAggregation(CyGroup group, CyColumn column, Aggregator agg) {
		aggregationSettings.setOverrideAggregation(group, column, agg);
	}

	@Override
	public void setOverrideAggregation(CyColumn column, Aggregator agg) {
		aggregationSettings.setOverrideAggregation(column, agg);
	}

	public void handleEvent(GroupAddedEvent e) {
		CyGroup addedGroup = e.getGroup();
		aggregationSettings.groupAdded(addedGroup);
	}
}
