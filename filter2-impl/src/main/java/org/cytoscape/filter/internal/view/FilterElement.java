package org.cytoscape.filter.internal.view;

import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class FilterElement extends NamedElement {
	
	private final CompositeFilter<CyNetwork, CyIdentifiable> filter;
	
	public FilterElement(String name, CompositeFilter<CyNetwork, CyIdentifiable> filter) {
		super(name);
		this.filter = filter;
	}
	
	public CompositeFilter<CyNetwork, CyIdentifiable> getFilter() {
		return filter;
	}
}