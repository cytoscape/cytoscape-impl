package org.cytoscape.filter.internal.filters.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.cytoscape.filter.internal.filters.event.FiltersChangedEvent;
import org.cytoscape.filter.internal.filters.event.FiltersChangedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterModelLocator {

	private Vector<CompositeFilter> filters = new Vector<CompositeFilter>();
	private List<FiltersChangedListener> filtersChangedListeners = new ArrayList<FiltersChangedListener>();
	
	private static final Logger logger = LoggerFactory.getLogger(FilterModelLocator.class);
	
	public void addFilters(Collection<CompositeFilter> filters) {
		if (filters != null) {
			boolean changed = this.filters.addAll(filters);
			
			if (changed)
				notifyListeners(new FiltersChangedEvent(this.filters));
		}
	}
	
	public void addFilter(CompositeFilter filter) {
		if (filter != null) {
			boolean changed = this.filters.add(filter);
			
			if (changed)
				notifyListeners(new FiltersChangedEvent(this.filters));
		}
	}
	
	public void removeFilters(Collection<CompositeFilter> filters) {
		if (filters != null) {
			boolean changed = this.filters.removeAll(filters);
			
			if (changed)
				notifyListeners(new FiltersChangedEvent(this.filters));
		}
	}
	
	public void removeFilter(CompositeFilter filter) {
		if (filters != null) {
			boolean changed = this.filters.remove(filter);
			
			if (changed)
				notifyListeners(new FiltersChangedEvent(this.filters));
		}
	}
	
	public Vector<CompositeFilter> getFilters() {
		return new Vector<CompositeFilter>(filters);
	}
	
	public void addListener(FiltersChangedListener listener) {
		if (listener != null)
			filtersChangedListeners.add(listener);
	}
	
	public boolean removeListener(FiltersChangedListener listener) {
		if (listener != null)
			return filtersChangedListeners.remove(listener);
		
		return false;
	}
	
	public void notifyListeners(FiltersChangedEvent event) {
		for (FiltersChangedListener listener : filtersChangedListeners) {
			try {
				listener.handleEvent(event);
			} catch (Exception ex) {
				logger.error("Error notifying listener of FiltersChangedEvent", ex);
			}
		}
	}
}
