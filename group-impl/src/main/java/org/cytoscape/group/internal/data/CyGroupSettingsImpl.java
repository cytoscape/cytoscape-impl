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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.ContainsTunables;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.CyGroupSettingsManager.DoubleClickAction;
import org.cytoscape.group.CyGroupSettingsManager.GroupViewType;
import org.cytoscape.group.internal.CyGroupImpl;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.events.GroupAddedEvent;
import org.cytoscape.group.events.GroupAddedListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.PropertyUpdatedListener;
import org.cytoscape.service.util.CyServiceRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides the context for both the global group settings and the
 * group-specific group settings.
 */
public class CyGroupSettingsImpl implements GroupAddedListener, 
                                            NetworkAddedListener,
                                            PropertyUpdatedListener,
                                            CyGroupSettingsManager {
	final CyGroupManagerImpl cyGroupManager;
	final CyGroupAggregationManagerImpl cyAggManager;
	final CyServiceRegistrar cyServiceRegistrar;
	final CyApplicationManager appMgr;
	final CyEventHelper eventHelper;

	CyProperty<Properties> groupSettingsProperties = null;

	// Column names for serialization/deserialization of group settings
	public final static String AGGREGATION_SETTINGS = "__AggregationSettings";
	public final static String AGGREGATION_OVERRIDE_SETTINGS = "__AggregationOverrideSettings";
	public final static String VIEW_SETTINGS = "__ViewSettings";

	// Property names for saving default group settings
	final static String PROPERTY_NAME = "groupSettings";

	// View settings
	final static String DOUBLE_CLICK_ACTION = "doubleClickAction";
	final static String GROUP_VIEW_TYPE = "groupViewType";
	final static String GROUP_NODE_OPACITY = "groupNodeOpacity";
	final static String USE_NESTED_NETWORKS = "useNestedNetworks";

	// Aggregation settings
	final static String AGG_ENABLED = "aggregationEnabled";
	final static String DEFAULT_INT_AGGREGATION = "defaultIntegerAggregation";
	final static String DEFAULT_LONG_AGGREGATION = "defaultLongAggregation";
	final static String DEFAULT_DOUBLE_AGGREGATION = "defaultDoubleAggregation";
	final static String DEFAULT_STRING_AGGREGATION = "defaultStringAggregation";
	final static String DEFAULT_BOOLEAN_AGGREGATION = "defaultBooleanAggregation";
	final static String DEFAULT_STRING_LIST_AGGREGATION = "defaultStringListAggregation";
	final static String DEFAULT_INTEGER_LIST_AGGREGATION = "defaultIntegerListAggregation";
	final static String DEFAULT_LONG_LIST_AGGREGATION = "defaultLongListAggregation";
	final static String DEFAULT_DOUBLE_LIST_AGGREGATION = "defaultDoubleListAggregation";
	final static String OVERRIDE_AGGREGATION = "overrideColumnAggregations";

	Map<Class<?>, Aggregator<?>> allGroupDefaultMap;
	Map<Class<?>, Aggregator<?>> allGroupListDefaultMap;
	Map<CyColumn, Aggregator<?>> allGroupOverrideMap;
	Map<String, String> allGroupOverridePropertyMap;

	// TODO: should these be network-specific?
	Map<CyGroup, GroupSpecificMaps> groupMap;
	Map<CyGroup, DoubleClickAction> groupActionMap;
	Map<CyGroup, GroupViewType> groupViewTypeMap;
	Map<CyGroup, Boolean> enableMap;
	Map<CyGroup, Boolean> nestedNetworkMap;
	Map<CyGroup, Double> opacityMap;
	DoubleClickAction action = DoubleClickAction.EXPANDCONTRACT;
	boolean enableAttributeAggregation = false;
	boolean useNestedNetworks = false;
	double groupNodeOpacity = 100.0;
	GroupViewType groupViewType = GroupViewType.NONE;
	boolean loadingNewGroup = false;
	
	private final Object lock = new Object();

	public CyGroupSettingsImpl(final CyGroupManagerImpl mgr, 
	                           final CyGroupAggregationManagerImpl aggMgr,
	                           final CyServiceRegistrar registrar) {
		this.cyGroupManager = mgr;
		this.cyAggManager = aggMgr;
		this.cyServiceRegistrar = registrar;
		this.appMgr = cyGroupManager.getService(CyApplicationManager.class);
		this.eventHelper = cyGroupManager.getService(CyEventHelper.class);

		allGroupDefaultMap = new HashMap<Class<?>,Aggregator<?>>();
		allGroupListDefaultMap = new HashMap<Class<?>,Aggregator<?>>();
		allGroupOverrideMap = new HashMap<CyColumn,Aggregator<?>>();
		allGroupOverridePropertyMap = new HashMap<String, String>(); // Special map for loading initial properties
		groupMap = new HashMap<CyGroup,GroupSpecificMaps>();
		
		groupActionMap = new ConcurrentHashMap<CyGroup, DoubleClickAction>(16, 0.75f, 2);
		groupViewTypeMap = new ConcurrentHashMap<CyGroup, GroupViewType>(16, 0.75f, 2);
		enableMap = new ConcurrentHashMap<CyGroup, Boolean>(16, 0.75f, 2);
		nestedNetworkMap = new ConcurrentHashMap<CyGroup, Boolean>(16, 0.75f, 2);
		opacityMap = new ConcurrentHashMap<CyGroup, Double>(16, 0.75f, 2);

		// Create our properties reader
		groupSettingsProperties = new PropsReader();

		Properties serviceProperties = new Properties();
		serviceProperties.setProperty("cyPropertyName","groupSettings.props");

		cyServiceRegistrar.registerService(groupSettingsProperties, CyProperty.class, serviceProperties);

		// Initialize our defaults from the properties
		loadProperties();
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
		updateProperties();
	}

	@Override
	public void setDoubleClickAction(CyGroup group, DoubleClickAction action) {
		if (group != null) {
			groupActionMap.put(group, action);
			updateSettingsInTable(group);
		} else {
			this.action = action;
			updateProperties();
		}
	}

	@Override
	public GroupViewType getGroupViewType() {
		return groupViewType;
	}

	@Override
	public GroupViewType getGroupViewType(CyGroup group) {
		if (groupViewTypeMap.containsKey(group)) {
			// System.out.println("getGroupViewType("+group+") = "+groupViewTypeMap.get(group));
			return groupViewTypeMap.get(group);
		}
		// System.out.println("getGroupViewType("+group+") = default ("+groupViewType+")");
		return groupViewType;
	}

	@Override
	public void setGroupViewType(GroupViewType groupViewType) {
		this.groupViewType = groupViewType;
		updateProperties();
	}

	@Override
	public void setGroupViewType(CyGroup group, GroupViewType groupViewType) {
		// System.out.println("setGroupViewType("+group+") to "+groupViewType);
		if (group != null) {
			GroupViewType oldType = getGroupViewType(group);
			groupViewTypeMap.put(group, groupViewType);
			updateSettingsInTable(group);
			if (!loadingNewGroup) {
				eventHelper.fireEvent(new GroupViewTypeChangedEvent(group, oldType, groupViewType));
			} else {
				// During session loading, we may have mis-identified the group view type because
				// we using the default.  If we did that, we might have incorrectly set the 
				// group node shown flag.  Fix that now...
				if (!oldType.equals(GroupViewType.NONE) && groupViewType.equals(GroupViewType.NONE)) {
					for (CyNetwork net: group.getNetworkSet()) {
						// Careful -- the network set includes the root
						if (net.equals(group.getRootNetwork()))
							continue;
						((CyGroupImpl)group).setGroupNodeShown(net, false);
					}
				}
			}
		} else {
			this.groupViewType = groupViewType;
			updateProperties();
		}
	}

	@Override
	public boolean getUseNestedNetworks() { return useNestedNetworks; }

	@Override
	public boolean getUseNestedNetworks(CyGroup group) {
		return get(nestedNetworkMap, group, useNestedNetworks);
	}

	@Override
  public void setUseNestedNetworks(boolean useNN) {
		useNestedNetworks = useNN;
		updateProperties();
	}

	@Override
  public void setUseNestedNetworks(CyGroup group, boolean useNN) {
		if (group != null) {
			nestedNetworkMap.put(group, useNN);
			updateSettingsInTable(group);
		} else {
			this.useNestedNetworks = useNN;
			updateProperties();
		}
	}

  public double getGroupNodeOpacity() { return groupNodeOpacity; }

  public double getGroupNodeOpacity(CyGroup group) {
	    return get(opacityMap, group, groupNodeOpacity);
	}

  public void setGroupNodeOpacity(double opacity) {
		groupNodeOpacity = opacity;
		updateProperties();
	}

  public void setGroupNodeOpacity(CyGroup group, double opacity) {
		if (group != null) {
			opacityMap.put(group, opacity);
		} else {
			groupNodeOpacity = opacity;
			updateProperties();
		}
	}

	private <K, V> V get(Map<K, V> map, K key, V defaultValue) {
		V value = map.get(key);
		if (value != null) {
			return value;
		}
		return defaultValue;
	}
	

	/***************************************************************************
	 *                         Aggregation handling                            *
	 **************************************************************************/

	// This routine is the main routine used to walk that cascade
	// of aggregations
	@Override
	public Aggregator<?> getAggregator(CyGroup group, CyColumn column) {
		Class<?> type = column.getType();
		synchronized (lock) {
			// First, make sure the our override map is up-to-date
			// This will check to see if we have a property for this
			// and update it if we do
			getOverrideAggregation(column);
			Map<CyColumn, Aggregator<?>> overrideMap = allGroupOverrideMap;

			Map<Class<?>, Aggregator<?>> defaultMap = allGroupDefaultMap;
			Map<Class<?>, Aggregator<?>> defaultListMap = allGroupListDefaultMap;
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps != null) {
				defaultMap = groupSpecificMaps.getDefaults();
				defaultListMap = groupSpecificMaps.getListDefaults();
				overrideMap = groupSpecificMaps.getOverrides();
			}
			Aggregator<?> aggregator = overrideMap.get(column);
			if (aggregator != null) {
				return aggregator;
			}
			if (type.isAssignableFrom(List.class)) {
				return defaultListMap.get(column.getListElementType());
			} else {
				return defaultMap.get(column.getType());
			}
		}
	}

	// Update all of our maps when we add a new group
	public void handleEvent(GroupAddedEvent e) {
		CyGroup addedGroup = e.getGroup();
		Map<Class<?>,Aggregator<?>> defMap = new HashMap<>();
		Map<Class<?>,Aggregator<?>> defListMap = new HashMap<>();
		CyNetwork network = appMgr.getCurrentNetwork();
		if (network == null || !addedGroup.isInNetwork(network)) {
			for (CyNetwork net: addedGroup.getNetworkSet()) {
				network = net;
				break;
			}
		}
		synchronized (lock) {
			for (Class<?> cKey: allGroupDefaultMap.keySet())
				defMap.put(cKey, allGroupDefaultMap.get(cKey));
			for (Class<?> cKey: allGroupListDefaultMap.keySet())
				defListMap.put(cKey, allGroupListDefaultMap.get(cKey));
			Map<CyColumn,Aggregator<?>> ovMap = new HashMap<CyColumn, Aggregator<?>>();
			for (CyColumn cKey: allGroupOverrideMap.keySet())
				ovMap.put(cKey, allGroupOverrideMap.get(cKey));
			groupMap.put(addedGroup, new GroupSpecificMaps(defMap, defListMap, ovMap));
		}
		// Now override these with any settings from the table
		loadingNewGroup = true; // Flag to indicate that we don't want to trigger visualization changes
		try {
			loadSettingsFromTable(addedGroup, network);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		loadingNewGroup = false;
	}

	public void handleEvent(NetworkAddedEvent e) {
		CyNetwork net = e.getNetwork();
		Set<CyGroup> groupSet = cyGroupManager.getGroupSet(net);
		if (groupSet == null || groupSet.isEmpty()) return;
		for (CyGroup group: groupSet) {
			loadSettingsFromTable(group, net);
		}
	}

	public void handleEvent(PropertyUpdatedEvent e) {
		if (e.getSource() != null && e.getSource().getName().equals("groupSettings")) {
			groupSettingsProperties = (CyProperty<Properties>)e.getSource();
			loadProperties();
		}
	}

	/***************************************************************************
	 *                     Global aggregation settings                         *
	 **************************************************************************/

	@Override
	public boolean getEnableAttributeAggregation() {
		return enableAttributeAggregation;
	}

	@Override
	public void setEnableAttributeAggregation(boolean aggregate) {
		this.enableAttributeAggregation = aggregate;
		updateProperties();
	}

	@Override
	public Aggregator<?> getDefaultAggregation(Class<?> ovClass) {
		synchronized (lock) {
			if (ovClass.isAssignableFrom(List.class))
				return getDefaultListAggregation(String.class);
			return get(allGroupDefaultMap, ovClass, null);
		}
	}

	@Override
	public void setDefaultAggregation(Class<?> ovClass, Aggregator<?> agg) {
		synchronized (lock) {
			allGroupDefaultMap.put(ovClass, agg);
		}
		updateProperties();
	}

	@Override
	public Aggregator<?> getDefaultListAggregation(Class<?> ovClass) {
		synchronized (lock) {
			return get(allGroupListDefaultMap, ovClass, null);
		}
	}

	@Override
	public void setDefaultListAggregation(Class<?> ovClass, Aggregator<?> agg) {
		synchronized (lock) {
			allGroupListDefaultMap.put(ovClass, agg);
		}
		updateProperties();
	}

	/**
	 * Get the override aggregation setting for a column (if any).
	 * Note that we need to be careful here since we may have a
	 * previously saved property that we need to be careful to
	 * copy over.
	 */
	@Override
	public Aggregator<?> getOverrideAggregation(CyColumn column) {
		synchronized (lock) {
			// First, check and see if we have an entry for this column
			if (allGroupOverrideMap.containsKey(column)) {
				return get(allGroupOverrideMap, column, null);
			}
			// We don't, so see if there is a default property
			if (allGroupOverridePropertyMap.containsKey(column.getName())) {
				// Great!  Now add that to our allGroupOverrideMap
				String aggrName = allGroupOverridePropertyMap.get(column.getName());
				Aggregator<?> aggr;
				if (column.getType().isAssignableFrom(List.class))
					aggr = cyAggManager.getListAggregator(column.getListElementType(), aggrName);
				else
					aggr = cyAggManager.getAggregator(column.getType(), aggrName);
				if (aggr != null)
					allGroupOverrideMap.put(column, aggr);
				return aggr;
			}
		}
		return null;
	}

	@Override
	public void setOverrideAggregation(CyColumn column, Aggregator<?> agg) {
		synchronized (lock) {
			allGroupOverrideMap.put(column, agg);
		}
		updateProperties();
	}

	/***************************************************************************
	 *                Group specific aggregation settings                      *
	 **************************************************************************/

	@Override
	public boolean getEnableAttributeAggregation(CyGroup group) {
		return get(enableMap, group, enableAttributeAggregation);
	}

	@Override
	public void setEnableAttributeAggregation(CyGroup group, 
	                                          boolean aggregate) {
		if (group != null) {
			enableMap.put(group, aggregate);
			updateSettingsInTable(group);
		} else {
			this.enableAttributeAggregation = aggregate;
			updateProperties();
		}
	}

	@Override
	public Aggregator<?> getDefaultAggregation(CyGroup group, Class<?> ovClass) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps != null)
				return groupSpecificMaps.getDefault(ovClass);
			return null;
		}
	}

	@Override
	public void setDefaultAggregation(CyGroup group, 
	                                  Class<?> ovClass, Aggregator<?> agg) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps == null) {
				groupSpecificMaps = new GroupSpecificMaps();
				groupMap.put(group, groupSpecificMaps);
			}
			groupSpecificMaps.setDefault(ovClass, agg);
		}
		updateSettingsInTable(group);
	}

	@Override
	public Aggregator<?> getDefaultListAggregation(CyGroup group, Class<?> ovClass) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps != null)
				return groupSpecificMaps.getListDefault(ovClass);
			return null;
		}
	}

	@Override
	public void setDefaultListAggregation(CyGroup group, 
	                                     Class<?> ovClass, Aggregator<?> agg) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps == null) {
				groupSpecificMaps = new GroupSpecificMaps();
				groupMap.put(group, groupSpecificMaps);
			}
			groupSpecificMaps.setListDefault(ovClass, agg);
		}
		updateSettingsInTable(group);
	}

	@Override
	public void setDefaultAggregation(CyGroup group, 
	                                  Class<?> ovClass, String agg) {
		Aggregator<?> def = cyAggManager.getAggregator(ovClass, agg);

		if (def != null)
			setDefaultAggregation(group, ovClass, def);
	}

	@Override
	public void setDefaultListAggregation(CyGroup group, 
	                                      Class<?> ovClass, String agg) {
		Aggregator<?> def = cyAggManager.getListAggregator(ovClass, agg);

		if (def != null)
			setDefaultListAggregation(group, ovClass, def);
	}

	@Override
	public Aggregator<?> getOverrideAggregation(CyGroup group, CyColumn column) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps != null) {
				return groupSpecificMaps.getOverride(column);
			}
			return null;
		}
	}

	@Override
	public void setOverrideAggregation(CyGroup group, 
	                                   CyColumn column, Aggregator<?> agg) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps == null) {
				groupSpecificMaps = new GroupSpecificMaps();
				groupMap.put(group, groupSpecificMaps);
			}
			groupSpecificMaps.setOverride(column, agg);
		}
		updateSettingsInTable(group);
	}

	/***************************************************************************
	 *                          Property handling                              *
	 **************************************************************************/

	/**
	 * Update the group CyProperties
	 */
	public void updateProperties() {
		Properties p = groupSettingsProperties.getProperties();
		setBooleanProperty(p, USE_NESTED_NETWORKS, useNestedNetworks);
		p.setProperty(DOUBLE_CLICK_ACTION, action.toString());
		p.setProperty(GROUP_VIEW_TYPE, groupViewType.toString());

		setBooleanProperty(p, AGG_ENABLED, enableAttributeAggregation);
		setAggregationProperty(p, DEFAULT_INT_AGGREGATION, Integer.class, null);
		setAggregationProperty(p, DEFAULT_LONG_AGGREGATION, Long.class, null);
		setAggregationProperty(p, DEFAULT_DOUBLE_AGGREGATION, Double.class, null);
		setAggregationProperty(p, DEFAULT_STRING_AGGREGATION, String.class, null);
		setAggregationProperty(p, DEFAULT_BOOLEAN_AGGREGATION, Boolean.class, null);
		setAggregationProperty(p, DEFAULT_STRING_LIST_AGGREGATION, List.class, String.class);
		setAggregationProperty(p, DEFAULT_INTEGER_LIST_AGGREGATION, List.class, Integer.class);
		setAggregationProperty(p, DEFAULT_LONG_LIST_AGGREGATION, List.class, Long.class);
		setAggregationProperty(p, DEFAULT_DOUBLE_LIST_AGGREGATION, List.class, Double.class);

		p.setProperty(OVERRIDE_AGGREGATION, encodeAggregationOverrides());
	}

	/**
	 * Load the defaults from the group CyProperties
	 */
	public void loadProperties() {
		// Load the default group properties
		Properties p = groupSettingsProperties.getProperties();

		// OK, get the view settings
		useNestedNetworks = getBooleanProperty(p, USE_NESTED_NETWORKS, useNestedNetworks);
		action = getDoubleClickProperty(p, DOUBLE_CLICK_ACTION, action);
		groupViewType = getGroupViewTypeProperty(p, GROUP_VIEW_TYPE, groupViewType);


		// OK, now get the aggregation settings
		enableAttributeAggregation = getBooleanProperty(p, AGG_ENABLED, enableAttributeAggregation);

		getAggregationProperty(p, DEFAULT_INT_AGGREGATION, Integer.class, null, AttributeHandlingType.AVG);
		getAggregationProperty(p, DEFAULT_LONG_AGGREGATION, Long.class, null, AttributeHandlingType.NONE);
		getAggregationProperty(p, DEFAULT_DOUBLE_AGGREGATION, Double.class, null, AttributeHandlingType.AVG);
		getAggregationProperty(p, DEFAULT_STRING_AGGREGATION, String.class, null, AttributeHandlingType.CSV);
		getAggregationProperty(p, DEFAULT_BOOLEAN_AGGREGATION, Boolean.class, null, AttributeHandlingType.NONE);
		getAggregationProperty(p, DEFAULT_STRING_LIST_AGGREGATION, List.class, String.class, AttributeHandlingType.UNIQUE);
		getAggregationProperty(p, DEFAULT_INTEGER_LIST_AGGREGATION, List.class, Integer.class, AttributeHandlingType.AVG);
		getAggregationProperty(p, DEFAULT_LONG_LIST_AGGREGATION, List.class, Long.class, AttributeHandlingType.AVG);
		getAggregationProperty(p, DEFAULT_DOUBLE_LIST_AGGREGATION, List.class, Double.class, AttributeHandlingType.AVG);

		getOverrideAggregationProperty(p, OVERRIDE_AGGREGATION);
	}

	private void getOverrideAggregationProperty(Properties p, String key) {
		CyNetwork currentNetwork = appMgr.getCurrentNetwork();
		if (currentNetwork != null) {
			CyTable nodeTable = currentNetwork.getDefaultNodeTable();
			if (p.containsKey(key)) {
				List<String[]> aggr = decodeAggregationOverrides(p.getProperty(key));
				for (String[] pair: aggr) {
					allGroupOverridePropertyMap.put(pair[0], pair[1]);
				}
			}
		}
	}

	private boolean getBooleanProperty(Properties p, String key, boolean prop) {
		String v = p.getProperty(key, Boolean.valueOf(prop).toString());
		return Boolean.parseBoolean(v);
	}

	private void setBooleanProperty(Properties p, String key, boolean prop) {
		p.setProperty(key, Boolean.valueOf(prop).toString());
	}

	private DoubleClickAction getDoubleClickProperty(Properties p, String key, DoubleClickAction prop) {
		String v = p.getProperty(key, prop.toString());
		return convertDoubleClick(v);
	}

	private GroupViewType getGroupViewTypeProperty(Properties p, String key, GroupViewType prop) {
		String v = p.getProperty(key, prop.toString());
		return convertGroupViewType(v);
	}

	private Aggregator<?> getAggregationProperty(Properties p, String key, Class<?> c, Class<?> listClass,
	                                             AttributeHandlingType defType) {
		String pValue = p.getProperty(key, defType.toString());

		List<Aggregator<?>> aggs;
		Aggregator<?> def;
	 	if (c.isAssignableFrom(List.class)) {
			aggs	= cyAggManager.getListAggregators(listClass);
			def = cyAggManager.getListAggregator(listClass, pValue);

			if (def != null) {
				allGroupListDefaultMap.put(listClass, def);
			} 
		} else {
			aggs	= cyAggManager.getAggregators(c);
			def = cyAggManager.getAggregator(c, pValue);

			if (def != null)
				allGroupDefaultMap.put(c, def);
		}

		return def;
	}

	private void setAggregationProperty(Properties p, String key, Class<?> clazz, Class<?> listClass) {
		Aggregator<?> a = null;
		if (clazz.isAssignableFrom(List.class)) {
			if (allGroupListDefaultMap.containsKey(listClass))
				a = allGroupListDefaultMap.get(listClass);
		} else {
			if (allGroupDefaultMap.containsKey(clazz))
				a = allGroupDefaultMap.get(clazz);
		}
		if (a != null) {
			p.setProperty(key, a.toString());
		} else {
			p.remove(key);
		}
	}

	/***************************************************************************
	 *        Group-specific settings serialization/deserialization            *
	 **************************************************************************/

	/**
	 * Update the settings for a specific group
	 */
	public void updateSettingsInTable(CyGroup group) {
		CyNetwork network = appMgr.getCurrentNetwork();
		if (network == null || !group.isInNetwork(network)) {
			for (CyNetwork net: group.getNetworkSet()) {
				network = net;
				break;
			}
		}
		List<String> viewSettings = new ArrayList<>();

		// Update all of our view settings as a list
		createColumnIfNeeded(network, group.getGroupNode(), VIEW_SETTINGS, List.class, String.class);

		addViewSetting(viewSettings, DOUBLE_CLICK_ACTION, getDoubleClickAction(group).toString());
		addViewSetting(viewSettings, USE_NESTED_NETWORKS, ""+getUseNestedNetworks(group));
		addViewSetting(viewSettings, GROUP_VIEW_TYPE, ""+getGroupViewType(group));
		addViewSetting(viewSettings, GROUP_NODE_OPACITY, ""+getGroupNodeOpacity(group));
		// currentNetwork.getRow(group.getGroupNode(), CyNetwork.HIDDEN_ATTRS).set(VIEW_SETTINGS, viewSettings);
		network.getRow(group.getGroupNode()).set(VIEW_SETTINGS, viewSettings);

		// Update default aggregations
		createColumnIfNeeded(network, group.getGroupNode(), AGGREGATION_SETTINGS, List.class, String.class);
		List<String> aggrSettings = new ArrayList<>();
		aggrSettings.add(AGG_ENABLED+"="+getEnableAttributeAggregation(group));
		addAggregationSetting(group, DEFAULT_INT_AGGREGATION, Integer.class, null, aggrSettings);
		addAggregationSetting(group, DEFAULT_LONG_AGGREGATION, Long.class, null, aggrSettings);
		addAggregationSetting(group, DEFAULT_DOUBLE_AGGREGATION, Double.class, null, aggrSettings);
		addAggregationSetting(group, DEFAULT_STRING_AGGREGATION, String.class, null, aggrSettings);
		addAggregationSetting(group, DEFAULT_BOOLEAN_AGGREGATION, Boolean.class, null, aggrSettings);

		addAggregationSetting(group, DEFAULT_STRING_LIST_AGGREGATION, List.class, String.class, aggrSettings);
		addAggregationSetting(group, DEFAULT_INTEGER_LIST_AGGREGATION, List.class, Integer.class, aggrSettings);
		addAggregationSetting(group, DEFAULT_LONG_LIST_AGGREGATION, List.class, Long.class, aggrSettings);
		addAggregationSetting(group, DEFAULT_DOUBLE_LIST_AGGREGATION, List.class, Double.class, aggrSettings);

		// currentNetwork.getRow(group.getGroupNode(), CyNetwork.HIDDEN_ATTRS).set(AGGREGATION_SETTINGS, aggrSettings);
		network.getRow(group.getGroupNode()).set(AGGREGATION_SETTINGS, aggrSettings);

		if (groupMap.containsKey(group)) {
			// Update overrides
			createColumnIfNeeded(network, group.getGroupNode(), AGGREGATION_OVERRIDE_SETTINGS, List.class, String.class);
			List<String> aggrOverrideSettings = new ArrayList<>();
			GroupSpecificMaps gsm = groupMap.get(group);
			Map<CyColumn,Aggregator<?>> overrides = groupMap.get(group).getOverrides();
			if (overrides == null || overrides.isEmpty())
				return;

			for (CyColumn column: overrides.keySet()) {
				aggrOverrideSettings.add(column.getName()+"="+overrides.get(column).toString());
			}

			// currentNetwork.getRow(group.getGroupNode(), CyNetwork.HIDDEN_ATTRS).set(AGGREGATION_OVERRIDE_SETTINGS, aggrOverrideSettings);
			network.getRow(group.getGroupNode()).set(AGGREGATION_OVERRIDE_SETTINGS, aggrOverrideSettings);
		}
	}

	void addAggregationSetting(CyGroup group, String attribute, 
	                           Class<?> clazz, Class<?> listClass, List<String> settings) {
		Aggregator<?> agg = null;
		if (clazz.isAssignableFrom(List.class))
			agg = getDefaultListAggregation(group, listClass);
		else
			agg = getDefaultAggregation(group, clazz);
		if (agg == null) return;

		settings.add(attribute+"="+agg.toString());
	}

	void addViewSetting(List<String> settings, String attribute, String value) {
		settings.add(attribute+"="+value);
	}


	/**
	 * Load the settings for a specific group
	 *
	 * @param group the group we want to load settings for
	 * @return true if we were able to load the settings, false otherwise
	 */
	public boolean loadSettingsFromTable(CyGroup group, CyNetwork network) {
		// System.out.println("Loading settings for "+group+" in network "+network+" ("+network.getSUID()+")");
		CyTable table = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		if (table.getColumn(VIEW_SETTINGS) == null) {
			// System.out.println("Oops -- no settings column");
			return false;
		}

		Long suid = group.getGroupNode().getSUID();

		List<String> viewSettings = 
			table.getRow(suid).getList(VIEW_SETTINGS, String.class);

		if (viewSettings != null && !viewSettings.isEmpty()) {
			for (String setting: viewSettings) {
				updateSetting(group, setting);
			}
		}

		List<String> aggrSettings = table.getRow(suid).getList(AGGREGATION_SETTINGS, String.class);
		if (aggrSettings != null && !aggrSettings.isEmpty()) {
			for (String setting: aggrSettings) {
				updateAggrSetting(group, setting);
			}
		}

		List<String> aggrOverrideSettings = table.getRow(suid).getList(AGGREGATION_OVERRIDE_SETTINGS, String.class);
		if (aggrOverrideSettings != null && !aggrOverrideSettings.isEmpty()) {
			for (String setting: aggrSettings) {
				updateAggrOverrideSetting(group, setting);
			}
		}

		return true;
	}

	void updateSetting(CyGroup group, String setting) {
		String[] pair = setting.split("=");
		if (pair.length != 2) return;
		if (pair[0].equals(DOUBLE_CLICK_ACTION)) {
			DoubleClickAction action = convertDoubleClick(pair[1]);
			if (action != null)
				setDoubleClickAction(group, action);
		} else if (pair[0].equals(GROUP_VIEW_TYPE)) {
			GroupViewType type = convertGroupViewType(pair[1]);
			if (type != null)
				setGroupViewType(group, type);
		} else if (pair[0].equals(USE_NESTED_NETWORKS))
			setUseNestedNetworks(group, Boolean.parseBoolean(pair[1]));
		else if (pair[0].equals(GROUP_NODE_OPACITY)) {
			setGroupNodeOpacity(group, Double.parseDouble(pair[1]));
		}

		return;
	}

	void updateAggrSetting(CyGroup group, String setting) {
		String[] pair = setting.split("=");
		if (AGG_ENABLED.equals(pair[0])) {
			setEnableAttributeAggregation(group, Boolean.parseBoolean(pair[1]));
		}
		else if (DEFAULT_INT_AGGREGATION.equals(pair[0]))
			setDefaultAggregation(group, Integer.class, pair[1]);
		else if (DEFAULT_LONG_AGGREGATION.equals(pair[0])) {
			setDefaultAggregation(group, Long.class, pair[1]);
		} else if (DEFAULT_DOUBLE_AGGREGATION.equals(pair[0])) {
			setDefaultAggregation(group, Double.class, pair[1]);
		} else if (DEFAULT_STRING_AGGREGATION.equals(pair[0])) {
			setDefaultAggregation(group, String.class, pair[1]);
		} else if (DEFAULT_STRING_LIST_AGGREGATION.equals(pair[0])) {
			setDefaultListAggregation(group, String.class, pair[1]);
		} else if (DEFAULT_INTEGER_LIST_AGGREGATION.equals(pair[0])) {
			setDefaultListAggregation(group, Integer.class, pair[1]);
		} else if (DEFAULT_LONG_LIST_AGGREGATION.equals(pair[0])) {
			setDefaultListAggregation(group, Long.class, pair[1]);
		} else if (DEFAULT_DOUBLE_LIST_AGGREGATION.equals(pair[0])) {
			setDefaultListAggregation(group, Double.class, pair[1]);
		} else if (DEFAULT_BOOLEAN_AGGREGATION.equals(pair[0])) {
			setDefaultAggregation(group, Boolean.class, pair[1]);
		}
		
	}

	void updateAggrOverrideSetting(CyGroup group, String setting) {
		String[] pair = setting.split("=");
		CyNetwork currentNetwork = appMgr.getCurrentNetwork();
		if (currentNetwork == null) 
			return;
		CyColumn column = currentNetwork.getDefaultNodeTable().getColumn(pair[0]);
		if (column == null)
			return;
		Aggregator<?> def = cyAggManager.getAggregator(column.getType(), pair[1]);
		if (def == null)
			return;

		setOverrideAggregation(group, column, def);
	}


	/***************************************************************************
	 *                      Encoding/Decoding routines                         *
	 **************************************************************************/

	/**
	 * Encode the override attributes
	 */
	public String encodeAggregationOverrides() {
		String str = "";
		for (CyColumn column: allGroupOverrideMap.keySet()) {
			if (str.length() == 0)
				str = column.getName()+"="+allGroupOverrideMap.get(column).toString();
			else
				str += "\t"+column.getName()+"="+allGroupOverrideMap.get(column).toString();
		}
		return str;
	}

	/**
	 * Decode the override attributes
	 */
	public List<String[]> decodeAggregationOverrides(String overrides) {
		List<String[]> results = new ArrayList<>();
		for (String override: overrides.split("\t")) {
			String[] pair = override.split("=");
			results.add(pair);
		}
		return results;
	}

	/**
	 */
	public void createColumnIfNeeded(CyNetwork network, CyNode node, String column, 
	                                 Class<?> clazz, Class<?> elementClazz) {
		// CyTable nodeTable = network.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		CyTable nodeTable = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		if (nodeTable.getColumn(column) == null) {
			if (clazz.isAssignableFrom(List.class)) {
				nodeTable.createListColumn(column, elementClazz, false);
			} else {
				nodeTable.createColumn(column, clazz, false);
			}
		}
	}

	private DoubleClickAction convertDoubleClick(String v) {
		for (DoubleClickAction action: DoubleClickAction.values()) {
			if (action.toString().equals(v))
				return action;
		}
		return null;
	}

	private GroupViewType convertGroupViewType(String v) {
		for (GroupViewType type: GroupViewType.values()) {
			if (type.toString().equals(v))
				return type;
		}
		return null;
	}

	/***************************************************************************
	 *                              Inner Classes                              *
	 **************************************************************************/

	class GroupSpecificMaps {
		Map<Class<?>, Aggregator<?>> defMap;
		Map<Class<?>, Aggregator<?>> defListMap;
		Map<CyColumn, Aggregator<?>> ovMap;

		GroupSpecificMaps () {
			this.defMap = null;
			this.defListMap = null;
			this.ovMap = null;
		}

		GroupSpecificMaps (Map<Class<?>, Aggregator<?>> defMap, 
		                   Map<Class<?>, Aggregator<?>> defListMap,
		                   Map<CyColumn, Aggregator<?>> ovMap) {
			this.defMap = defMap;
			this.defListMap = defListMap;
			this.ovMap = ovMap;
		}

		void setDefault(Class<?> ovClass, Aggregator<?> agg) {
			if (defMap == null) defMap = new HashMap<Class<?>, Aggregator<?>>();

			defMap.put(ovClass, agg);
		}

		void setListDefault(Class<?> ovClass, Aggregator<?> agg) {
			if (defListMap == null) defListMap = new HashMap<Class<?>, Aggregator<?>>();

			defListMap.put(ovClass, agg);
		}

		void setOverride(CyColumn column, Aggregator<?> agg) {
			if (ovMap == null) ovMap = new HashMap<CyColumn, Aggregator<?>>();

			ovMap.put(column, agg);
		}

		Aggregator<?> getDefault (Class<?> c) {
			if (defMap != null && defMap.containsKey(c))
				return defMap.get(c);
			return null;
		}

		Aggregator<?> getListDefault (Class<?> c) {
			if (defListMap != null && defListMap.containsKey(c))
				return defListMap.get(c);
			return null;
		}

		Aggregator<?> getOverride(CyColumn c) {
			if (ovMap != null && ovMap.containsKey(c))
				return ovMap.get(c);
			return null;
		}

		Map<Class<?>,Aggregator<?>> getDefaults() {return defMap;}
		Map<Class<?>,Aggregator<?>> getListDefaults() {return defListMap;}
		Map<CyColumn,Aggregator<?>> getOverrides() {return ovMap;}
	}

	class PropsReader extends AbstractConfigDirPropsReader {
		PropsReader() {
			super("groupSettings", "groupSettings.props", CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR);
		}
	}
}
