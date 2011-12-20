package org.cytoscape.cpath2.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A class to store a given nodes's
 * chemical modification(s), etc.,
 * along with a string of abbreviations for the respective attribute
 * (which is used in the construction of the node label).
 */
public class NodeAttributesWrapper {
	// map of cellular location
	// or chemical modifications
	private Map<String, Object> attributesMap;
	// abbreviations string
	private String abbreviationString;

	// contructor
	public NodeAttributesWrapper(Map<String,Object> attributesMap, String abbreviationString) {
		this.attributesMap = attributesMap;
		this.abbreviationString = abbreviationString;
	}

	// gets the attributes map
	public Map<String,Object> getMap() {
		return attributesMap;
	}

	// gets the attributes map as list
	public List<String> getList() {
		return (attributesMap != null) ? new ArrayList<String>(attributesMap.keySet()) : null;
	}

	// gets the abbrevation string (used in node label)
	public String getAbbreviationString() {
		return abbreviationString;
	}
}
