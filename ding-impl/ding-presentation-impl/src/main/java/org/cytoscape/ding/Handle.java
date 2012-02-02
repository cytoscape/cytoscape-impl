package org.cytoscape.ding;

import java.awt.geom.Point2D;

import org.cytoscape.ding.impl.DEdgeView;


/**
 * Defines a handle, an anchor point in the edge. 
 *
 */
public interface Handle {

	/**
	 * Calculate absolute position of this handle for the given edge view.
	 * 
	 * @param graphView graph view for this handle
	 * @param edgeView The handle belongs to this edge view.
	 * 
	 * @return Absolute position of this handle in the network view.
	 */
	Point2D calculateHandleLocation(final DEdgeView edgeView);
	
	/**
	 * Define this handle.  Handle will be described as a relative location from source and target node.
	 * 
	 * NOTE: in the implementation, the given (x, y) values will be converted to relative position.
	 * The conversion equation is exchangeable.
	 * 
	 * @param graphView graph view for this handle
	 * @param edgeView The handle belongs to this edge view.
	 * @param x Absolute value for X-location.
	 * @param y Absolute value for Y-location.
	 */
	void defineHandle(final DEdgeView edgeView, final double x, final double y);
	
	/**
	 * Create string representation of this object for parsing.
	 * @return
	 */
	String getSerializableString();
}
