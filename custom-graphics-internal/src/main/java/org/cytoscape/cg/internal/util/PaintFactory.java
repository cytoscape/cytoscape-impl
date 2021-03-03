package org.cytoscape.cg.internal.util;

import java.awt.Paint;
import java.awt.geom.Rectangle2D;

/**
 * Factory for creating paint object for a rectangular bounds.
 */
public interface PaintFactory {
	
	/**
	 * Creates a new Paint object bounded by the given rectangular region.
	 */
	Paint getPaint(Rectangle2D bound);
}