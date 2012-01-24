package org.cytoscape.filter.internal.filters.event;

import java.util.Vector;

import org.cytoscape.filter.internal.filters.model.CompositeFilter;

public class FiltersChangedEvent {
	
	private Vector<CompositeFilter> filters;

	public FiltersChangedEvent(Vector<CompositeFilter> filters) {
		this.filters = filters != null ? filters : new Vector<CompositeFilter>();
	}

	public Vector<CompositeFilter> getFilters() {
		return filters;
	}
}
