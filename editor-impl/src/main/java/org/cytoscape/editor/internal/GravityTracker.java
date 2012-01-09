package org.cytoscape.editor.internal;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class GravityTracker<S> {

	private final Map<S,Double> gravityMap = new HashMap<S,Double>();
	private final List<S> sortedKeys = new ArrayList<S>(); 

	public int add(S key, double value) {
		if ( key == null )
			throw new NullPointerException("key cannot be null");

		if ( gravityMap.containsKey(key) )
			throw new IllegalArgumentException("key: '" + key.toString() + "' already exists - duplicate keys not allowed");
		gravityMap.put(key,value);
		int index = getIndex(key); 
		sortedKeys.add(index,key);
		return index; 
	}

	public int getIndex(S key) {
		if ( key == null )
			throw new NullPointerException("key cannot be null");

	 	Double gravity = gravityMap.get(key);
		if ( gravity == null )
			return sortedKeys.size();

		double newGravity = gravity.doubleValue();

		int i = 0;

		for (S k : sortedKeys ) {
			if ( k.equals(key) )
				break;
			if ( newGravity < gravityMap.get(k) )
				break;
			i++;
		}

		return i;
	}

	public void remove(S key) {
		Double d = gravityMap.remove(key);

		if ( d != null )
			sortedKeys.remove(key);
	}
}

