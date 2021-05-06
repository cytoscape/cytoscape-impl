package org.cytoscape.filter.internal.filters.composite;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.filter.internal.ApplyCheck;
import org.cytoscape.filter.internal.MemoizableTransformer;
import org.cytoscape.filter.model.AbstractTransformer;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.work.Tunable;

public class CompositeFilterImpl<C, E> extends AbstractTransformer<C, E> implements CompositeFilter<C, E>, MemoizableTransformer, ApplyCheck<C,E> {
	static final String ID = "org.cytoscape.CompositeFilter";
	public static final Type DEFAULT_TYPE = Type.ALL;
	
	private Type type;
	
	final List<Filter<C, E>> filters;

	private Class<C> contextType;

	private Class<E> elementType;
	
	public CompositeFilterImpl(Class<C> contextType, Class<E> elementType) {
		type = DEFAULT_TYPE;
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
		return ID;
	}

	
	@Override
	public void append(Filter<C, E> filter) {
		checkTypes(filter);
		filters.add(filter);
		notifyListeners();
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
		notifyListeners();
	}

	@Override
	public Filter<C, E> get(int index) {
		return filters.get(index);
	}

	@Override
	public Filter<C, E> remove(int index) {
		try {
			return filters.remove(index);
		} finally {
			notifyListeners();
		}
	}
	
	@Override
	public int getLength() {
		return filters.size();
	}

	@Override
	@Tunable
	public Type getType() {
		return type;
	}

	@Override
	public void setType(Type type) {
		this.type = type;
		notifyListeners();
	}

	@Override
	public int indexOf(Filter<C, E> filter) {
		return filters.indexOf(filter);
	}
	
	@Override
	public void startCaching() {
		for(Filter<C,E> filter : filters) {
			if(filter instanceof MemoizableTransformer) {
				((MemoizableTransformer)filter).startCaching();
			}
		}
	}
	
	@Override
	public void clearCache() {
		for(Filter<C,E> filter : filters) {
			if(filter instanceof MemoizableTransformer) {
				((MemoizableTransformer)filter).clearCache();
			}
		}
	}
	
	@Override
	public boolean accepts(C context, E element) {
		if(filters.isEmpty()) 
			return true;
		
		for (Filter<C,E> filter : filters) {
			boolean result = filter.accepts(context, element);
			if (result != (type == CompositeFilter.Type.ALL)) {
				return result; // Short-circuit
			}
		}
		return type == CompositeFilter.Type.ALL;
	}
	
	@Override
	public boolean isAlwaysFalse() {
		if(filters.isEmpty()) 
			return false;
		
		for(Filter<?,?> filter : filters) {
			boolean result = filter.isAlwaysFalse();
			if (result == (type == CompositeFilter.Type.ALL)) {
				return result; // Short-circuit
			}
		}
		return type != CompositeFilter.Type.ALL;
	}
	
	@Override
	public boolean appliesTo(C context, E element) {
		if(filters.isEmpty()) 
			return false;
		
		for (Filter<C,E> filter : filters) {
			boolean applies;
			if(filter instanceof ApplyCheck)
				applies = ((ApplyCheck<C,E>)filter).appliesTo(context, element);
			else
				applies = true;
			
			if (applies) {
				return true;
			}
		}
		return false;
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
