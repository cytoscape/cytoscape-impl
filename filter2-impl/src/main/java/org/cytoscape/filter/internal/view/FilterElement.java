package org.cytoscape.filter.internal.view;

import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

class FilterElement extends NamedElement {
	public final CompositeFilter<CyNetwork, CyIdentifiable> filter;
	
	public FilterElement(String name, CompositeFilter<CyNetwork, CyIdentifiable> filter) {
		super(name);
		this.filter = filter;
	}
	
	@Override
	public boolean isPlaceholder() {
		return filter == null;
	}
}