package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;

public class CyListImpl<T> implements List<T> {
	private Class<T> elementType;
	private List<T> delegate;
	private CyEventHelper eventHelper;
	private CyRow row;
	private CyColumn column;
	
	public CyListImpl(Class<T> elementType, List<T> delegate, CyEventHelper eventHelper, CyRow row, CyColumn column) {
		this.elementType = elementType;
		this.delegate = delegate;
		this.eventHelper = eventHelper;
		this.row = row;
		this.column = column;
	}
	
	public Class<T> getListElementType() {
		return elementType;
	}
	
	@Override
	public boolean add(T item) {
		checkType(item);
		if (delegate.add(item)) {
			fireEvent();
			return true;
		}
		return false;
	}

	private void checkType(T item) {
		if (item == null || !elementType.isAssignableFrom(item.getClass())) {
			throw new IllegalArgumentException("This list only allows objects of type " + elementType.getName());
		}
	}

	private void fireEvent() {
		// TODO: If this is a virtual column, we need to ensure all dependents
		// fire events.
		eventHelper.addEventPayload(row.getTable(), new RowSetRecord(row, column.getName(), this, this), RowsSetEvent.class);
	}

	@Override
	public void add(int index, T item) {
		checkType(item);
		fireEvent();
		delegate.add(index, item);
	}

	@Override
	public boolean addAll(Collection<? extends T> items) {
		for (T item : items) {
			checkType(item);
		}
		if (delegate.addAll(items)) {
			fireEvent();
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> items) {
		for (T item : items) {
			checkType(item);
		}
		if (delegate.addAll(index, items)) {
			fireEvent();
			return true;
		}
		return false;
	}

	@Override
	public void clear() {
		if (delegate.size() == 0) {
			return;
		}
		delegate.clear();
		fireEvent(); 
	}

	@Override
	public boolean contains(Object item) {
		return delegate.contains(item);
	}

	@Override
	public boolean containsAll(Collection<?> items) {
		return delegate.containsAll(items);
	}

	@Override
	public T get(int index) {
		return delegate.get(index);
	}

	@Override
	public int indexOf(Object item) {
		return delegate.indexOf(item);
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return listIterator();
	}

	@Override
	public int lastIndexOf(Object item) {
		return delegate.lastIndexOf(item);
	}

	@Override
	public ListIterator<T> listIterator() {
		final ListIterator<T> iterator = delegate.listIterator();
		return createListIterator(iterator);
	}

	private ListIterator<T> createListIterator(final ListIterator<T> iterator) {
		return new ListIterator<T>() {
			@Override
			public void add(T item) {
				checkType(item);
				iterator.add(item);
				fireEvent();
			}

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public boolean hasPrevious() {
				return iterator.hasPrevious();
			}

			@Override
			public T next() {
				return iterator.next();
			}

			@Override
			public int nextIndex() {
				return iterator.nextIndex();
			}

			@Override
			public T previous() {
				return iterator.previous();
			}

			@Override
			public int previousIndex() {
				return iterator.previousIndex();
			}

			@Override
			public void remove() {
				iterator.remove();
				fireEvent();
			}

			@Override
			public void set(T item) {
				checkType(item);
				iterator.set(item);
				fireEvent();
			}
		};
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return delegate.listIterator(index);
	}

	@Override
	public boolean remove(Object item) {
		if (delegate.remove(item)) {
			fireEvent();
			return true;
		}
		return false;
	}

	@Override
	public T remove(int index) {
		T value = delegate.remove(index);
		if (value == null) {
			return null;
		}
		fireEvent();
		return value;
	}

	@Override
	public boolean removeAll(Collection<?> items) {
		if (delegate.removeAll(items)) {
			fireEvent();
			return true;
		}
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> items) {
		if (delegate.retainAll(items)) {
			fireEvent();
			return true;
		}
		return true;
	}

	@Override
	public T set(int index, T item) {
		T value = delegate.set(index, item);
		fireEvent();
		return value;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return new CyListImpl<T>(elementType, delegate.subList(fromIndex, toIndex), eventHelper, row, column);
	}

	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	@Override
	public <S> S[] toArray(S[] container) {
		return (S[]) delegate.toArray(container);
	}
	
	@Override
	public String toString() {
		return delegate.toString();
	}
}
