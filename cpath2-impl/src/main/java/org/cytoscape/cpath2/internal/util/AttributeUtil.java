package org.cytoscape.cpath2.internal.util;

import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyNetwork;

public class AttributeUtil {
	public static void set(CyNetwork network, CyTableEntry entry, String name, Object value, Class<?> type) {
		CyRow row = network.getCyRow(entry);
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

	public static void copyAttributes(CyTableEntry source, CyTableEntry target) {
		// TODO Auto-generated method stub
		
	}
}
