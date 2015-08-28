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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import org.cytoscape.group.internal.data.aggregators.NoneAggregator;

public class CyGroupAggregationSettings {
	
	final CyGroupAggregationManager cyAggManager;
	final CyGroupSettingsImpl settings;
	final CyGroupManagerImpl cyGroupMgr;
	final CyApplicationManager appMgr;
	CyNetwork currentNetwork = null;
	Map<CyColumn, Aggregator<?>> overrides;

	/**********************************
	 * Default aggregation attributes *
	 *********************************/
	// Default aggregations

	@Tunable(description="Enable attribute aggregation:", 
	         groups={"Attribute Aggregation Settings"}, gravity=10.0)
	public boolean enableAttributeAggregation = false;

	// Integer
	@Tunable(description="Integer column aggregation default:", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation Settings"}, 
	         params="displayState=collapsed",
	         dependsOn="enableAttributeAggregation=true", gravity=11.0)
	public ListSingleSelection<Aggregator<?>> integerDefault;

	// Long
	@Tunable(description="Long column aggregation default:", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation Settings"},
	         dependsOn="enableAttributeAggregation=true", gravity=12.0)
	public ListSingleSelection<Aggregator<?>> longDefault;

	// Float
	@Tunable(description="Float column aggregation default:", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation Settings"},
	         dependsOn="enableAttributeAggregation=true", gravity=13.0)
	public ListSingleSelection<Aggregator<?>> floatDefault;

	// Double
	@Tunable(description="Double column aggregation default:", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation Settings"},
	         dependsOn="enableAttributeAggregation=true", gravity=14.0)
	public ListSingleSelection<Aggregator<?>> doubleDefault;

	// String
	@Tunable(description="String column aggregation default:", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation Settings"},
	         dependsOn="enableAttributeAggregation=true", gravity=15.0)
	public ListSingleSelection<Aggregator<?>> stringDefault;

	// Boolean
	@Tunable(description="Boolean column aggregation default:", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation Settings"},
	         dependsOn="enableAttributeAggregation=true", gravity=16.0)
	public ListSingleSelection<Aggregator<?>> booleanDefault;

	// String List
	@Tunable(description="String List column aggregation default:", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation Settings"},
	         dependsOn="enableAttributeAggregation=true", gravity=17.0)
	public ListSingleSelection<Aggregator<?>> stringListDefault;

	// Integer List
	@Tunable(description="Integer List column aggregation default:", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation Settings"},
	         dependsOn="enableAttributeAggregation=true", gravity=17.1)
	public ListSingleSelection<Aggregator<?>> integerListDefault;

	// Long List
	@Tunable(description="Long List column aggregation default:", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation Settings"},
	         dependsOn="enableAttributeAggregation=true", gravity=17.2)
	public ListSingleSelection<Aggregator<?>> longListDefault;

	// Float List
	@Tunable(description="Float List column aggregation default:", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation Settings"},
	         dependsOn="enableAttributeAggregation=true", gravity=17.3)
	public ListSingleSelection<Aggregator<?>> floatListDefault;

	// Double List
	@Tunable(description="Double List column aggregation default:", 
	         groups={"Attribute Aggregation Settings", "Default Aggregation Settings"},
	         dependsOn="enableAttributeAggregation=true", gravity=17.4)
	public ListSingleSelection<Aggregator<?>> doubleListDefault;

	/**********************************
	 * Default aggregation overrides  *
	 *********************************/
	public ListSingleSelection<String> attrSelection = 
		new ListSingleSelection<String>(Collections.singletonList("No attributes available"));

	@Tunable(description="Attribute to override:", 
	         groups={"Attribute Aggregation Settings", "Aggregation Overrides"},
	         dependsOn="enableAttributeAggregation=true", 
	         params="displayState=collapsed", gravity=20.0)
	public ListSingleSelection<String> getAttrSelection() {
		// Now, build the list of attributes -- we'll focus on 
		// node attributes for now
		List<String> attrList = new ArrayList<String>();
		currentNetwork = appMgr.getCurrentNetwork();
		if (currentNetwork != null) {
			for (CyColumn column: currentNetwork.getDefaultNodeTable().getColumns()) {
				if (column.getName().equals(CyNetwork.SUID)) continue;
				attrList.add(column.getName());
			}
		} else {
			attrList.add("No attributes available");
		}
		attrSelection = new ListSingleSelection<String>(attrList);
		return attrSelection;
	}
	public void setAttrSelection(ListSingleSelection<String> input) {
		// Ignore because ListSingleSelection is set in the handler and not here.
	}

	@Tunable(description="Attribute Type:",
	         groups={"Attribute Aggregation Settings", "Aggregation Overrides"},
	         dependsOn="enableAttributeAggregation=true",
	         listenForChange={"AttrSelection"}, gravity=21.0)
  public String getAttrType() {
		if (currentNetwork == null || attrSelection == null || 
		    attrSelection.getSelectedValue() == null ||
		    attrSelection.getSelectedValue().length() == 0) {
			if (aggregationType != null)
				aggregationType.setPossibleValues(cyAggManager.getAggregators(NoneAggregator.class));
			else
				aggregationType = new ListSingleSelection<Aggregator<?>>(cyAggManager.getAggregators(NoneAggregator.class));
			return "-- No Network --";
		}

		// Get the attribute from the selection
		String columnName = attrSelection.getSelectedValue();
		CyTable nodeTable = currentNetwork.getDefaultNodeTable();
		CyColumn column = nodeTable.getColumn(columnName);
		if (column == null) return "-- No Such Column -- ";

		if (aggregationType != null) {
			if (column.getType().equals(List.class))
				aggregationType.setPossibleValues(cyAggManager.getListAggregators(column.getListElementType()));
			else
				aggregationType.setPossibleValues(cyAggManager.getAggregators(column.getType()));
		} else {
			if (column.getType().equals(List.class))
				aggregationType = 
								new ListSingleSelection<Aggregator<?>>(cyAggManager.getListAggregators(column.getListElementType()));
			else
				aggregationType = new ListSingleSelection<Aggregator<?>>(cyAggManager.getAggregators(column.getType()));
		}

		// Now, if we already have an override for this attribute, make sure that
		// it's reflected in what the user sees
		if (aggregationType.getSelectedValue() == null) {
			Aggregator<?> type = settings.getOverrideAggregation(column);
			if (type != null) aggregationType.setSelectedValue(type);
		}

		// Get it's type
		String t = column.getType().getSimpleName();
		if (column.getType().equals(List.class))
			t += " of "+column.getListElementType().getSimpleName()+"s";
		return t;
	}
	public void setAttrType(String t) {
	}

	private ListSingleSelection<Aggregator<?>> aggregationType = null;
	@Tunable(description="Aggregation Type:",
	         groups={"Attribute Aggregation Settings", "Aggregation Overrides"},
	         dependsOn="enableAttributeAggregation=true",
	         listenForChange={"AttrSelection"}, gravity=22.0)
	public ListSingleSelection<Aggregator<?>> getAggregationType() {   
		// We need to do this because Cytoscape's Tunables processing doesn't correctly
		// order listenForChange initializations
		if (aggregationType == null) {
			aggregationType = new ListSingleSelection<Aggregator<?>>(cyAggManager.getAggregators(NoneAggregator.class));
		}
		return aggregationType;
	}

	public void setAggregationType(ListSingleSelection<Aggregator<?>> input) {
		if (currentNetwork == null ||
		    aggregationType == null || aggregationType.getSelectedValue() == null) return;
		
		String columnName = attrSelection.getSelectedValue();
		CyTable nodeTable = currentNetwork.getDefaultNodeTable();
		CyColumn column = nodeTable.getColumn(columnName);
		Aggregator<?> agg = aggregationType.getSelectedValue();
		overrides.put(column, agg);
	}

	public CyGroupAggregationSettings(final CyGroupManagerImpl cyGroupMgr,
		                                final CyGroupAggregationManager cyAggManager,
	                                  final CyGroupSettingsImpl settings) {
		this.cyGroupMgr = cyGroupMgr;
		this.cyAggManager = cyAggManager;
		this.settings = settings;
		this.appMgr = cyGroupMgr.getService(CyApplicationManager.class);
		this.overrides = new HashMap<CyColumn, Aggregator<?>>();
		this.enableAttributeAggregation = settings.getEnableAttributeAggregation();

		initializeDefaults();
	}

	public Map<CyColumn, Aggregator<?>> getOverrideMap() {
		return overrides;
	}

	public boolean getAttributeAggregationEnabled() {
		return enableAttributeAggregation;
	}

	public Aggregator<?> getDefaultAggregator(Class<?> c) {
		if (c.equals(Integer.class))
			return integerDefault.getSelectedValue();
		else if (c.equals(Long.class))
			return longDefault.getSelectedValue();
		else if (c.equals(Float.class))
			return floatDefault.getSelectedValue();
		else if (c.equals(Double.class))
			return doubleDefault.getSelectedValue();
		else if (c.equals(String.class))
			return stringDefault.getSelectedValue();
		else if (c.equals(List.class))
			return getDefaultListAggregator(String.class);
		else if (c.equals(Boolean.class))
			return booleanDefault.getSelectedValue();
		return null;
	}

	public Aggregator<?> getDefaultListAggregator(Class<?> c) {
		if (c.equals(String.class))
			return stringListDefault.getSelectedValue();
		if (c.equals(Integer.class))
			return integerListDefault.getSelectedValue();
		if (c.equals(Long.class))
			return longListDefault.getSelectedValue();
		if (c.equals(Float.class))
			return floatListDefault.getSelectedValue();
		if (c.equals(Double.class))
			return doubleListDefault.getSelectedValue();
		return null;
	}

	private void initializeDefaults() {
		// Create the selections
		integerDefault = createDefaults(Integer.class, AttributeHandlingType.AVG);
		longDefault = createDefaults(Long.class, AttributeHandlingType.NONE);
		floatDefault = createDefaults(Float.class, AttributeHandlingType.AVG);
		doubleDefault = createDefaults(Double.class, AttributeHandlingType.AVG);
		stringDefault = createDefaults(String.class, AttributeHandlingType.CSV);
		booleanDefault = createDefaults(Boolean.class, AttributeHandlingType.NONE);

		stringListDefault = createListDefaults(String.class, AttributeHandlingType.UNIQUE);
		integerListDefault = createListDefaults(Integer.class, AttributeHandlingType.AVG);
		longListDefault = createListDefaults(Long.class, AttributeHandlingType.AVG);
		floatListDefault = createListDefaults(Float.class, AttributeHandlingType.AVG);
		doubleListDefault = createListDefaults(Double.class, AttributeHandlingType.AVG);
	}

	private ListSingleSelection<Aggregator<?>> createDefaults(Class<?> c, 
	                                                          AttributeHandlingType type) {
			List<Aggregator<?>> aggs = cyAggManager.getAggregators(c);
			Aggregator<?> def = null;
			for (Aggregator<?> a: aggs) {
				if (a.toString().equals(type.toString())) {
					def = a;
					break;
				}
			}
			ListSingleSelection<Aggregator<?>> lss = new ListSingleSelection<Aggregator<?>>(aggs);

			if (def != null) {
				// If we've never initialized our default aggregations, do so now
				if (settings.getDefaultAggregation(def.getSupportedType()) == null) {
					settings.setDefaultAggregation(def.getSupportedType(), def); 
					lss.setSelectedValue(def);
				} else {
					lss.setSelectedValue(settings.getDefaultAggregation(def.getSupportedType()));
				}
			}
			return lss;
	}

	private ListSingleSelection<Aggregator<?>> createListDefaults(Class<?> c, 
	                                                              AttributeHandlingType type) {
			List<Aggregator<?>> aggs = cyAggManager.getListAggregators(c);
			Aggregator<?> def = null;
			for (Aggregator<?> a: aggs) {
				if (a.toString().equals(type.toString())) {
					def = a;
					break;
				}
			}
			ListSingleSelection<Aggregator<?>> lss = new ListSingleSelection<Aggregator<?>>(aggs);

			if (def != null) {
				// If we've never initialized our default aggregations, do so now
				if (settings.getDefaultAggregation(def.getSupportedType()) == null) {
					settings.setDefaultAggregation(def.getSupportedType(), def); 
					lss.setSelectedValue(def);
				} else {
					lss.setSelectedValue(settings.getDefaultAggregation(def.getSupportedType()));
				}
			}
			return lss;
	}
}
