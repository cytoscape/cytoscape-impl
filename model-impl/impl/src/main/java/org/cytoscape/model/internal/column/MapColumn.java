package org.cytoscape.model.internal.column;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.model.CyRow;

class MapColumn implements ColumnData {

	private final Map<Object,Object> attributes;
	
	public MapColumn(Map<Object,Object> attributes) {
		this.attributes = attributes;
	}
	
	@Override
	public int countMatchingRows(Object value) {
		return Collections.frequency(attributes.values(), value);
	}
	
	@Override
	public Collection<CyRow> getMatchingRows(Map<Object, CyRow> rows, Object value) {
		List<CyRow> matchingRows = new ArrayList<>();
		for(Entry<Object, Object> entry: attributes.entrySet()) {
			if(entry.getValue().equals(value))
				matchingRows.add(rows.get(entry.getKey()));
		}
		return matchingRows;
	}
	
	@Override
	public <T> Collection<T> getMatchingKeys(Object value, Class<T> type) {
		List<T> matchingKeys = new ArrayList<>();
		for(Entry<Object, Object> entry: attributes.entrySet()) {
			if(entry.getValue().equals(value))
				matchingKeys.add((T)entry.getKey());
		}
		return matchingKeys;
	}
	
	@Override
	public boolean put(Object key, Object value) {
		Object prev = attributes.put(key, value);
		if(prev == null && value == null)
			return false;
		if(prev == null)
			return true;
		return !prev.equals(value);
	}
	
	@Override
	public Object get(Object key) {
		return attributes.get(key);
	}
	
	@Override
	public Set<Object> keySet() {
		return attributes.keySet();
	}
	
	@Override
	public boolean remove(Object key) {
		return attributes.remove(key) != null;
	}

}
