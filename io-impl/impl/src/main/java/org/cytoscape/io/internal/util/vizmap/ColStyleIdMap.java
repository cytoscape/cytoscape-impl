package org.cytoscape.io.internal.util.vizmap;

import java.util.IdentityHashMap;

import org.cytoscape.view.vizmap.VisualStyle;

public class ColStyleIdMap {

	private final IdentityHashMap<VisualStyle,Integer> idMap = new IdentityHashMap<>();
	private int next = 1;
	
	public synchronized String getId(VisualStyle vs) {
		return String.valueOf(idMap.computeIfAbsent(vs, k -> next++));
	}
	
	public boolean contains(VisualStyle vs) {
		return idMap.containsKey(vs);
	}
}
