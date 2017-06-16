package org.cytoscape.graph.render.immed.arrow;

import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;


public class HalfCircleArrow extends AbstractArrow {
	public HalfCircleArrow(boolean filled) {
		super(1);
		arrow = new Arc2D.Double(-1f, -0.5f, 1f, 1f, 90, 180, filled ? Arc2D.PIE : Arc2D.OPEN);
		// no  cap
//		System.out.println("HalfCircleArrow ----------------------------------");
	}
}
