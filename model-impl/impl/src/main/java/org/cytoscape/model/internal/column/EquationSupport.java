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
	private final Map<Object,Equation> equations;

	public EquationSupport(ColumnData delegate) {
		this.delegate = delegate;
		this.equations = new HashMap<>();
	}

	@Override
	public void put(Object key, Object value) {
		if(value instanceof Equation) {
			equations.put(key, (Equation)value);
		} else {
			delegate.put(key, value);
		}
	}

	@Override
	public Object get(Object key) {
		Equation equation = equations.get(key);
		if(equation == null) {
			return delegate.get(key);
		}
		return equation;
	}

	@Override
	public void remove(Object key) {
		equations.remove(key);
		delegate.remove(key);
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
