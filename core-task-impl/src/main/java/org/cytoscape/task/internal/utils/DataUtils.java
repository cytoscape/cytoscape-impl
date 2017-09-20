package org.cytoscape.task.internal.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class DataUtils {

	public static final String PARENT_NETWORK_COLUMN = "__parentNetwork.SUID";
	
	public static String getNetworkName(CyNetwork network) {
		String name = "";
		
		try {
			name = network.getRow(network).get(CyNetwork.NAME, String.class);
		} catch (Exception e) {
		}
		
		if (name == null || name.trim().isEmpty())
			name = "? (SUID: " + network.getSUID() + ")";
		
		return name;
	}
	
	public static String getViewTitle(CyNetworkView view) {
		String title = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		
		if (title == null || title.trim().isEmpty())
			title = getNetworkName(view.getModel());
		
		return title;
	}
	
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

	public static <T> T convertString(String value, Class<T> type) {
		if (type.equals(Long.class))
			return (T)Long.valueOf(value);
		if (type.equals(Double.class))
			return (T)Double.valueOf(value);
		if (type.equals(Integer.class))
			return (T)Integer.valueOf(value);
		if (type.equals(Boolean.class))
			return (T)Boolean.valueOf(value);
		if (type.equals(String.class))
			return (T)value;
		return null;
	}

	public static <T> List<T> convertStringList(String value, Class<T> listElementType) {
		String[] splitString = getCSV(value);
		List<T> list = new ArrayList<T>();
		for (String s: splitString) {
			T val = convertString(s, listElementType);
			if (val != null)
				list.add(val);
		}
		if (list.size() > 0)
			return list;

		return null;
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
		if (data.size() == 0)
			return "{}";

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
		if (data.size() == 0)
			return "[]";

		String result = "[";
		for (Object v: data)
			result += v.toString() + ",";
		return result.substring(0, result.length()-1)+"]";
	}

	public static Class getType(String type) {
		if ("double".equalsIgnoreCase(type))
			return Double.class;
		if ("integer".equalsIgnoreCase(type))
			return Integer.class;
		if ("long".equalsIgnoreCase(type))
			return Long.class;
		if ("boolean".equalsIgnoreCase(type))
			return Boolean.class;
		if ("string".equalsIgnoreCase(type))
			return String.class;
		if ("list".equalsIgnoreCase(type))
			return List.class;

		return String.class;
	}

	public static String getType(Class type) {
		if (type.equals(Double.class))
			return "double";
		if (type.equals(Integer.class))
			return "integer";
		if (type.equals(Long.class))
			return "long";
		if (type.equals(Boolean.class))
			return "boolean";
		if (type.equals(String.class))
			return "string";
		if (type.equals(List.class))
			return "list";
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
		if (obj instanceof CyNode)
			return CyNode.class;
		if (obj instanceof CyEdge)
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

	/**
	 * Save provenance info (parent subnetwork), which is used by the GUI.
	 * @param net
	 * @param parentSUID SUID of the parent network
	 */
	public static void saveParentNetworkSUID(final CySubNetwork net, final Long parentSUID) {
		final CyTable hiddenTable = net.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		
		if (hiddenTable.getColumn(PARENT_NETWORK_COLUMN) == null)
			hiddenTable.createColumn(PARENT_NETWORK_COLUMN, Long.class, true);
		
		hiddenTable.getRow(net.getSUID()).set(PARENT_NETWORK_COLUMN, parentSUID);
	}

	public static Long getParentNetworkSUID(final CySubNetwork net) {
		final CyTable table = net.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		
		return table != null ? table.getRow(net.getSUID()).get(PARENT_NETWORK_COLUMN, Long.class) : null;
	}
}
