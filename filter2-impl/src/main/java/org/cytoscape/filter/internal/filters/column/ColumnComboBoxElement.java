package org.cytoscape.filter.internal.filters.column;

import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;

public class ColumnComboBoxElement implements Comparable<ColumnComboBoxElement> {
	private final Class<?> tableType;
	private final SelectedColumnType colType;
	private final String name;
	private final String description;
	
	
	public static enum SelectedColumnType {
		NONE,
		INTEGER,
		DOUBLE,
		STRING,
		BOOLEAN;
		
		private static SelectedColumnType fromColumn(CyColumn col) {
			Class<?> colType = col.getType();
			if(colType.isAssignableFrom(List.class))
				colType = col.getListElementType();
			
			if(colType == Integer.class || colType == Long.class) // treat longs as ints for now, 
				return INTEGER;
			if(colType == Double.class)
				return DOUBLE;
			if(colType == Boolean.class)
				return BOOLEAN;
			if(colType == String.class)
				return STRING;
			return NONE;
		}
	}
	
	
	public ColumnComboBoxElement(Class<?> tableType, CyColumn col) {
		this.tableType = tableType;
		this.colType = SelectedColumnType.fromColumn(col);
		this.name = col.getName();
		
		if (CyNode.class.equals(tableType)) {
			description = "Node: " + name;
		} else if (CyEdge.class.equals(tableType)) {
			description = "Edge: " + name;
		} else {
			description = name;
		}
	}
	
	public ColumnComboBoxElement(String description) {
		this.tableType = null;
		this.colType = SelectedColumnType.NONE;
		this.name = "";
		this.description = description;
	}
	
	public Class<?> getTableType() {
		return tableType;
	}


	public SelectedColumnType getColType() {
		return colType;
	}


	public String getName() {
		return name;
	}


	public String getDescription() {
		return description;
	}


	@Override
	public String toString() {
		return description;
	}
	
	@Override
	public int compareTo(ColumnComboBoxElement other) {
		if (tableType == null && other.tableType == null) {
			return String.CASE_INSENSITIVE_ORDER.compare(name, other.name);
		}
		
		if (tableType == null) {
			return -1;
		}
		
		if (other.tableType == null) {
			return 1;
		}
		
		if (tableType.equals(other.tableType)) {
			return String.CASE_INSENSITIVE_ORDER.compare(name, other.name);
		}
		
		if (tableType.equals(CyNode.class)) {
			return -1;
		}
		
		return 1;
	}
}
