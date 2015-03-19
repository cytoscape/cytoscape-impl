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

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.ContainsTunables;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.CyGroupSettingsManager.DoubleClickAction;
import org.cytoscape.group.CyGroupSettingsManager.GroupViewType;
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
import org.cytoscape.service.util.CyServiceRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides the context for both the global group settings and the
 * group-specific group settings.
 */
public class CyGroupSettingsImpl implements GroupAddedListener, CyGroupSettingsManager {
	final CyGroupManagerImpl cyGroupManager;
	final CyGroupAggregationManagerImpl cyAggManager;
	final CyServiceRegistrar cyServiceRegistrar;
	final CyApplicationManager appMgr;

	CyProperty<Properties> groupSettingsProperties = null;

	// Column names for serialization/deserialization of group settings
	final static String AGGREGATION_SETTINGS = "__AggregationSettings";
	final static String AGGREGATION_OVERRIDE_SETTINGS = "__AggregationOverrideSettings";
	final static String VIEW_SETTINGS = "__ViewSettings";

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
	final static String DEFAULT_FLOAT_AGGREGATION = "defaultFloatAggregation";
	final static String DEFAULT_DOUBLE_AGGREGATION = "defaultDoubleAggregation";
	final static String DEFAULT_STRING_AGGREGATION = "defaultStringAggregation";
	final static String DEFAULT_BOOLEAN_AGGREGATION = "defaultBooleanAggregation";
	final static String DEFAULT_LIST_AGGREGATION = "defaultListAggregation";
	final static String OVERRIDE_AGGREGATION = "overrideColumnAggregations";

	Map<Class, Aggregator> allGroupDefaultMap;
	Map<CyColumn, Aggregator> allGroupOverrideMap;
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
	
	private final Object lock = new Object();

	public CyGroupSettingsImpl(final CyGroupManagerImpl mgr, 
	                           final CyGroupAggregationManagerImpl aggMgr,
	                           final CyServiceRegistrar registrar) {
		this.cyGroupManager = mgr;
		this.cyAggManager = aggMgr;
		this.cyServiceRegistrar = registrar;
		this.appMgr = cyGroupManager.getService(CyApplicationManager.class);

		allGroupDefaultMap = new HashMap<Class,Aggregator>();
		allGroupOverrideMap = new HashMap<CyColumn,Aggregator>();
		allGroupOverridePropertyMap = new HashMap<String, String>(); // Special map for loading initial properties
		groupMap = new HashMap<CyGroup,GroupSpecificMaps>();
		
		groupActionMap = new ConcurrentHashMap<CyGroup, DoubleClickAction>(16, 0.75f, 2);
		groupViewTypeMap = new ConcurrentHashMap<CyGroup, GroupViewType>(16, 0.75f, 2);
		enableMap = new ConcurrentHashMap<CyGroup, Boolean>(16, 0.75f, 2);
		nestedNetworkMap = new ConcurrentHashMap<CyGroup, Boolean>(16, 0.75f, 2);
		opacityMap = new ConcurrentHashMap<CyGroup, Double>(16, 0.75f, 2);

		// Create our properties reader
		groupSettingsProperties = new PropsReader();

		// Initialize our defaults from the properties
		loadProperties();

		Properties serviceProperties = new Properties();
		serviceProperties.setProperty("cyPropertyName","groupSettings.props");

		cyServiceRegistrar.registerService(groupSettingsProperties, CyProperty.class, serviceProperties);
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
		if (groupViewTypeMap.containsKey(group))
			return groupViewTypeMap.get(group);
		return groupViewType;
	}

	@Override
	public void setGroupViewType(GroupViewType groupViewType) {
		this.groupViewType = groupViewType;
		updateProperties();
	}

	@Override
	public void setGroupViewType(CyGroup group, GroupViewType groupViewType) {
		if (group != null) {
			groupViewTypeMap.put(group, groupViewType);
			updateSettingsInTable(group);
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
	public Aggregator getAggregator(CyGroup group, CyColumn column) {
		Class type = column.getType();
		synchronized (lock) {
			// First, make sure the our override map is up-to-date
			// This will check to see if we have a property for this
			// and update it if we do
			getOverrideAggregation(column);
			Map<CyColumn, Aggregator> overrideMap = allGroupOverrideMap;

			Map<Class, Aggregator> defaultMap = allGroupDefaultMap;
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps != null) {
				defaultMap = groupSpecificMaps.getDefaults();
				overrideMap = groupSpecificMaps.getOverrides();
			}
			Aggregator aggregator = overrideMap.get(column.getName());
			if (aggregator != null) {
				return aggregator;
			}
			return defaultMap.get(column.getType());
		}
	}

	// Update all of our maps when we add a new group
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
			// Now override these with any settings from the table
			loadSettingsFromTable(addedGroup);
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
	public Aggregator getDefaultAggregation(Class ovClass) {
		synchronized (lock) {
			return get(allGroupDefaultMap, ovClass, null);
		}
	}

	@Override
	public void setDefaultAggregation(Class ovClass, Aggregator agg) {
		synchronized (lock) {
			allGroupDefaultMap.put(ovClass, agg);
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
	public Aggregator getOverrideAggregation(CyColumn column) {
		synchronized (lock) {
			// First, check and see if we have an entry for this column
			if (allGroupOverrideMap.containsKey(column))
				return get(allGroupOverrideMap, column, null);
			// We don't, so see if there is a default property
			if (allGroupOverridePropertyMap.containsKey(column.getName())) {
				// Great!  Now add that to our allGroupOverrideMap
				String aggrName = allGroupOverridePropertyMap.get(column.getName());
				Aggregator aggr = cyAggManager.getAggregator(column.getType(), aggrName);
				if (aggr != null)
					allGroupOverrideMap.put(column, aggr);
				return aggr;
			}
		}
		return null;
	}

	@Override
	public void setOverrideAggregation(CyColumn column, Aggregator agg) {
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
	public Aggregator getDefaultAggregation(CyGroup group, Class ovClass) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps != null)
				return groupSpecificMaps.getDefault(ovClass);
			return null;
		}
	}

	@Override
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
		updateSettingsInTable(group);
	}

	@Override
	public void setDefaultAggregation(CyGroup group, 
	                                  Class ovClass, String agg) {
		Aggregator def = cyAggManager.getAggregator(ovClass, agg);

		if (def != null)
			setDefaultAggregation(group, ovClass, def);
	}

	@Override
	public Aggregator getOverrideAggregation(CyGroup group, CyColumn column) {
		synchronized (lock) {
			GroupSpecificMaps groupSpecificMaps = groupMap.get(group);
			if (groupSpecificMaps != null)
				return groupSpecificMaps.getOverride(column);
			return null;
		}
	}

	@Override
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
		setAggregationProperty(p, DEFAULT_INT_AGGREGATION, Integer.class);
		setAggregationProperty(p, DEFAULT_LONG_AGGREGATION, Long.class);
		setAggregationProperty(p, DEFAULT_FLOAT_AGGREGATION, Float.class);
		setAggregationProperty(p, DEFAULT_DOUBLE_AGGREGATION, Double.class);
		setAggregationProperty(p, DEFAULT_STRING_AGGREGATION, String.class);
		setAggregationProperty(p, DEFAULT_LIST_AGGREGATION, List.class);
		setAggregationProperty(p, DEFAULT_BOOLEAN_AGGREGATION, Boolean.class);

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

		getAggregationProperty(p, DEFAULT_INT_AGGREGATION, Integer.class, AttributeHandlingType.AVG);
		getAggregationProperty(p, DEFAULT_LONG_AGGREGATION, Long.class, AttributeHandlingType.NONE);
		getAggregationProperty(p, DEFAULT_FLOAT_AGGREGATION, Float.class, AttributeHandlingType.AVG);
		getAggregationProperty(p, DEFAULT_DOUBLE_AGGREGATION, Double.class, AttributeHandlingType.AVG);
		getAggregationProperty(p, DEFAULT_STRING_AGGREGATION, String.class, AttributeHandlingType.CSV);
		getAggregationProperty(p, DEFAULT_LIST_AGGREGATION, List.class, AttributeHandlingType.UNIQUE);
		getAggregationProperty(p, DEFAULT_BOOLEAN_AGGREGATION, Boolean.class, AttributeHandlingType.NONE);

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

	private Aggregator getAggregationProperty(Properties p, String key, Class<?> c, AttributeHandlingType defType) {
		List<Aggregator> aggs = cyAggManager.getAggregators(c);
		String pValue = p.getProperty(key, defType.toString());

		Aggregator def = cyAggManager.getAggregator(c, pValue);

		if (def != null)
			allGroupDefaultMap.put(c, def);

		return def;
	}

	private void setAggregationProperty(Properties p, String key, Class<?> clazz) {
		if (allGroupDefaultMap.containsKey(clazz)) {
			Aggregator a = allGroupDefaultMap.get(clazz);
			if (a != null)
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
		CyNetwork currentNetwork = appMgr.getCurrentNetwork();
		List<String> viewSettings = new ArrayList<>();

		// Update all of our view settings as a list
		createColumnIfNeeded(currentNetwork, group.getGroupNode(), VIEW_SETTINGS, List.class, String.class);

		addViewSetting(viewSettings, DOUBLE_CLICK_ACTION, getDoubleClickAction(group).toString());
		addViewSetting(viewSettings, USE_NESTED_NETWORKS, ""+getUseNestedNetworks(group));
		addViewSetting(viewSettings, GROUP_VIEW_TYPE, ""+getGroupViewType(group));
		addViewSetting(viewSettings, GROUP_NODE_OPACITY, ""+getGroupNodeOpacity(group));
		// currentNetwork.getRow(group.getGroupNode(), CyNetwork.HIDDEN_ATTRS).set(VIEW_SETTINGS, viewSettings);
		currentNetwork.getRow(group.getGroupNode()).set(VIEW_SETTINGS, viewSettings);

		// Update default aggregations
		createColumnIfNeeded(currentNetwork, group.getGroupNode(), AGGREGATION_SETTINGS, List.class, String.class);
		List<String> aggrSettings = new ArrayList<>();
		aggrSettings.add(AGG_ENABLED+"="+getEnableAttributeAggregation(group));
		addAggregationSetting(group, DEFAULT_INT_AGGREGATION, Integer.class, aggrSettings);
		addAggregationSetting(group, DEFAULT_LONG_AGGREGATION, Long.class, aggrSettings);
		addAggregationSetting(group, DEFAULT_FLOAT_AGGREGATION, Float.class, aggrSettings);
		addAggregationSetting(group, DEFAULT_DOUBLE_AGGREGATION, Double.class, aggrSettings);
		addAggregationSetting(group, DEFAULT_STRING_AGGREGATION, String.class, aggrSettings);
		addAggregationSetting(group, DEFAULT_LIST_AGGREGATION, List.class, aggrSettings);
		addAggregationSetting(group, DEFAULT_BOOLEAN_AGGREGATION, Boolean.class, aggrSettings);

		// currentNetwork.getRow(group.getGroupNode(), CyNetwork.HIDDEN_ATTRS).set(AGGREGATION_SETTINGS, aggrSettings);
		currentNetwork.getRow(group.getGroupNode()).set(AGGREGATION_SETTINGS, aggrSettings);

		if (groupMap.containsKey(group)) {
			// Update overrides
			createColumnIfNeeded(currentNetwork, group.getGroupNode(), AGGREGATION_OVERRIDE_SETTINGS, List.class, String.class);
			List<String> aggrOverrideSettings = new ArrayList<>();
			GroupSpecificMaps gsm = groupMap.get(group);
			Map<CyColumn,Aggregator> overrides = groupMap.get(group).getOverrides();
			if (overrides == null || overrides.size() == 0)
				return;

			for (CyColumn column: overrides.keySet()) {
				aggrOverrideSettings.add(column.getName()+"="+overrides.get(column).toString());
			}

			// currentNetwork.getRow(group.getGroupNode(), CyNetwork.HIDDEN_ATTRS).set(AGGREGATION_OVERRIDE_SETTINGS, aggrOverrideSettings);
			currentNetwork.getRow(group.getGroupNode()).set(AGGREGATION_OVERRIDE_SETTINGS, aggrOverrideSettings);
		}
	}

	void addAggregationSetting(CyGroup group, String attribute, Class clazz, List<String> settings) {
		Aggregator agg = getDefaultAggregation(group, clazz);
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
	public boolean loadSettingsFromTable(CyGroup group) {
		CyNetwork currentNetwork = appMgr.getCurrentNetwork();
		CyTable table = currentNetwork.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		if (table.getColumn(VIEW_SETTINGS) == null)
			return false;

		List<String> viewSettings = table.getRow(group.getGroupNode()).getList(VIEW_SETTINGS, String.class);
		for (String setting: viewSettings) {
			updateSetting(group, setting);
		}

		List<String> aggrSettings = table.getRow(group.getGroupNode()).getList(AGGREGATION_SETTINGS, String.class);
		for (String setting: aggrSettings) {
			updateAggrSetting(group, setting);
		}

		List<String> aggrOverrideSettings = table.getRow(group.getGroupNode()).getList(AGGREGATION_OVERRIDE_SETTINGS, String.class);
		for (String setting: aggrSettings) {
			updateAggrOverrideSetting(group, setting);
		}
		
		return true;
	}

	void updateSetting(CyGroup group, String setting) {
		String[] pair = setting.split("=");
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
		} else if (DEFAULT_FLOAT_AGGREGATION.equals(pair[0])) {
			setDefaultAggregation(group, Float.class, pair[1]);
		} else if (DEFAULT_DOUBLE_AGGREGATION.equals(pair[0])) {
			setDefaultAggregation(group, Double.class, pair[1]);
		} else if (DEFAULT_STRING_AGGREGATION.equals(pair[0])) {
			setDefaultAggregation(group, String.class, pair[1]);
		} else if (DEFAULT_LIST_AGGREGATION.equals(pair[0])) {
			setDefaultAggregation(group, List.class, pair[1]);
		} else if (DEFAULT_BOOLEAN_AGGREGATION.equals(pair[0])) {
			setDefaultAggregation(group, Boolean.class, pair[1]);
		}
		
	}

	void updateAggrOverrideSetting(CyGroup group, String setting) {
		String[] pair = setting.split("=");
		CyNetwork currentNetwork = appMgr.getCurrentNetwork();
		CyColumn column = currentNetwork.getDefaultNodeTable().getColumn(pair[0]);
		if (column == null)
			return;
		Aggregator def = cyAggManager.getAggregator(column.getType(), pair[1]);
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
			System.out.println("override: "+override);
			String[] pair = override.split("=");
			results.add(pair);
		}
		return results;
	}

	/**
	 */
	public void createColumnIfNeeded(CyNetwork network, CyNode node, String column, Class clazz, Class elementClazz) {
		// CyTable nodeTable = network.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		CyTable nodeTable = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		if (nodeTable.getColumn(column) == null) {
			if (clazz.equals(List.class)) {
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

	class PropsReader extends AbstractConfigDirPropsReader {
		PropsReader() {
			super("groupSettings", "groupSettings.props", CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR);
		}
	}
}
