package org.cytoscape.search.internal.search;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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



import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;


/**
 * This object will serve as input to the CustomMultiFieldQueryParser.
 * It contains attribute fields names and their types.
 */
public class AttributeFields {
	
	private final Map<String, Class<?>> columnTypeMap = new HashMap<>();

	public AttributeFields(CyNetwork network) {
		initFields(network.getDefaultNodeTable());
		initFields(network.getDefaultEdgeTable());
	}

	public AttributeFields(CyTable table) {
		initFields(table);
	}
	
	/**
	 * Initialize this object with attribute fields names and their type.
	 * Eventually, fields[i] will hold attribute field name and types[i] will hold its type.
	 * fields[] and types[] contain both node and edge attributes.
	 * ID (INDEX_FIELD) is treated as another attribute of type string.
	 * There are probably better ways to do this, but there you go :)
	 */
	private void initFields(CyTable table) {
		for(CyColumn column : table.getColumns()) {
			String name = column.getName(); 
			if(name != null) {
				columnTypeMap.put(name.toLowerCase(), column.getType());
			}
		}
	}

	public String[] getFields() {
		String[] keys = new String[columnTypeMap.size()];
		
		int i = 0;
		for(String key : columnTypeMap.keySet()) {
			keys[i++] = key;
		}
		return keys;
	}

	public Class<?> getType(String name) {
		return columnTypeMap.get(name.toLowerCase());
	}
	
}
