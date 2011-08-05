package org.cytoscape.ding.customgraphics;


/**
 * Wrapper for actual implementations of layers.
 * In current version, it's always CustomGraphic 
 *
 */
public interface Layer<T> {
	
	/**
	 * Each layer has immutable Z-Order value for rendering.
	 * This method returns the value as int.
	 * 
	 * @return
	 */
	int getZorder();
	
	T getLayerObject();
}
