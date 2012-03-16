
package org.cytoscape.group.data.internal;

import org.cytoscape.model.CyColumn;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
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

public class CyGroupSettingsImpl extends AbstractTask implements CyGroupSettings, GroupAddedListener {
	CyGroupManager cyGroupManager;
	Map<Class, Aggregator> allGroupDefaultMap;
	Map<CyColumn, Aggregator> allGroupOverrideMap;
	Map<CyGroup, GroupSpecificMaps> groupMap;


	@Tunable(description="Double-Click action", 
	         groups={"User Action Settings"}/*, params="displayState=collapse"*/)
  public ListSingleSelection<DoubleClickAction> doubleClickAction = 
		new ListSingleSelection<DoubleClickAction>(DoubleClickAction.None,DoubleClickAction.ExpandContract,
		                                           DoubleClickAction.Select);


	/**********************************
	 * Default aggregation attributes *
	 *********************************/
	// Default aggregations

	@Tunable(description="Enable attribute aggregation", groups={"Attribute Aggregation Settings"})
	public boolean enableAttributeAggregation = false;

	// Integer
	@Tunable(description="Integer column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"}, params="displayState=collapsed",
	         dependsOn="enableAttributeAggregation=true")
	public ListSingleSelection<AttributeHandlingType> integerDefault;

	// Long
	@Tunable(description="Long column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
	public ListSingleSelection<AttributeHandlingType> longDefault;

	// Float
	@Tunable(description="Float column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
	public ListSingleSelection<AttributeHandlingType> floatDefault;

	// Double
	@Tunable(description="Double column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
	public ListSingleSelection<AttributeHandlingType> doubleDefault;

	// List
	@Tunable(description="List column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
	public ListSingleSelection<AttributeHandlingType> listDefault;

	// String
	@Tunable(description="String column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
	public ListSingleSelection<AttributeHandlingType> stringDefault;

	// Boolean
	@Tunable(description="Boolean column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
	public ListSingleSelection<AttributeHandlingType> booleanDefault;


	public CyGroupSettingsImpl(CyGroupManager mgr) {
		this.cyGroupManager = mgr;
		allGroupDefaultMap = new HashMap<Class,Aggregator>();
		allGroupOverrideMap = new HashMap<CyColumn,Aggregator>();
		groupMap = new HashMap<CyGroup,GroupSpecificMaps>();

		initializeDefaults();

		// Set some defaults
		doubleClickAction.setSelectedValue(DoubleClickAction.ExpandContract);

	}

	// This is a little funky, but we don't really have a task, so we just provide the run method
	// and do nothing
	public void run (TaskMonitor taskMonitor) {}

	@Override
	public boolean getEnableAttributeAggregation() {
		return enableAttributeAggregation;
	}

	@Override
	public void setEnableAttributeAggregation(boolean aggregate) {
		this.enableAttributeAggregation = aggregate;
	}

	@Override
	public DoubleClickAction getDoubleClickAction() {
		return doubleClickAction.getSelectedValue();
	}

	@Override
	public void setDoubleClickAction(DoubleClickAction action) {
		doubleClickAction.setSelectedValue(action);
	}

	@Override
	public Aggregator getAggregator(CyGroup group, CyColumn column) {
		updateDefaults();
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
	public void setDefaultAggregation(CyGroup group, Class ovClass, Aggregator agg) {
		if (!groupMap.containsKey(group)) {
			groupMap.put(group, new GroupSpecificMaps());
		}
		groupMap.get(group).setDefault(ovClass, agg);
	}

	@Override
	public void setDefaultAggregation(Class ovClass, Aggregator agg) {
		allGroupDefaultMap.put(ovClass, agg);
	}

	@Override
	public void setOverrideAggregation(CyGroup group, CyColumn column, Aggregator agg) {
		if (!groupMap.containsKey(group)) {
			groupMap.put(group, new GroupSpecificMaps());
		}
		groupMap.get(group).setOverride(column, agg);
	}

	@Override
	public void setOverrideAggregation(CyColumn column, Aggregator agg) {
		allGroupOverrideMap.put(column, agg);
	}

	public void handleEvent(GroupAddedEvent e) {
		CyGroup addedGroup = e.getGroup();
		updateDefaults();
		Map<Class,Aggregator> defMap = new HashMap<Class, Aggregator>();
		for (Class cKey: allGroupDefaultMap.keySet())
			defMap.put(cKey, allGroupDefaultMap.get(cKey));
		Map<CyColumn,Aggregator> ovMap = new HashMap<CyColumn, Aggregator>();
		for (CyColumn cKey: allGroupOverrideMap.keySet())
			ovMap.put(cKey, allGroupOverrideMap.get(cKey));
		groupMap.put(addedGroup, new GroupSpecificMaps(defMap, ovMap));
	}

	private void updateDefaults() {
		// Update our defaults first
		allGroupDefaultMap.put(Boolean.class, new BooleanAggregator(booleanDefault.getSelectedValue()));
		allGroupDefaultMap.put(Integer.class, new IntegerAggregator(integerDefault.getSelectedValue()));
		allGroupDefaultMap.put(Float.class, new FloatAggregator(floatDefault.getSelectedValue()));
		allGroupDefaultMap.put(Long.class, new LongAggregator(longDefault.getSelectedValue()));
		allGroupDefaultMap.put(Double.class, new DoubleAggregator(doubleDefault.getSelectedValue()));
		allGroupDefaultMap.put(List.class, new ListAggregator(listDefault.getSelectedValue()));
		allGroupDefaultMap.put(String.class, new StringAggregator(stringDefault.getSelectedValue()));
	}

	private void initializeDefaults() {
		// Create the selection
		integerDefault = 
			new ListSingleSelection<AttributeHandlingType>(Arrays.asList(IntegerAggregator.getSupportedTypes()));
		longDefault = 
			new ListSingleSelection<AttributeHandlingType>(Arrays.asList(LongAggregator.getSupportedTypes()));
		floatDefault = 
			new ListSingleSelection<AttributeHandlingType>(Arrays.asList(FloatAggregator.getSupportedTypes()));
		doubleDefault = 
			new ListSingleSelection<AttributeHandlingType>(Arrays.asList(DoubleAggregator.getSupportedTypes()));
		stringDefault = 
			new ListSingleSelection<AttributeHandlingType>(Arrays.asList(StringAggregator.getSupportedTypes()));
		listDefault = 
			new ListSingleSelection<AttributeHandlingType>(Arrays.asList(ListAggregator.getSupportedTypes()));
		booleanDefault = 
			new ListSingleSelection<AttributeHandlingType>(Arrays.asList(BooleanAggregator.getSupportedTypes()));

		integerDefault.setSelectedValue(AttributeHandlingType.AVG);
		longDefault.setSelectedValue(AttributeHandlingType.NONE);
		floatDefault.setSelectedValue(AttributeHandlingType.AVG);
		doubleDefault.setSelectedValue(AttributeHandlingType.AVG);
		stringDefault.setSelectedValue(AttributeHandlingType.CSV);
		listDefault.setSelectedValue(AttributeHandlingType.UNIQUE);
		booleanDefault.setSelectedValue(AttributeHandlingType.NONE);

		// Initialize the defaults
		allGroupDefaultMap.put(Integer.class, new IntegerAggregator(AttributeHandlingType.AVG));
		allGroupDefaultMap.put(Long.class, new LongAggregator(AttributeHandlingType.NONE));
		allGroupDefaultMap.put(Float.class, new FloatAggregator(AttributeHandlingType.AVG));
		allGroupDefaultMap.put(Double.class, new DoubleAggregator(AttributeHandlingType.AVG));
		allGroupDefaultMap.put(String.class, new StringAggregator(AttributeHandlingType.CSV));
		allGroupDefaultMap.put(List.class, new ListAggregator(AttributeHandlingType.UNIQUE));
		allGroupDefaultMap.put(Boolean.class, new BooleanAggregator(AttributeHandlingType.NONE));
	}

	class GroupSpecificMaps {
		Map<Class, Aggregator> defMap;
		Map<CyColumn, Aggregator> ovMap;

		GroupSpecificMaps () {
			this.defMap = null;
			this.ovMap = null;
		}

		GroupSpecificMaps (Map<Class, Aggregator> defMap, Map<CyColumn, Aggregator> ovMap) {
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

		Map<Class,Aggregator> getDefaults() {return defMap;}
		Map<CyColumn,Aggregator> getOverrides() {return ovMap;}
	}

}
