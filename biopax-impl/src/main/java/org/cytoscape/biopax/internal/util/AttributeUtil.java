package org.cytoscape.biopax.internal.util;

import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class AttributeUtil {
	
	public static void set(CyNetwork network, CyIdentifiable entry, String name, Object value, Class<?> type) {
		set(network, entry, CyNetwork.DEFAULT_ATTRS, name, value, type);
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
