package org.cytoscape.model.internal.column;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class CanonicalStringPool {

	private static final int MAX_SIZE = 2000;
	
	private final Map<String,String> pool = new Object2ObjectOpenHashMap<>();
	
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
