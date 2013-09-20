package org.cytoscape.filter.internal.degree;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;

public interface DegreeFilterView {
	static class EdgeTypeElement {
		public final CyEdge.Type type;
		public final String description;
		
		public EdgeTypeElement(Type type, String description) {
			this.type = type;
			this.description = description;
		}
		
		@Override
		public String toString() {
			return description;
		}
	}
}
