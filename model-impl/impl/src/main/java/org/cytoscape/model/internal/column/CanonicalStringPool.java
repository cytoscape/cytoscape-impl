package org.cytoscape.model.internal.column;

import java.util.HashMap;
import java.util.Map;

public class CanonicalStringPool {

	private final Map<String,String> pool = new HashMap<>();
	
	public String canonicalize(String s) {
		if(pool.size() > 1000) {
			pool.clear();
		}
		
		String canon = pool.putIfAbsent(s, s);
		return canon == null ? s : canon;
	}

}
