package org.cytoscape.model.internal.column;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyRow;

public interface ColumnData {
	
	Object get(Object key);
	Set<Object> keySet();
	
	/**
	 * Returns true if the value changed
	 */
	boolean put(Object key, Object value); 
	boolean remove(Object key);
	
	int countMatchingRows(Object value);
	Collection<CyRow> getMatchingRows(Map<Object, CyRow> rows, Object value);
	<T> Collection<T> getMatchingKeys(Object value, Class<T> type);

}
