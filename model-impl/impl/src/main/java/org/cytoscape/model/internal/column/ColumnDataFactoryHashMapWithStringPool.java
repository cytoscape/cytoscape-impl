package org.cytoscape.model.internal.column;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ColumnDataFactoryHashMapWithStringPool implements ColumnDataFactory {

	private final CanonicalStringPool stringPool = new CanonicalStringPool();

	@Override
	public void clearCache() {
		stringPool.clear();
	}
	

	@Override
	public ColumnData create(Class<?> primaryKeyType, Class<?> type, Class<?> listElementType, int defaultInitSize) {
		if(String.class.equals(type)) {
			return new CanonicalStringPoolFilter(stringPool, new MapColumn(new HashMap<>(defaultInitSize)));
		}
		return new MapColumn(new HashMap<>(defaultInitSize));
	}

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
