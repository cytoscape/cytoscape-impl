package org.cytoscape.model.internal.column;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koloboke.collect.map.hash.HashLongDoubleMaps;
import com.koloboke.collect.map.hash.HashLongIntMaps;
import com.koloboke.collect.map.hash.HashLongLongMaps;
import com.koloboke.collect.map.hash.HashLongObjMaps;
import com.koloboke.collect.set.hash.HashLongSets;


public class KolobokeColumnDataFactory implements ColumnDataFactory {

	private final CanonicalStringPool stringPool = new CanonicalStringPool();
	
	@Override
	public void clearCache() {
		stringPool.clear();
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ColumnData create(Class<?> primaryKeyType, Class<?> type, Class<?> listElementType, int defaultInitSize) {
		// Maps that store Object references can hold Equation objects directly.
		// Primitive maps require a wrapper to hold the Equations.
		if(Long.class.equals(primaryKeyType)) {
			if(Integer.class.equals(type)) {
				return new EquationSupport(new MapColumn((Map)HashLongIntMaps.newMutableMap()));
			} else if(Long.class.equals(type)) {
				return new EquationSupport(new MapColumn((Map)HashLongLongMaps.newMutableMap()));
			} else if(Double.class.equals(type)) {
				return new EquationSupport(new MapColumn((Map)HashLongDoubleMaps.newMutableMap()));
			} else if(String.class.equals(type)) {
				return new CanonicalStringPoolFilter(stringPool, new MapColumn((Map)HashLongObjMaps.newMutableMap()));
			} else if(Boolean.class.equals(type)) {
				return new EquationSupport(new LongToBooleanColumn(() -> HashLongSets.newMutableSet()));
			}
		}
		
		return new MapColumn(new HashMap<>(defaultInitSize));
	}

	// Koloboke does not provide primitive Lists :(
	@Override
	public List<?> createList(Class<?> elementType, List<?> data) {
		if(String.class.equals(elementType)) {
			List<Object> canonData = new ArrayList<>(data.size());
			for(Object value : data) {
				if(value instanceof String) {
					value = stringPool.canonicalize((String)value);
				}
				canonData.add(value);
			}
			return canonData;
		}
		
		return new ArrayList<>(data);
	}

}
