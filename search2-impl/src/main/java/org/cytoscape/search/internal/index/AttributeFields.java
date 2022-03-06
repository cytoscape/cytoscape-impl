package org.cytoscape.search.internal.index;

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
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;


/**
 * This object will serve as input to the CustomMultiFieldQueryParser.
 * It contains attribute fields names and their types.
 */
public class AttributeFields {
	
	private final Map<String,Class<?>> columnTypeMap = new HashMap<>();
	private final Map<String,String> partNameMap = new HashMap<>();

	
	public AttributeFields(CyNetwork network) {
		initFields(network.getDefaultNodeTable());
		initFields(network.getDefaultEdgeTable());
	}

	public AttributeFields(CyTable table) {
		initFields(table);
	}
	
	
	private void initFields(CyTable table) {
		for(CyColumn column : table.getColumns()) {
			
			String fullName = column.getName().toLowerCase();
			
			if(fullName != null) {
				Class<?> type = column.getType();
				if(type == List.class)
					type = column.getListElementType();
				
				columnTypeMap.putIfAbsent(fullName, type);
				
				String partName = column.getNameOnly().toLowerCase();
				if(!fullName.equals(partName)) {
					partNameMap.putIfAbsent(partName, fullName);
				}
			}
		}
	}

	public String[] getFields() {
		return columnTypeMap.keySet().toArray(String[]::new);
	}

	public Class<?> getType(String fullName) {
		return columnTypeMap.get(fullName.toLowerCase());
	}
	
	public String getFullName(String partName) {
		return partNameMap.get(partName);
	}
	
	@Override
	public String toString() {
		return "AttributeFields(" + columnTypeMap.toString() + ") parts:"+partNameMap;
	}
	
}
