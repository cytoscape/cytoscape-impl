package org.cytoscape.view.vizmap.gui.internal;

import java.util.Comparator;
import java.util.Objects;

public record ColumnSpec(GraphObjectType tableType, String columnName) implements Comparable<ColumnSpec> {

	@Override
	public int compareTo(ColumnSpec that) {
		return 
			Objects.compare(this, that, 
				Comparator.comparing(ColumnSpec::tableType)
				.thenComparing(ColumnSpec::columnName)
			);
	}

	

}
