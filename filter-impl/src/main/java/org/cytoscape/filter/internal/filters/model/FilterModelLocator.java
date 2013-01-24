package org.cytoscape.filter.internal.filters.model;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
				notifyListeners(new FiltersChangedEvent(this.filters, filter));
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
