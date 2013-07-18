package org.cytoscape.filter.internal.transformers;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.work.Tunable;

public class CompositeFilterImpl<C, E> implements CompositeFilter<C, E> {
	@Tunable()
	public Type type;
	
	List<Filter<C, E>> filters;

	private Class<C> contextType;

	private Class<E> elementType;
	
	public CompositeFilterImpl(Class<C> contextType, Class<E> elementType) {
		filters = new ArrayList<Filter<C,E>>();
		this.contextType = contextType;
		this.elementType = elementType;
	}
	
	@Override
	public String getName() {
		return "Group";
	}

	@Override
	public String getId() {
		return "org.cytoscape.FilterChain";
	}

	
	@Override
	public void append(Filter<C, E> filter) {
		checkTypes(filter);
		filters.add(filter);
	}

	private void checkTypes(Filter<C, E> filter) {
		Class<C> otherContextType = filter.getContextType();
		if (!otherContextType.equals(contextType)) {
			throw new IllegalArgumentException("Incompatible context type: " + otherContextType);
		}

		Class<E> otherElementType = filter.getElementType();
		if (!otherElementType.equals(elementType)) {
			throw new IllegalArgumentException("Incompatible element type: " + otherElementType);
		}
	}

	@Override
	public void insert(int index, Filter<C, E> filter) {
		checkTypes(filter);
		filters.add(index, filter);
	}

	@Override
	public Filter<C, E> get(int index) {
		return filters.get(index);
	}

	@Override
	public Filter<C, E> remove(int index) {
		return filters.remove(index);
	}
	
	@Override
	public int getLength() {
		return filters.size();
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public int indexOf(Filter<C, E> filter) {
		return filters.indexOf(filter);
	}
	
	@Override
	public boolean accepts(C context, E element) {
		for (int i = 0; i < filters.size(); i++) {
			Filter<C, E> filter = filters.get(i);
			boolean result = filter.accepts(context, element);
			if (result != (type == CompositeFilter.Type.ALL)) {
				// Short-circuit
				return result;
			}
		}
		
		return type == CompositeFilter.Type.ALL;
	}
	
	@Override
	public Class<C> getContextType() {
		return contextType;
	}
	
	@Override
	public Class<E> getElementType() {
		return elementType;
	}
}
