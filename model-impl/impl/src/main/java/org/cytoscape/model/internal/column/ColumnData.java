package org.cytoscape.model.internal.column;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyRow;

public interface ColumnData {
	
	int countMatchingRows(Object value);
	Collection<CyRow> getMatchingRows(Map<Object, CyRow> rows, Object value);
	<T> Collection<T> getMatchingKeys(Object value, Class<T> type);
	
	void put(Object key, Object value);
	Object get(Object key);
	void remove(Object key);
	Set<Object> keySet();

}
