package org.cytoscape.task.internal.table;

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
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.work.AbstractTask;

import org.cytoscape.task.internal.utils.DataUtils;

public abstract class AbstractTableDataTask extends AbstractTask {
	CyTableManager cyTableManager;

	AbstractTableDataTask(CyTableManager cyTableManager) {
		this.cyTableManager = cyTableManager;
	}

	public Map<String, Object> getCyIdentifierData(CyTable table,
	                                               CyIdentifiable id, 
	                                               List<String> columnList) {
		if (id == null) return null;
	
		if (columnList == null)
			columnList = new ArrayList<String>(CyTableUtil.getColumnNames(table));
		
		return getDataFromTable(table, id.getSUID(), columnList);
	}

	public CyTable getNetworkTable(CyNetwork network, Class<? extends CyIdentifiable> type, 
	                               String namespace) {
		return network.getTable(type, getNamespace(namespace));
	}


	public int setCyIdentifierData(CyTable table, CyIdentifiable id, Map<CyColumn, Object> valueMap) {
		if (id == null || valueMap.size() == 0) return 0;

		int count = 0;
		CyRow row = table.getRow(id.getSUID());
		for (CyColumn column: valueMap.keySet()) {
			Class type = column.getType();
				String name = column.getName();
			Object value = valueMap.get(column);
			if (value != null) {
				row.set(name, type.cast(value));
				count++;
			}
		}
		return count;
	}

	public void createColumn(CyTable table, String name, String typeName, String elementTypeName) {
		Class type = DataUtils.getType(typeName);
		Class elementType = DataUtils.getType(elementTypeName);

		if (table.getColumn(name) != null)
			throw new IllegalArgumentException("A column named "+name+" already exists");
		if (type.equals(List.class))
			table.createListColumn(name, elementType, false);
		else
			table.createColumn(name, type, false);
		return;
	}

	public CyTable getUnattachedTable(String tableName) {
		Set<CyTable> tables = cyTableManager.getAllTables(false);
		for (CyTable table: tables) {
			if (tableName.equalsIgnoreCase(table.getTitle()))
				return table;
		}
		return null;
	}

	public Map<String, Object> getDataFromTable(CyTable table, Object key, List<String> columnList) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (table.rowExists(key)) {
			CyRow row = table.getRow(key);
			for (String column: columnList) {
				result.put(column, row.getRaw(column));
			}
		}
		return result;
	}

	public String getNamespace(String namespace) {
		if (namespace == null || 
		    namespace.equalsIgnoreCase(CyNetwork.DEFAULT_ATTRS) || 
		    namespace.equalsIgnoreCase("default"))
			return CyNetwork.DEFAULT_ATTRS;
		else if (namespace.equalsIgnoreCase("hidden") ||
		         namespace.equalsIgnoreCase(CyNetwork.HIDDEN_ATTRS))
			return CyNetwork.HIDDEN_ATTRS;
		else if (namespace.equalsIgnoreCase("local") ||
		         namespace.equalsIgnoreCase(CyNetwork.LOCAL_ATTRS))
			return CyNetwork.LOCAL_ATTRS;
		else if (namespace.equalsIgnoreCase("shared") ||
		         namespace.equalsIgnoreCase(CyRootNetwork.SHARED_ATTRS))
			return CyRootNetwork.SHARED_ATTRS;
		else
			return namespace;
	}

}
