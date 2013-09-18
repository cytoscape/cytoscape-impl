package org.cytoscape.biopax.internal.util;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
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
		return (abbreviationString != null && !abbreviationString.isEmpty())
				? abbreviationString : "";
	}
}
