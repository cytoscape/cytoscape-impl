package org.cytoscape.command.internal;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringToModelImpl implements StringToModel {
	private final CyApplicationManager appMgr;
	private final CyNetworkManager netMgr;
	private final CyNetworkViewManager netViewMgr;
	private final CyTableManager tableMgr;

	private final static Logger logger = LoggerFactory.getLogger(StringToModelImpl.class);

	private final static String ALL = "all";
	private final static String CURRENT = "current";
	private final static String NAME = "name";
	private final static String SELECTED = "selected";
	private final static String SUID = "suid";
	private final static String UNSELECTED = "unselected";
	
	public StringToModelImpl(CyApplicationManager appMgr, CyNetworkManager netMgr, CyTableManager tableMgr,
	                         CyNetworkViewManager netViewMgr) {
		this.appMgr = appMgr;
		this.netMgr = netMgr;
		this.netViewMgr = netViewMgr;
		this.tableMgr = tableMgr;
	}

	@Override
	public CyNetwork getNetwork(String strNet) {
		if (strNet == null || strNet.length() == 0 || strNet.equalsIgnoreCase(CURRENT))
			return appMgr.getCurrentNetwork();

		// Look for any special prefix
		String column = CyNetwork.NAME;
		String[] splitString = strNet.split(":");
		if (splitString.length > 1) {
			if (SUID.equalsIgnoreCase(splitString[0])) {
				Long suid = getLong(splitString[1]);
				if (suid == null) return null;
				return netMgr.getNetwork(suid);
			}
			if (NAME.equalsIgnoreCase(splitString[0]))
				strNet = splitString[1];
		}

		for (CyNetwork net: netMgr.getNetworkSet()) {
			if (strNet.equalsIgnoreCase(net.getRow(net).get(CyNetwork.NAME, String.class)))
				return net;
		}
		return null;
	}
	
	@Override
	public CyTable getTable(String strTable) {
		if (strTable == null || strTable.length() == 0 || strTable.equalsIgnoreCase(CURRENT))
			return appMgr.getCurrentTable();

		// Look for any special prefix
		CyNetwork network;
		String[] splitString = strTable.split(":");
		if (splitString.length > 1) {
			if (splitString[0].equalsIgnoreCase("node")) {
				network = getNetwork(splitString[1]);
				if(network != null)
					return network.getDefaultNodeTable();
			}
			if (splitString[0].equalsIgnoreCase("edge")) {
				network = getNetwork(splitString[1]);
				if(network != null)
					return network.getDefaultEdgeTable();
			}
			if (splitString[0].equalsIgnoreCase("network")) {
				network = getNetwork(splitString[1]);
				if(network != null)
					return network.getDefaultNetworkTable();
			}
		}
		else
		{
			for (CyTable tab :  tableMgr.getGlobalTables())
			{
				if (tab.getTitle().contains(strTable))
					return tab;
			}
		}

		return null;
	}

	@Override
	public List<CyNode> getNodeList(CyNetwork net, String nodeList) {
		if (net == null)
			net = getNetwork(null);

		if (nodeList.equalsIgnoreCase(ALL)) {
			return net.getNodeList();
		}

		if (nodeList.equalsIgnoreCase(SELECTED)) {
			return CyTableUtil.getNodesInState(net, CyNetwork.SELECTED, true);
		}

		if (nodeList.equalsIgnoreCase(UNSELECTED)) {
			return CyTableUtil.getNodesInState(net, CyNetwork.SELECTED, false);
		}

		Set<CyRow> rows = parseList(nodeList, net.getDefaultNodeTable());
		if (rows == null) return null;

		List<CyNode> nodes = new ArrayList<CyNode>();
		for (CyRow row: rows) {
			Long suid = row.get(CyNetwork.SUID, Long.class);
			nodes.add(net.getNode(suid));
		}
		return nodes;
	}

	@Override
	public List<CyEdge> getEdgeList(CyNetwork net, String edgeList) {
		if (net == null)
			net = getNetwork(null);

		if (edgeList.equalsIgnoreCase(ALL)) {
			return net.getEdgeList();
		}

		if (edgeList.equalsIgnoreCase(SELECTED)) {
			return CyTableUtil.getEdgesInState(net, CyNetwork.SELECTED, true);
		}

		if (edgeList.equalsIgnoreCase(UNSELECTED)) {
			return CyTableUtil.getEdgesInState(net, CyNetwork.SELECTED, false);
		}

		Set<CyRow> rows =  parseList(edgeList, net.getDefaultEdgeTable());
		if (rows == null) return null;

		List<CyEdge> edges = new ArrayList<CyEdge>();
		for (CyRow row: rows) {
			Long suid = row.get(CyNetwork.SUID, Long.class);
			edges.add(net.getEdge(suid));
		}
		return edges;
	}

	@Override
	public List<CyRow> getRowList(CyTable table, String rowList) {
		if (table == null) return null;

		if (rowList.equalsIgnoreCase(ALL)) {
			return table.getAllRows();
		}

		Set<CyRow> rows =  parseList(rowList, table);
		if (rows == null) return null;
		return new ArrayList<CyRow>(rows);
	}

	private Set<CyRow> parseList(String list, CyTable table) {
		// Use a HashSet to we only get one of each CyIdentifiable
		Set<CyRow> rows = new HashSet<CyRow>();

		// Create a map so we only have to traverse the table once!
		Map<String, List<String>> columnMap = new HashMap<String,List<String>>();

		for (String token: list.split(",")) {
			String[] t = token.trim().split(":");
			if (t.length == 2) {
				// Special case SUID: don't add it to the map
				if (SUID.equalsIgnoreCase(t[0])) {
					Long suid = getLong(t[1]);
					if (suid != null && table.rowExists(suid))
						rows.add(table.getRow(suid));
				} else
					updateMap(columnMap, t[0], t[1]);
			} else {
				updateMap(columnMap, CyNetwork.NAME, t[0]);
			}
		}

		// Our map might be empty if we used all SUIDs
		if (columnMap.isEmpty())
			return rows;

		for (CyRow row: table.getAllRows()) {
			for (String key: columnMap.keySet()) {
				if (table.getColumn(key) != null) {
					Object rawValue = row.getRaw(key);
					if (rawValue == null) continue;
					String rowValue = rawValue.toString();
					for (String value: columnMap.get(key)) {
						if (rowValue.equalsIgnoreCase(value))
							rows.add(row);
					}
				}
			}
		}

		return rows;
	}

	private void updateMap(Map<String, List<String>>map, String key, String value) {
		if (!map.containsKey(key))
			map.put(key, new ArrayList<String>());

		map.get(key).add(value);
	}

	private Long getLong(String value) {
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
