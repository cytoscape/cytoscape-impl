package org.cytoscape.view.vizmap.gui.internal;

import java.util.Comparator;

public record ColumnSpec(GraphObjectType tableType, String columnName) {
	
	public static Comparator<ColumnSpec> comparingName() {
		return 
			Comparator.comparing(ColumnSpec::tableType)
			.thenComparing((a, b) -> a.columnName.compareToIgnoreCase(b.columnName));
	}
	
}
