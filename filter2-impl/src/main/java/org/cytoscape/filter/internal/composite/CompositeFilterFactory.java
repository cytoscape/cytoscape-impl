package org.cytoscape.filter.internal.composite;

import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.FilterFactory;

public class CompositeFilterFactory<C, E> implements FilterFactory<C, E> {

	private Class<C> contextType;
	private Class<E> elementType;

	public CompositeFilterFactory(Class<C> contextType, Class<E> elementType) {
		this.contextType = contextType;
		this.elementType = elementType;
	}
	
	@Override
	public String getId() {
		return CompositeFilterImpl.ID;
	}

	@Override
	public Filter<C, E> createFilter() {
		return new CompositeFilterImpl<C, E>(contextType, elementType);
	}

}
