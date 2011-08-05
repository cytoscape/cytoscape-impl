package org.cytoscape.view.vizmap.gui.internal;

import java.util.HashMap;
import java.util.Map;

public class AttributeSet {
	
	// Node, Edge, or Network.
	private final Class<?> targetObjectType;
	
	private final Map<String, Class<?>> attrNameTypeMap;

	public AttributeSet(final Class<?> targetObjectType) {
		this.targetObjectType = targetObjectType;
		this.attrNameTypeMap = new HashMap<String, Class<?>>();
	}
	
	public Class<?> getObjectType() {
		return this.targetObjectType;
	}
	
	public Map<String, Class<?>> getAttrMap() {
		return this.attrNameTypeMap;
	}
}
