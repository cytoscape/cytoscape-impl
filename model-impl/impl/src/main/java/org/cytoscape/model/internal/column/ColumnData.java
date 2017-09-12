package org.cytoscape.model.internal.column;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyRow;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public interface ColumnData {
	
	int countMatchingRows(Object value);
	Collection<CyRow> getMatchingRows(Map<Object, CyRow> rows, Object value);
	<T> Collection<T> getMatchingKeys(Object value, Class<T> type);
	
	void put(Object key, Object value);
	Object get(Object key);
	void remove(Object key);
	Set<Object> keySet();
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ColumnData create(Class<?> primaryKeyType, Class<?> type, Class<?> listElementType, int defaultInitSize) {
		if(Long.class.equals(primaryKeyType)) {
			if(Integer.class.equals(type)) {
				return new MapColumnWithEquationSupport((Map)new Long2IntOpenHashMap(defaultInitSize));
			}
		}
		
		return new MapColumn(new HashMap<>(defaultInitSize));
	}
	

}
