package org.cytoscape.model.internal.column;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.equations.Equation;

import com.google.common.collect.Sets;

public class MapColumnWithEquationSupport extends MapColumn {

	private Map<Object, Equation> equations;
	
	public MapColumnWithEquationSupport(Map<Object, Object> attributes) {
		super(attributes);
		equations = new HashMap<>();
	}

	@Override
	public void put(Object key, Object value) {
		if(value instanceof Equation) {
			equations.put(key, (Equation)value);
		} else {
			super.put(key, value);
		}
	}

	@Override
	public Object get(Object key) {
		Equation equation = equations.get(key);
		if(equation == null) {
			return super.get(key);
		}
		return equation;
	}

	@Override
	public void remove(Object key) {
		equations.remove(key);
		super.remove(key);
	}

	@Override
	public Set<Object> keySet() {
		return Sets.union(super.keySet(), equations.keySet());
	}
}
