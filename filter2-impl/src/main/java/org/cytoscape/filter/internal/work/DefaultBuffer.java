package org.cytoscape.filter.internal.work;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultBuffer<C, E> implements TransformerBuffer<C, E> {
	
	private Set<E> elements;
	private Class<C> contextType;
	private Class<E> elementType;

	public DefaultBuffer(Class<C> contextType, Class<E> elementType, int initialCapacity, int concurrencyLevel) {
		this.contextType = contextType;
		this.elementType = elementType;
		if (concurrencyLevel > 1) {
			elements = Collections.newSetFromMap(new ConcurrentHashMap<>(initialCapacity, 0.75f, concurrencyLevel));
		} else {
			elements = Collections.newSetFromMap(new IdentityHashMap<>(initialCapacity));
		}
	}

	@Override
	public List<E> getElementList(C context) {
		return new ArrayList<>(elements);
	}
	
	@Override
	public Class<C> getContextType() {
		return contextType;
	}

	@Override
	public Class<E> getElementType() {
		return elementType;
	}

	@Override
	public int getElementCount(C context) {
		return elements.size();
	}

	@Override
	public void collect(E element) {
		elements.add(element);
	}

	@Override
	public void clear() {
		elements.clear();
	}
}
