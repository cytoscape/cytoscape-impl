
package org.cytoscape.group.data.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.ContainsTunables;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.data.CyGroupSettings;
import org.cytoscape.group.events.GroupAddedEvent;
import org.cytoscape.group.events.GroupAddedListener;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides the context for both the global group settings and the
 * group-specific group settings.
 */
public class CyGroupSettingsImpl implements CyGroupSettings, GroupAddedListener {
	CyGroupManager cyGroupManager;
	CyGroupAggregationManager cyAggManager;
	CyApplicationManager cyApplicationManager;

	Map<Class, Aggregator> allGroupDefaultMap;
	Map<CyColumn, Aggregator> allGroupOverrideMap;
	Map<CyGroup, GroupSpecificMaps> groupMap;
	Map<CyGroup, DoubleClickAction> groupActionMap;
	Map<CyGroup, Boolean> enableMap;
	Map<CyGroup, Boolean> nestedNetworkMap;
	Map<CyGroup, Boolean> hideGroupMap;
	Map<CyGroup, Double> opacityMap;
	DoubleClickAction action = DoubleClickAction.ExpandContract;
	boolean enableAttributeAggregation = false;
	boolean useNestedNetworks = false;
	boolean hideGroupNode = true;
	double groupNodeOpacity = 100.0;

	public CyGroupSettingsImpl(CyGroupManager mgr, 
	                           CyGroupAggregationManager aggMgr,
	                           CyApplicationManager appManager) {
		this.cyGroupManager = mgr;
		this.cyAggManager = aggMgr;
		this.cyApplicationManager = appManager;

		allGroupDefaultMap = new HashMap<Class,Aggregator>();
		allGroupOverrideMap = new HashMap<CyColumn,Aggregator>();
		groupMap = new HashMap<CyGroup,GroupSpecificMaps>();
		groupActionMap = new HashMap<CyGroup, DoubleClickAction>();
		enableMap = new HashMap<CyGroup, Boolean>();
		nestedNetworkMap = new HashMap<CyGroup, Boolean>();
		hideGroupMap = new HashMap<CyGroup, Boolean>();
		opacityMap = new HashMap<CyGroup, Double>();
	}

	/***************************************************************************
	 *                             View settings                               *
	 **************************************************************************/

	@Override
	public DoubleClickAction getDoubleClickAction() {
		return action;
	}

	@Override
	public DoubleClickAction getDoubleClickAction(CyGroup group) {
		if (groupActionMap.containsKey(group))
			return groupActionMap.get(group);
		return action;
	}

	@Override
	public void setDoubleClickAction(DoubleClickAction action) {
		this.action = action;
	}

	@Override
	public void setDoubleClickAction(CyGroup group, DoubleClickAction action) {
		if (group != null)
			groupActionMap.put(group, action);
		else
			this.action = action;
	}

	@Override
	public boolean getUseNestedNetworks() { return useNestedNetworks; }

	@Override
  public boolean getUseNestedNetworks(CyGroup group) {
		if (nestedNetworkMap.containsKey(group))
			return nestedNetworkMap.get(group);
		return useNestedNetworks;
	}

	@Override
  public void setUseNestedNetworks(boolean useNN) {
		useNestedNetworks = useNN;
	}

	@Override
  public void setUseNestedNetworks(CyGroup group, boolean useNN) {
		if (group != null)
			nestedNetworkMap.put(group, useNN);
		else
			this.useNestedNetworks = useNN;
	}

	@Override
  public boolean getHideGroupNode() { return hideGroupNode; }

	@Override
  public boolean getHideGroupNode(CyGroup group) {
		if (hideGroupMap.containsKey(group))
			return hideGroupMap.get(group);
		return hideGroupNode;
	}

	@Override
  public void setHideGroupNode(boolean hideGroup) {
		hideGroupNode = hideGroup;
	}

	@Override
  public void setHideGroupNode(CyGroup group, boolean hideGroup) {
		if (group != null)
			hideGroupMap.put(group, hideGroup);
		else
			this.hideGroupNode = hideGroup;
	}

	@Override
  public double getGroupNodeOpacity() { return groupNodeOpacity; }

	@Override
  public double getGroupNodeOpacity(CyGroup group) {
		if (opacityMap.containsKey(group))
			return opacityMap.get(group);
		return groupNodeOpacity;
	}

	@Override
  public void setGroupNodeOpacity(double opacity) {
		groupNodeOpacity = opacity;
	}

	@Override
  public void setGroupNodeOpacity(CyGroup group, double opacity) {
		if (group != null)
			opacityMap.put(group, opacity);
		else
			groupNodeOpacity = opacity;
	}



	/***************************************************************************
	 *                         Aggregation settings                            *
	 **************************************************************************/

	@Override
	public boolean getEnableAttributeAggregation() {
		return enableAttributeAggregation;
	}

	@Override
	public boolean getEnableAttributeAggregation(CyGroup group) {
		if (enableMap.containsKey(group))
			return enableMap.get(group);
		return enableAttributeAggregation;
	}

	@Override
	public void setEnableAttributeAggregation(boolean aggregate) {
		this.enableAttributeAggregation = aggregate;
	}

	@Override
	public void setEnableAttributeAggregation(CyGroup group, 
	                                          boolean aggregate) {
		if (group != null)
			enableMap.put(group, aggregate);
		else
			this.enableAttributeAggregation = aggregate;
	}

	@Override
	public Aggregator getAggregator(CyGroup group, CyColumn column) {
		Class type = column.getType();
		Map<Class, Aggregator> defaultMap = allGroupDefaultMap;
		Map<CyColumn, Aggregator> overrideMap = allGroupOverrideMap;
		if (groupMap.containsKey(group)) {
			defaultMap = groupMap.get(group).getDefaults();
			overrideMap = groupMap.get(group).getOverrides();
		}
		if (overrideMap.containsKey(column))
			return overrideMap.get(column);
		return defaultMap.get(column.getType());
	}

	@Override
	public void setDefaultAggregation(CyGroup group, 
	                                  Class ovClass, Aggregator agg) {
		if (!groupMap.containsKey(group)) {
			groupMap.put(group, new GroupSpecificMaps());
		}
		groupMap.get(group).setDefault(ovClass, agg);
	}
	@Override
	public Aggregator getDefaultAggregation(CyGroup group, Class ovClass) {
		if (groupMap.containsKey(group))
			return groupMap.get(group).getDefault(ovClass);
		return null;
	}

	@Override
	public void setDefaultAggregation(Class ovClass, Aggregator agg) {
		allGroupDefaultMap.put(ovClass, agg);
	}

	@Override
	public Aggregator getDefaultAggregation(Class ovClass) {
		if (allGroupDefaultMap.containsKey(ovClass))
			return allGroupDefaultMap.get(ovClass);
		return null;
	}

	@Override
	public void setOverrideAggregation(CyGroup group, 
	                                   CyColumn column, Aggregator agg) {
		if (!groupMap.containsKey(group)) {
			groupMap.put(group, new GroupSpecificMaps());
		}
		groupMap.get(group).setOverride(column, agg);
	}
	@Override
	public Aggregator getOverrideAggregation(CyGroup group, CyColumn column) {
		if (groupMap.containsKey(group))
			return groupMap.get(group).getOverride(column);
		return null;
	}

	@Override
	public void setOverrideAggregation(CyColumn column, Aggregator agg) {
		allGroupOverrideMap.put(column, agg);
	}
	@Override
	public Aggregator getOverrideAggregation(CyColumn column) {
		if (allGroupOverrideMap.containsKey(column))
			return allGroupOverrideMap.get(column);
		return null;
	}

	public void handleEvent(GroupAddedEvent e) {
		CyGroup addedGroup = e.getGroup();
		Map<Class,Aggregator> defMap = new HashMap<Class, Aggregator>();
		for (Class cKey: allGroupDefaultMap.keySet())
			defMap.put(cKey, allGroupDefaultMap.get(cKey));
		Map<CyColumn,Aggregator> ovMap = new HashMap<CyColumn, Aggregator>();
		for (CyColumn cKey: allGroupOverrideMap.keySet())
			ovMap.put(cKey, allGroupOverrideMap.get(cKey));
		groupMap.put(addedGroup, new GroupSpecificMaps(defMap, ovMap));
	}

	class GroupSpecificMaps {
		Map<Class, Aggregator> defMap;
		Map<CyColumn, Aggregator> ovMap;

		GroupSpecificMaps () {
			this.defMap = null;
			this.ovMap = null;
		}

		GroupSpecificMaps (Map<Class, Aggregator> defMap, 
		                   Map<CyColumn, Aggregator> ovMap) {
			this.defMap = defMap;
			this.ovMap = ovMap;
		}

		void setDefault(Class ovClass, Aggregator agg) {
			if (defMap == null) defMap = new HashMap<Class, Aggregator>();

			defMap.put(ovClass, agg);
		}

		void setOverride(CyColumn column, Aggregator agg) {
			if (ovMap == null) ovMap = new HashMap<CyColumn, Aggregator>();

			ovMap.put(column, agg);
		}

		Aggregator getDefault (Class c) {
			if (defMap != null && defMap.containsKey(c))
				return defMap.get(c);
			return null;
		}

		Aggregator getOverride(CyColumn c) {
			if (ovMap != null && ovMap.containsKey(c))
				return ovMap.get(c);
			return null;
		}

		Map<Class,Aggregator> getDefaults() {return defMap;}
		Map<CyColumn,Aggregator> getOverrides() {return ovMap;}
	}
}
