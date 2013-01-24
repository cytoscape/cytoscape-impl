package org.cytoscape.search.internal.util;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
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



import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;


/**
 * This object will serve as input to the CustomMultiFieldQueryParser.
 * It contains attribute fields names and their types.
 * This way CustomMultiFieldQueryParser can recognize numeric attribute fields.
 */
public class AttributeFields {
	private final Map<String, Class<?>> columnTypeMap;

	public AttributeFields(final CyNetwork network) {
		this.columnTypeMap = new HashMap<String, Class<?>>();
		initFields(network);
	}

	/**
	 * Initialize this object with attribute fields names and their type.
	 * Eventually, fields[i] will hold attribute field name and types[i] will hold its type.
	 * fields[] and types[] contain both node and edge attributes.
	 * ID (INDEX_FIELD) is treated as another attribute of type string.
	 * There are probably better ways to do this, but there you go :)
	 */
	private void initFields(final CyNetwork network) {
		CyTable nodeCyDataTable = network.getDefaultNodeTable();
		for (final CyColumn column : nodeCyDataTable.getColumns())
			columnTypeMap.put(EnhancedSearchUtils.replaceWhitespace(column.getName()).toLowerCase(),
					  column.getType());

		CyTable edgeCyDataTable = network.getDefaultEdgeTable();
		for (final CyColumn column : edgeCyDataTable.getColumns())
			columnTypeMap.put(EnhancedSearchUtils.replaceWhitespace(column.getName()).toLowerCase(),
					  column.getType());
	}

	/**
	 * Get list of fields
	 */
	public String[] getFields() {
		final String[] keys = new String[columnTypeMap.size()];
		
		int i = 0;
		for (final String key : columnTypeMap.keySet())
			keys[i++] = key;

		return keys;
	}

	public Class<?> getType(final String attrName) {
		Class<?> valueType = columnTypeMap.get(attrName);
		return valueType;
	}
}
