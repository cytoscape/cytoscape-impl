package org.cytoscape.io.internal.read;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;


/**
 * This class stores an old-to-new SUID map and is responsible for updating the values in SUID-typed CyColumns.
 */
public class SUIDUpdater {

	private static final String SUID_COLUMN_SUFFIX = ".SUID";
	
	private Map<Long/*old SUID*/, Long/*new SUID*/> suidMap;
	private Map<CyTable, Set<String>/*column names*/> suidColumnMap;
	
	public SUIDUpdater() {
		init();
	}
	
	public void init() {
		suidMap = new HashMap<Long, Long>();
		suidColumnMap = new WeakHashMap<CyTable, Set<String>>();
	}
	
	public void addSUIDMapping(final Long oldSUID, final Long newSUID) {
		if (oldSUID != null && newSUID != null)
			suidMap.put(oldSUID, newSUID);
	}
	
	public Long getNewSUID(final Long oldSUID) {
		return suidMap.get(oldSUID);
	}
	
	public void addSUIDColumn(final CyTable table, final String name) {
		Set<String> columnNames = suidColumnMap.get(table);
		
		if (columnNames == null) {
			columnNames = new HashSet<String>();
			suidColumnMap.put(table, columnNames);
		}
		
		columnNames.add(name);
	}
	
	public void updateSUIDColumns() {
		for (final Map.Entry<CyTable, Set<String>> entry : suidColumnMap.entrySet()) {
			final CyTable tbl = entry.getKey();
			final Set<String> columnNames = entry.getValue();
			
			for (final String name : columnNames)
				updateRows(tbl, name);
		}
	}
	
	public void dispose() {
		init();
	}
	
	public static boolean isUpdatableSUIDColumn(final String columnName) {
    	return columnName != null && columnName.endsWith(SUID_COLUMN_SUFFIX);
    }
	
	@Override
	public String toString() {
		return "SUIDUpdater{ suidMap=" + suidMap + ", suidColumnMap=" + suidColumnMap + " }";
	}

	private void updateRows(final CyTable tbl, final String columnName) {
		final CyColumn column = tbl.getColumn(columnName);
		final Class<?> type = column.getType();
		final Class<?> listType = column.getListElementType();
		
		if (type != Long.class && listType != Long.class)
			return;
		
		for (final CyRow row : tbl.getAllRows()) {
			if (type == Long.class) {
				final Long oldSUID = row.get(columnName, Long.class);
				final Long newSUID = getNewSUID(oldSUID);
				
				if (newSUID != null)
					row.set(columnName, newSUID);
			} else if (type == List.class) {
				final List<Long> oldList = row.getList(columnName, Long.class);
				
				if (oldList != null && !oldList.isEmpty()) {
					final List<Long> newList = new ArrayList<Long>(oldList.size());
					
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
