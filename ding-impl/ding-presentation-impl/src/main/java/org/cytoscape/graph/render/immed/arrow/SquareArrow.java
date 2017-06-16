package org.cytoscape.graph.render.immed.arrow;

import java.awt.geom.Rectangle2D;


public class SquareArrow extends AbstractArrow {
	public SquareArrow() {
		super(1.25);
		arrow = new Rectangle2D.Double(-1.25,-.5, 1, 1);
		// no  cap
	}
}
