package org.cytoscape.biopax.internal.util;

import java.util.List;
import java.util.Map.Entry;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;

public class AttributeUtil {
	public static void set(CyTableEntry entry, String name, Object value, Class<?> type) {
		set(entry, null, name, value, type);
	}

	public static void copyAttributes(CyTableEntry source, CyTableEntry target) {
		CyRow sourceRow = source.getCyRow();
		for (Entry<String, Object> entry : sourceRow.getAllValues().entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			CyColumn column = sourceRow.getTable().getColumn(key);
			Class<?> type;
			if (value instanceof List) {
				type = column.getListElementType();
			} else {
				type = column.getType();
			}
			set(target, key, value, type);
		}
	}
	
	
	public static void set(CyTableEntry entry, String tableName, String name, Object value, Class<?> type) {
		CyRow row = (tableName==null) ? entry.getCyRow() : entry.getCyRow(tableName);
		CyTable table = row.getTable();
		CyColumn column = table.getColumn(name);
		if (column == null) {
			if (value instanceof List) {
				table.createListColumn(name, type, false);
			} else { 
				table.createColumn(name, type, false);
			}
		}
		row.set(name, value);
	}
}
