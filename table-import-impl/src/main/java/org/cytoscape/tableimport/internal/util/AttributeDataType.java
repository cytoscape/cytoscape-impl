package org.cytoscape.tableimport.internal.util;

import java.util.List;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

public enum AttributeDataType {
	TYPE_STRING(String.class, null, "ab", "String"),
	TYPE_INTEGER(Integer.class, null, "1", "Integer"),
	TYPE_FLOATING(Double.class, null, "1.0", "Floating Point"),
	TYPE_BOOLEAN(Boolean.class, null, "y/n", "Boolean"),
	TYPE_STRING_LIST(List.class, String.class, "[ ab ]", "List of Strings"),
	TYPE_INTEGER_LIST(List.class, Integer.class, "[ 1 ]", "List of Integers"),
	TYPE_FLOATING_LIST(List.class, Double.class, "[ 1.0 ]", "List of Floating Point Numbers"),
	TYPE_BOOLEAN_LIST(List.class, Boolean.class, "[ y/n ]", "List of Booleans");
	
	private final Class<?> type;
	private final Class<?> listType;
	private final String text;
	private final String description;
	
    private AttributeDataType(final Class<?> type, final Class<?> listType, final String text, final String description) {
		this.type = type;
		this.listType = listType;
		this.text = text;
		this.description = description;
	}
    
    public Class<?> getType() {
		return type;
	}
    
    public Class<?> getListType() {
		return listType;
	}
    
    public boolean isList() {
    	return listType != null;
    }
    
    public String getText() {
		return text;
	}
    
    public String getDescription() {
		return description;
	}
}
