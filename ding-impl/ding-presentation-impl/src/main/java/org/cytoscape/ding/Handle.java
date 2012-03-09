package org.cytoscape.ding;

import java.awt.geom.Point2D;

import org.cytoscape.view.model.View;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.model.CyEdge;


/**
 * Defines a handle, an anchor point in the edge. 
 *
 */
public interface Handle {

	/**
	 * Calculate absolute position of this handle for the given edge view.
	 * 
	 * @param netView graph view for this handle
	 * @param edgeView The handle belongs to this edge view.
	 * 
	 * @return Absolute position of this handle in the network view.
	 */
	Point2D calculateHandleLocation(final CyNetworkView netView, final View<CyEdge> edgeView);
	
	/**
	 * Define this handle.  Handle will be described as a relative location from source and target node.
	 * 
	 * NOTE: in the implementation, the given (x, y) values will be converted to relative position.
	 * The conversion equation is exchangeable.
	 * 
	 * @param netView graph view for this handle
	 * @param edgeView The handle belongs to this edge view.
	 * @param x Absolute value for X-location.
	 * @param y Absolute value for Y-location.
	 */
	void defineHandle(final CyNetworkView netView, final View<CyEdge> edgeView, final double x, final double y);
	
	/**
	 * Create string representation of this object for parsing.
	 * @return
	 */
	String getSerializableString();
}
