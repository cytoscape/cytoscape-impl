package org.cytoscape.cpath2.internal.util;

import java.util.List;
import java.util.Map.Entry;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class AttributeUtil {
	
	public static void set(CyNetwork network, CyIdentifiable entry, String name, Object value, Class<?> type) {
		set(network, entry, CyNetwork.DEFAULT_ATTRS, name, value, type);
	}

	public static void copyAttributes(CyNetwork srcNetwork, CyIdentifiable source, CyNetwork tgtNetwork, CyIdentifiable target) {
		CyRow sourceRow = srcNetwork.getRow(source);
		for (Entry<String, Object> entry : sourceRow.getAllValues().entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			CyColumn column = sourceRow.getTable().getColumn(key);
			if (!column.getVirtualColumnInfo().isVirtual()) { //skip virtual cols! (bug fixed)
				Class<?> type;
				if (value instanceof List) {
					type = column.getListElementType();
				} else {
					type = column.getType();
				}

				set(tgtNetwork, target, key, value, type);
			}
		}
	}
	
	
	public static void set(CyNetwork network, CyIdentifiable entry, String tableName, String name, Object value, Class<?> type) {
		CyRow row = network.getRow(entry, tableName);
		CyTable table = row.getTable();
		CyColumn column = table.getColumn(name);
		
		if (value != null) {
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
}
