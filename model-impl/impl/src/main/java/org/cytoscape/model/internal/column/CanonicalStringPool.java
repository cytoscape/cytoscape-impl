package org.cytoscape.model.internal.column;

import java.util.HashMap;
import java.util.Map;

public class CanonicalStringPool {

	private static final int MAX_SIZE = 2000;
	
	private final Map<String,String> pool = new HashMap<>();
	
	public String canonicalize(String s) {
		if(pool.size() > MAX_SIZE) {
			pool.clear();
		}
		String canon = pool.get(s);
		if(canon == null) {
			pool.put(s, s);
			canon = s;
		}
		return canon;
	}

	public void clear() {
		pool.clear();
	}

}
