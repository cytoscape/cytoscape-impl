package org.cytoscape.io.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

/**
 * This class stores an old-to-new SUID map and is responsible for updating the values in SUID-typed CyColumns.
 */
public class SUIDUpdater {

	public static final String SUID_COLUMN_SUFFIX = ".SUID";
	
	private Map<Long/*old SUID*/, Long/*new SUID*/> suidMap;
	private Set<CyTable> tables;
	private Map<CyTable, Set<String>/*column names*/> ignoredColumns;
	
	public SUIDUpdater() {
		init();
	}
	
	public void init() {
		suidMap = new HashMap<>();
		tables = new HashSet<>();
		ignoredColumns = new HashMap<>();
	}
	
	public void addSUIDMapping(final Long oldSUID, final Long newSUID) {
		if (oldSUID != null && newSUID != null)
			suidMap.put(oldSUID, newSUID);
	}
	
	public Long getNewSUID(final Long oldSUID) {
		return suidMap.get(oldSUID);
	}
	
	public void addTable(final CyTable table) {
		tables.add(table);
	}
	
	public void addTables(final Set<CyTable> networkTables) {
		if (networkTables != null)
			tables.addAll(networkTables);
	}
	
	public void ignoreColumn(final CyTable table, final String columnName) {
		if (table != null && columnName != null) {
			Set<String> names = ignoredColumns.get(table);
		
			if (names == null) {
				names = new HashSet<>();
				ignoredColumns.put(table, names);
			}
			
			names.add(columnName);
		}
	}
	
	public void updateSUIDColumns() {
		for (final CyTable tbl : tables) {
			final Collection<CyColumn> columns = tbl.getColumns();
			
			for (final CyColumn c : columns) {
				if (isUpdatable(c)) {
					Set<String> ignoredNames = ignoredColumns.get(tbl);
					
					if (ignoredNames == null || !ignoredNames.contains(c.getName()))
						updateRows(tbl, c);
				}
			}
		}
	}
	
	public static boolean isUpdatable(final CyColumn column) {
		if (column != null
				&& !column.isPrimaryKey()
				&& !column.getVirtualColumnInfo().isVirtual()
				&& (column.getType() == Long.class ||
						(Collection.class.isAssignableFrom(column.getType()) &&
								column.getListElementType() == Long.class)))
			return isUpdatable(column.getName());
		
		return false;
    }
	
	public static boolean isUpdatable(final String columnName) {
		return columnName != null && columnName.endsWith(SUID_COLUMN_SUFFIX);
	}
	
	public void dispose() {
		init();
	}
	
	@Override
	public String toString() {
		return "SUIDUpdater{ suidMap=" + suidMap + ", tables=" + tables + " }";
	}
	
	private void updateRows(final CyTable tbl, final CyColumn column) {
		final String columnName = column.getName();
		final Class<?> type = column.getType();
		final Class<?> listType = column.getListElementType();
		
		if (type != Long.class && listType != Long.class)
			return;
		
		for (final CyRow row : tbl.getAllRows()) {
			if (type == Long.class) {
				final Long oldSUID = row.get(columnName, Long.class);
				final Long newSUID = getNewSUID(oldSUID);
				row.set(columnName, newSUID);
			} else if (type == List.class) {
				final List<Long> oldList = row.getList(columnName, Long.class);
				
				if (oldList != null && !oldList.isEmpty()) {
					final List<Long> newList = new ArrayList<>(oldList.size());
					
					for (final Long oldSUID : oldList) {
						final Long newSUID = getNewSUID(oldSUID);
						
						if (newSUID != null)
							newList.add(newSUID);
					}
					
					row.set(columnName, newList);
				}
			}
		}
	}
}
