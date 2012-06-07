package org.cytoscape.view.vizmap.gui.internal;

import java.util.HashMap;
import java.util.Map;

public final class AttributeSet {
	
	// Node, Edge, or Network.
	private final Class<?> targetObjectType;
	private final Map<String, Class<?>> attrNameTypeMap;

	public AttributeSet(final Class<?> targetObjectType) {
		this.targetObjectType = targetObjectType;
		this.attrNameTypeMap = new HashMap<String, Class<?>>();
	}
	
	/**
	 * Graph object type
	 */
	public Class<?> getObjectType() {
		return this.targetObjectType;
	}
	
	/**
	 * Map from column name to column data type
	 */
	public Map<String, Class<?>> getAttrMap() {
		return this.attrNameTypeMap;
	}
}
