package org.cytoscape.model.internal.column;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.equations.Equation;
import org.cytoscape.model.CyRow;

import com.google.common.collect.Sets;

public class EquationSupport implements ColumnData {

	private final ColumnData delegate;
	private final Map<Object,Object> equations; // no need to cast

	public EquationSupport(ColumnData delegate) {
		this.delegate = delegate;
		this.equations = new HashMap<>();
	}

	@Override
	public boolean put(Object key, Object value) {
		if(value instanceof Equation) {
			equations.put(key, value);
			delegate.remove(key);
			return true; // hard to know if value of equation changed, so best to be conservative
		} else {
			equations.remove(key);
			return delegate.put(key, value);
		}
	}

	@Override
	public Object get(Object key) {
		Object equation = equations.get(key);
		if(equation == null) {
			return delegate.get(key);
		}
		return equation;
	}

	@Override
	public boolean remove(Object key) {
		if(equations.remove(key) != null)
			return true;
		return delegate.remove(key);
	}

	@Override
	public Set<Object> keySet() {
		return Sets.union(delegate.keySet(), equations.keySet());
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
}
