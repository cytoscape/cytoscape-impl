package org.cytoscape.filter.internal.filters.event;

import java.util.Vector;

import org.cytoscape.filter.internal.filters.model.CompositeFilter;

public class FiltersChangedEvent {
	
	private final Vector<CompositeFilter> filters;
	private final CompositeFilter currentFilter;

	public FiltersChangedEvent(final Vector<CompositeFilter> filters) {
		this(filters, null);
	}
	
	public FiltersChangedEvent(final Vector<CompositeFilter> filters, final CompositeFilter currentFilter) {
		this.filters = filters != null ? filters : new Vector<CompositeFilter>();
		
		if (currentFilter != null)
			this.currentFilter = currentFilter;
		else
			this.currentFilter = filters.isEmpty() ? null : filters.get(0);
	}

	public Vector<CompositeFilter> getFilters() {
		return filters;
	}

	public CompositeFilter getCurrentFilter() {
		return currentFilter;
	}
}
