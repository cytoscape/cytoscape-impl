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
import org.cytoscape.work.ContainsTunables;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.events.GroupAddedEvent;
import org.cytoscape.group.events.GroupAddedListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides the context for both the global group settings and the
 * group-specific group settings.
 */
public class CyGroupSettingsImpl implements GroupAddedListener {
  public enum DoubleClickAction {
    None("None"),
    ExpandContract("Expand/Contract"),
    Select("Select");

    private final String name;
    DoubleClickAction(String n) {this.name = n;}
    public String toString() {return name;}
 }

	final CyGroupManager cyGroupManager;
	final CyGroupAggregationManager cyAggManager;

	Map<Class, Aggregator> allGroupDefaultMap;
	Map<CyColumn, Aggregator> allGroupOverrideMap;
	Map<CyGroup, GroupSpecificMaps> groupMap;
	Map<CyGroup, DoubleClickAction> groupActionMap;
	Map<CyGroup, Boolean> enableMap;
	Map<CyGroup, Boolean> nestedNetworkMap;
	Map<CyGroup, Boolean> hideGroupMap;
	Map<CyGroup, Boolean> showCompoundNodeMap;
	Map<CyGroup, Double> opacityMap;
	DoubleClickAction action = DoubleClickAction.ExpandContract;
	boolean enableAttributeAggregation = false;
	boolean useNestedNetworks = false;
	boolean hideGroupNode = true;
	boolean showCompoundNode = false;
	double groupNodeOpacity = 100.0;
	
	private final Object lock = new Object();

	public CyGroupSettingsImpl(final CyGroupManager mgr, 
	                           final CyGroupAggregationManager aggMgr) {
		this.cyGroupManager = mgr;
		this.cyAggManager = aggMgr;

		allGroupDefaultMap = new HashMap<Class,Aggregator>();
		allGroupOverrideMap = new HashMap<CyColumn,Aggregator>();
		groupMap = new HashMap<CyGroup,GroupSpecificMaps>();
		
		groupActionMap = new ConcurrentHashMap<CyGroup, DoubleClickAction>(16, 0.75f, 2);
		enableMap = new ConcurrentHashMap<CyGroup, Boolean>(16, 0.75f, 2);
		nestedNetworkMap = new ConcurrentHashMap<CyGroup, Boolean>(16, 0.75f, 2);
		hideGroupMap = new ConcurrentHashMap<CyGroup, Boolean>(16, 0.75f, 2);
		showCompoundNodeMap = new ConcurrentHashMap<CyGroup, Boolean>(16, 0.75f, 2);
		opacityMap = new ConcurrentHashMap<CyGroup, Double>(16, 0.75f, 2);
	}

	/***************************************************************************
	 *                             View settings                               *
	 **************************************************************************/

	public DoubleClickAction getDoubleClickAction() {
		return action;
	}

	public DoubleClickAction getDoubleClickAction(CyGroup group) {
		if (groupActionMap.containsKey(group))
			return groupActionMap.get(group);
		return action;
	}

	public void setDoubleClickAction(DoubleClickAction action) {
		this.action = action;
	}

	public void setDoubleClickAction(CyGroup group, DoubleClickAction action) {
		if (group != null)
			groupActionMap.put(group, action);
		else
			this.action = action;
	}

	public boolean getUseNestedNetworks() { return useNestedNetworks; }

	private <K, V> V get(Map<K, V> map, K key, V defaultValue) {
		V value = map.get(key);
		if (value != null) {
			return value;
		}
		return defaultValue;
	}
	
    public boolean getUseNestedNetworks(CyGroup group) {
    	return get(nestedNetworkMap, group, useNestedNetworks);
	}

  public void setUseNestedNetworks(boolean useNN) {
		useNestedNetworks = useNN;
	}

  public void setUseNestedNetworks(CyGroup group, boolean useNN) {
		if (group != null)
			nestedNetworkMap.put(group, useNN);
		else
			this.useNestedNetworks = useNN;
	}

  public boolean getHideGroupNode() { return hideGroupNode; }

  public boolean getHideGroupNode(CyGroup group) {
	    return get(hideGroupMap, group, hideGroupNode); 
	}

  public void setHideGroupNode(boolean hideGroup) {
		hideGroupNode = hideGroup;
	}

  public void setHideGroupNode(CyGroup group, boolean hideGroup) {
		if (group != null)
			hideGroupMap.put(group, hideGroup);
		else
			this.hideGroupNode = hideGroup;
	}

	public boolean getShowCompoundNode() { return showCompoundNode; }

	public boolean getShowCompoundNode(CyGroup group) { 
		return get(showCompoundNodeMap, group, showCompoundNode); 
	}

	public void setShowCompoundNode(boolean showCompoundNode) { 
		this.showCompoundNode = showCompoundNode; 
	}

	public void setShowCompoundNode(CyGroup group, boolean showCompoundNode) { 
		if (group != null)
			showCompoundNodeMap.put(group, showCompoundNode);
		else
			this.showCompoundNode = showCompoundNode;
	}

  public double getGroupNodeOpacity() { return groupNodeOpacity; }

  public double getGroupNodeOpacity(CyGroup group) {
	    return get(opacityMap, group, groupNodeOpacity);
	}

  public void setGroupNodeOpacity(double opacity) {
		groupNodeOpacity = opacity;
	}

  public void setGroupNodeOpacity(CyGroup group, double opacity) {
		if (group != null)
			opacityMap.put(group, opacity);
		else
			groupNodeOpacity = opacity;
	}



	/***************************************************************************
	 *                         Aggregation settings                            *
	 **************************************************************************/

	public boolean getEnableAttributeAggregation() {
		return enableAttributeAggregation;
	}

	public boolean getEnableAttributeAggregation(CyGroup group) {
		return get(enableMap, group, enableAttributeAggregation);
	}

	public void setEnableAttributeAggregation(boolean aggregate) {
		this.enableAttributeAggregation = aggregate;
	}

	public void setEnableAttributeAggregation(CyGroup group, 
	                                          boolean aggregate) {
		if (group != null)
			enableMap.put(group, aggregate);
		else
			this.enableAttributeAggregation = aggregate;
	}

	public Aggregator getAggregator(CyGroup group, CyColumn column) {
		Class type = column.getType();
		synchronized (lock) {
			Map<Class, Aggregator> defaultMap = allGroupDefaultMap;
			Map<CyColumn, Aggregator> overrideMap = allGroupOverrideMap;
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps != null) {
				defaultMap = groupSpecificMaps.getDefaults();
				overrideMap = groupSpecificMaps.getOverrides();
			}
			Aggregator aggregator = overrideMap.get(column);
			if (aggregator != null) {
				return aggregator;
			}
			return defaultMap.get(column.getType());
		}
	}

	public void setDefaultAggregation(CyGroup group, 
	                                  Class ovClass, Aggregator agg) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps == null) {
				groupSpecificMaps = new GroupSpecificMaps();
				groupMap.put(group, groupSpecificMaps);
			}
			groupSpecificMaps.setDefault(ovClass, agg);
		}
	}
	public Aggregator getDefaultAggregation(CyGroup group, Class ovClass) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps != null)
				return groupSpecificMaps.getDefault(ovClass);
			return null;
		}
	}

	public void setDefaultAggregation(Class ovClass, Aggregator agg) {
		synchronized (lock) {
			allGroupDefaultMap.put(ovClass, agg);
		}
	}

	public Aggregator getDefaultAggregation(Class ovClass) {
		synchronized (lock) {
			return get(allGroupDefaultMap, ovClass, null);
		}
	}

	public void setOverrideAggregation(CyGroup group, 
	                                   CyColumn column, Aggregator agg) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps == null) {
				groupSpecificMaps = new GroupSpecificMaps();
				groupMap.put(group, groupSpecificMaps);
			}
			groupSpecificMaps.setOverride(column, agg);
		}
	}
	public Aggregator getOverrideAggregation(CyGroup group, CyColumn column) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps != null)
				return groupSpecificMaps.getOverride(column);
			return null;
		}
	}

	public void setOverrideAggregation(CyColumn column, Aggregator agg) {
		synchronized (lock) {
			allGroupOverrideMap.put(column, agg);
		}
	}
	public Aggregator getOverrideAggregation(CyColumn column) {
		synchronized (lock) {
			return get(allGroupOverrideMap, column, null);
		}
	}

	public void handleEvent(GroupAddedEvent e) {
		CyGroup addedGroup = e.getGroup();
		Map<Class,Aggregator> defMap = new HashMap<Class, Aggregator>();
		synchronized (lock) {
			for (Class cKey: allGroupDefaultMap.keySet())
				defMap.put(cKey, allGroupDefaultMap.get(cKey));
			Map<CyColumn,Aggregator> ovMap = new HashMap<CyColumn, Aggregator>();
			for (CyColumn cKey: allGroupOverrideMap.keySet())
				ovMap.put(cKey, allGroupOverrideMap.get(cKey));
			groupMap.put(addedGroup, new GroupSpecificMaps(defMap, ovMap));
		}
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
