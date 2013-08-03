package org.cytoscape.task.internal.utils;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;

public class ColumnValueTunable {
	
	@Tunable (description="Namespace for table", context="nogui")
	public String namespace = "default";

	@Tunable (description="List of columns to set", context="nogui")
	public String columnList;

	@Tunable (description="List of values", context="nogui")
	public String valueList;

	public ColumnValueTunable() {
	}

	public String getNamespace() { return namespace; }

	public List<CyColumn> getColumnList(CyTable table) {
		if (table == null) return null;

		if (columnList == null || columnList.equalsIgnoreCase("all"))
			return new ArrayList<CyColumn>(table.getColumns());

		String[] columns = columnList.split(",");
		List<CyColumn> returnValue = new ArrayList<CyColumn>();
		for (String column: columns) {
			CyColumn c = table.getColumn(column);
			if (c != null) returnValue.add(c);
		}
		return returnValue;
	}

	public List<String> getColumnNames(CyTable table) {
		if (table == null) return null;

		List<String> resultString = new ArrayList<String>();
		for (CyColumn column: getColumnList(table))
			resultString.add(column.getName());
		return resultString;
	}

	public Map<CyColumn, Object> getValueMap(CyTable table) {
		if (table == null) return null;
	
		Map<CyColumn, Object> returnValues = new HashMap<CyColumn, Object>();

		List<String> values = parseValues(valueList);
		int index = 0;
		for (CyColumn column: getColumnList(table)) {
			String value = values.get(index);
			Class type = column.getType(); // Get the type of the column
			returnValues.put(column, getValue(value, type, column));
			index++;
		}
		return returnValues;
	}

	private Object getValue(String value, Class type, CyColumn column) {
		if (value.length() == 0)
			return null;

		if (type.equals(List.class)) {
			Class elementType = column.getListElementType();
			String[] values = value.split(",");
			List<Object> vList = new ArrayList<Object>();
			for (String v: values) {
				vList.add(getValue(v, elementType, column));
			}
			return vList;
		} else if (type.equals(String.class)) {
			return value;
		} else if (type.equals(Integer.class)) {
			return Integer.valueOf(value);
		} else if (type.equals(Long.class)) {
			return Long.valueOf(value);
		} else if (type.equals(Double.class)) {
			return Double.valueOf(value);
		} else if (type.equals(Boolean.class)) {
			return Boolean.valueOf(value);
		}
		return null;
	}

	private List<String> parseValues(String values) {
		List<String> vList = new ArrayList<String>();

		boolean inList = false;
		String listString = "";
		String[] v1 = values.split(",");
		for (String v: v1) {
			v = v.trim();
			if (inList) {
				listString += ","+v;
				if (v.endsWith("]")) {
					vList.add(listString.substring(0, listString.length()-1));
					inList = false;
				}
			} else if (v.startsWith("[")) {
				inList = true;
				listString = v.substring(1);
			} else {
				vList.add(v);
			}
		}
		return vList;
	}
}
