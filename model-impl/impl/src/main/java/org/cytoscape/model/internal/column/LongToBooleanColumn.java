package org.cytoscape.model.internal.column;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyRow;

import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class LongToBooleanColumn implements ColumnData {

	// need to track both true and false because cell values can also be unset
	private final Set<Long> trueKeys  = new LongOpenHashSet();
	private final Set<Long> falseKeys = new LongOpenHashSet();
	
	
	@Override
	public int countMatchingRows(Object value) {
		if(Boolean.TRUE.equals(value))
			return trueKeys.size();
		if(Boolean.FALSE.equals(value))
			return falseKeys.size();
		return 0;
	}

	@Override
	public Collection<CyRow> getMatchingRows(Map<Object, CyRow> rows, Object value) {
		Set<Long> keys;
		if(Boolean.TRUE.equals(value))
			keys = trueKeys;
		else if(Boolean.FALSE.equals(value))
			keys = falseKeys;
		else
			keys = Collections.emptySet();
		
		List<CyRow> matchingRows = new ArrayList<>();
		for(Object key : keys) {
			matchingRows.add(rows.get(key));
		}
		return matchingRows;
	}

	@Override
	public <T> Collection<T> getMatchingKeys(Object value, Class<T> type) {
		Set<Long> keys;
		if(Boolean.TRUE.equals(value))
			keys = trueKeys;
		else if(Boolean.FALSE.equals(value))
			keys = falseKeys;
		else
			keys = Collections.emptySet();
		
		return new ArrayList<>((Set<T>)keys);
	}
	
	@Override
	public void put(Object key, Object value) {
		if(Boolean.TRUE.equals(value)) {
			trueKeys.add((Long)key);
			falseKeys.remove(key);
		}
		else if(Boolean.FALSE.equals(value)) {
			falseKeys.add((Long)key);
			trueKeys.remove(key);
		}
	}

	@Override
	public Object get(Object key) {
		if(trueKeys.contains(key))
			return Boolean.TRUE;
		if(falseKeys.contains(key))
			return Boolean.FALSE;
		return null;
	}

	@Override
	public void remove(Object key) {
		trueKeys.remove(key);
		falseKeys.remove(key);
	}

	@Override
	public Set<Object> keySet() {
		return Sets.union(trueKeys, falseKeys);
	}

}

