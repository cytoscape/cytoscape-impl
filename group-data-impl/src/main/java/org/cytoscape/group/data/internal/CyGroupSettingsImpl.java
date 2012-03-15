
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
	@Tunable(description="Enable attribute aggregation", groups={"Attribute Aggregation Settings"})
	public boolean enableAttributeAggregation = false;

	// Integer
	@Tunable(description="Integer column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"}, params="displayState=collapsed",
	         dependsOn="enableAttributeAggregation=true")
  public ListSingleSelection<AttributeHandlingType> getIntegerAggregationDefault() {
		// Get the list of options
		List<AttributeHandlingType> options = Arrays.asList(IntegerAggregator.getSupportedTypes());

		// Create the selection
		ListSingleSelection<AttributeHandlingType> types = 
			new ListSingleSelection<AttributeHandlingType>(options);

		// Initialize it to our default
		types.setSelectedValue(AttributeHandlingType.AVG);
		return types;
	}

	public void setIntegerAggregationDefault(ListSingleSelection<AttributeHandlingType> input) {
		allGroupDefaultMap.put(Integer.class, new IntegerAggregator(input.getSelectedValue()));
	}

	// Long
	@Tunable(description="Long column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
  public ListSingleSelection<AttributeHandlingType> getLongAggregationDefault() {
		// Get the list of options
		List<AttributeHandlingType> options = Arrays.asList(LongAggregator.getSupportedTypes());

		// Create the selection
		ListSingleSelection<AttributeHandlingType> types = 
			new ListSingleSelection<AttributeHandlingType>(options);

		// Initialize it to our default
		types.setSelectedValue(AttributeHandlingType.AVG);
		return types;
	}

	public void setLongAggregationDefault(ListSingleSelection<AttributeHandlingType> input) {
		allGroupDefaultMap.put(Long.class, new LongAggregator(input.getSelectedValue()));
	}

	// Float
	@Tunable(description="Float column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
  public ListSingleSelection<AttributeHandlingType> getFloatAggregationDefault() {
		// Get the list of options
		List<AttributeHandlingType> options = Arrays.asList(FloatAggregator.getSupportedTypes());

		// Create the selection
		ListSingleSelection<AttributeHandlingType> types = 
			new ListSingleSelection<AttributeHandlingType>(options);

		// Initialize it to our default
		types.setSelectedValue(AttributeHandlingType.AVG);
		return types;
	}

	public void setFloatAggregationDefault(ListSingleSelection<AttributeHandlingType> input) {
		allGroupDefaultMap.put(Float.class, new FloatAggregator(input.getSelectedValue()));
	}

	// Double
	@Tunable(description="Double column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
  public ListSingleSelection<AttributeHandlingType> getDoubleAggregationDefault() {
		// Get the list of options
		List<AttributeHandlingType> options = Arrays.asList(DoubleAggregator.getSupportedTypes());

		// Create the selection
		ListSingleSelection<AttributeHandlingType> types = 
			new ListSingleSelection<AttributeHandlingType>(options);

		// Initialize it to our default
		types.setSelectedValue(AttributeHandlingType.AVG);
		return types;
	}

	public void setDoubleAggregationDefault(ListSingleSelection<AttributeHandlingType> input) {
		allGroupDefaultMap.put(Double.class, new DoubleAggregator(input.getSelectedValue()));
	}


	// List
	@Tunable(description="List column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
  public ListSingleSelection<AttributeHandlingType> getListAggregationDefault() {
		// Get the list of options
		List<AttributeHandlingType> options = Arrays.asList(ListAggregator.getSupportedTypes());

		// Create the selection
		ListSingleSelection<AttributeHandlingType> types = 
			new ListSingleSelection<AttributeHandlingType>(options);

		// Initialize it to our default
		types.setSelectedValue(AttributeHandlingType.UNIQUE);
		return types;
	}

	public void setListAggregationDefault(ListSingleSelection<AttributeHandlingType> input) {
		allGroupDefaultMap.put(List.class, new ListAggregator(input.getSelectedValue()));
	}

	// String
	@Tunable(description="String column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
  public ListSingleSelection<AttributeHandlingType> getStringAggregationDefault() {
		// Get the list of options
		List<AttributeHandlingType> options = Arrays.asList(StringAggregator.getSupportedTypes());

		// Create the selection
		ListSingleSelection<AttributeHandlingType> types = 
			new ListSingleSelection<AttributeHandlingType>(options);

		// Initialize it to our default
		types.setSelectedValue(AttributeHandlingType.TSV);
		return types;
	}

	public void setStringAggregationDefault(ListSingleSelection<AttributeHandlingType> input) {
		allGroupDefaultMap.put(String.class, new StringAggregator(input.getSelectedValue()));
	}

	// Boolean
	@Tunable(description="Boolean column aggregation default", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation"},
	         dependsOn="enableAttributeAggregation=true")
  public ListSingleSelection<AttributeHandlingType> getBooleanAggregationDefault() {
		// Get the list of options
		List<AttributeHandlingType> options = Arrays.asList(BooleanAggregator.getSupportedTypes());

		// Create the selection
		ListSingleSelection<AttributeHandlingType> types = 
			new ListSingleSelection<AttributeHandlingType>(options);

		// Initialize it to our default
		types.setSelectedValue(AttributeHandlingType.OR);
		return types;
	}

	public void setBooleanAggregationDefault(ListSingleSelection<AttributeHandlingType> input) {
		allGroupDefaultMap.put(Boolean.class, new BooleanAggregator(input.getSelectedValue()));
	}


	public CyGroupSettingsImpl(CyGroupManager mgr) {
		this.cyGroupManager = mgr;
		allGroupDefaultMap = new HashMap<Class,Aggregator>();
		allGroupOverrideMap = new HashMap<CyColumn,Aggregator>();
		groupMap = new HashMap<CyGroup,GroupSpecificMaps>();

		// Set some defaults
		doubleClickAction.setSelectedValue(DoubleClickAction.ExpandContract);

		// Build the options
		

		// Initialize the defaults
		allGroupDefaultMap.put(Boolean.class, new BooleanAggregator(AttributeHandlingType.NONE));
		allGroupDefaultMap.put(Double.class, new DoubleAggregator(AttributeHandlingType.AVG));
		allGroupDefaultMap.put(Long.class, new LongAggregator(AttributeHandlingType.NONE));
		allGroupDefaultMap.put(Integer.class, new IntegerAggregator(AttributeHandlingType.SUM));
		allGroupDefaultMap.put(Float.class, new FloatAggregator(AttributeHandlingType.SUM));
		allGroupDefaultMap.put(String.class, new StringAggregator(AttributeHandlingType.MCV));
		allGroupDefaultMap.put(List.class, new ListAggregator(AttributeHandlingType.UNIQUE));
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
