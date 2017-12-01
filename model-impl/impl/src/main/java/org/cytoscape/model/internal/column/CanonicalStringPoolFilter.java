package org.cytoscape.model.internal.column;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyRow;

public class CanonicalStringPoolFilter implements ColumnData {

	private final CanonicalStringPool stringPool;
	private final ColumnData delegate;
	
	public CanonicalStringPoolFilter(CanonicalStringPool stringPool, ColumnData delegate) {
		this.stringPool = stringPool;
		this.delegate = delegate;
	}

	@Override
	public int countMatchingRows(Object value) {
		return delegate.countMatchingRows(value);
	}

	@Override
	public Collection<CyRow> getMatchingRows(Map<Object, CyRow> rows, Object value) {
		return delegate.getMatchingRows(rows, value);
	}

	@Override
	public <T> Collection<T> getMatchingKeys(Object value, Class<T> type) {
		return delegate.getMatchingKeys(value, type);
	}

	@Override
	public void put(Object key, Object value) {
		if(value instanceof String) {
			value = stringPool.canonicalize((String)value);
		}
		delegate.put(key, value);
	}

	@Override
	public Object get(Object key) {
		return delegate.get(key);
	}

	@Override
	public void remove(Object key) {
		delegate.remove(key);
	}

	@Override
	public Set<Object> keySet() {
		return delegate.keySet();
	}

}
