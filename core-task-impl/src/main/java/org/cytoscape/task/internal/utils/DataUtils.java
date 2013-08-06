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

import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class DataUtils {

	public static String getNodeName(CyTable table, CyNode node) {
		String name = table.getRow(node.getSUID()).get(CyNetwork.NAME, String.class);
		name += " (SUID: "+node.getSUID()+")";
		return name;
	}

	public static String getEdgeName(CyTable table, CyEdge edge) {
		String name = table.getRow(edge.getSUID()).get(CyNetwork.NAME, String.class);
		name += " (SUID: "+edge.getSUID()+")";
		return name;
	}

	public static String convertData(Object data) {
		if (data instanceof List)
			return convertListToString((List)data);
		else if (data instanceof Map)
			return convertMapToString((Map)data);
		else
			return data.toString();
	}

	public static String convertMapToString(Map data) {
		String result = "{";
		for (Object key: data.keySet()) {
			Object v = data.get(key);
			if (v == null) continue;
			result += key.toString()+":";
			if (v instanceof List)
				result += convertListToString((List)v)+",";
			else if (v instanceof Map)
				result += convertMapToString((Map)v)+",";
			else
				result += v.toString()+",";
		}
		return result.substring(0, result.length()-1)+"}";
	}

	public static String convertListToString(List<Object> data) {
		String result = "[";
		for (Object v: data) {
			result += v.toString()+",";
		}
		return result.substring(0, result.length()-1)+"]";
	}

	public static String getNetworkTitle(CyNetwork network) {
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}

	public static Class getType(String type) {
		if ("double".equalsIgnoreCase(type))
			return Double.class;
		else if ("integer".equalsIgnoreCase(type))
			return Integer.class;
		else if ("long".equalsIgnoreCase(type))
			return Long.class;
		else if ("boolean".equalsIgnoreCase(type))
			return Boolean.class;
		else if ("string".equalsIgnoreCase(type))
			return String.class;
		else if ("list".equalsIgnoreCase(type))
			return List.class;

		return String.class;
	}

	public static String getType(Class type) {
		if (type.equals(Double.class))
			return "double";
		else if (type.equals(Integer.class))
			return "integer";
		else if (type.equals(Long.class))
			return "long";
		else if (type.equals(Boolean.class))
			return "boolean";
		else if (type.equals(String.class))
			return "string";
		else if (type.equals(List.class))
			return "list";
		else 
			return type.getName();
	}

	public static String getIdentifiableType(Class <? extends CyIdentifiable> type) {
		if (type.equals(CyNetwork.class)) return "Network";
		if (type.equals(CyNode.class)) return "Node";
		if (type.equals(CyEdge.class)) return "Edge";
		return "unknown";
	}

	public static Class<? extends CyIdentifiable> getIdentifiableClass(CyIdentifiable obj) {
		if (obj instanceof CyNetwork)
			return CyNetwork.class;
		else if (obj instanceof CyNode)
			return CyNode.class;
		else if (obj instanceof CyEdge)
			return CyEdge.class;
		return CyIdentifiable.class;
	}

	public static String[] getCSV(String str) {
		// Split the string, but allow for protected commas
		String [] s1 = str.split("(?<!\\\\),");
		// Now replace any backslashes with nothing.
		for (int index = 0; index < s1.length; index++) {
			String s = s1[index];
			s1[index] = s.replaceAll("\\\\", "");
		}
		return s1;
	}

}
