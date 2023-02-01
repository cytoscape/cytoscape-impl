package org.cytoscape.ding.internal.util;

import static org.cytoscape.ding.internal.util.MathUtil.getGradientAngle;
import static org.cytoscape.ding.internal.util.MathUtil.getGradientAxis;
import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.junit.Test;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class MathUtilTest {
	
	/*
	   (-1,-1)---( 0,-1)---( 1,-1)
	      |         |         |
	   (-1, 0)---( 0, 0)---( 1, 0)
	      |         |         |
	   (-1, 1)---( 0, 1)---( 1, 1)
	 */
	private Rectangle2D gradientBounds = new Rectangle2D.Double(-1, -1, 2, 2);
	
	@Test
	public void testGetGradientAngle() {
		assertEquals(  0.0, getGradientAngle(new Point.Double(-1, 0), new Point.Double( 1, 0)), 0.0);
		assertEquals( 45.0, getGradientAngle(new Point.Double(-1, 1), new Point.Double( 1,-1)), 0.0);
		assertEquals( 90.0, getGradientAngle(new Point.Double( 0, 1), new Point.Double( 0,-1)), 0.0);
		assertEquals(135.0, getGradientAngle(new Point.Double( 1, 1), new Point.Double(-1,-1)), 0.0);
		assertEquals(180.0, getGradientAngle(new Point.Double( 1, 0), new Point.Double(-1, 0)), 0.0);
		assertEquals(225.0, getGradientAngle(new Point.Double( 1,-1), new Point.Double(-1, 1)), 0.0);
		assertEquals(270.0, getGradientAngle(new Point.Double( 0,-1), new Point.Double( 0, 1)), 0.0);
		assertEquals(315.0, getGradientAngle(new Point.Double(-1,-1), new Point.Double( 1, 1)), 0.0);
	}
	
	@Test
	public void testGetGradientAxix() {
		assertEqualLines(new Line2D.Double(-1, 0,  1, 0), getGradientAxis(gradientBounds,   0.0));
		assertEqualLines(new Line2D.Double(-1, 1,  1,-1), getGradientAxis(gradientBounds,  45.0));
		assertEqualLines(new Line2D.Double( 0, 1,  0,-1), getGradientAxis(gradientBounds,  90.0));
		assertEqualLines(new Line2D.Double( 1, 1, -1,-1), getGradientAxis(gradientBounds, 135.0));
		assertEqualLines(new Line2D.Double( 1, 0, -1, 0), getGradientAxis(gradientBounds, 180.0));
		assertEqualLines(new Line2D.Double( 1,-1, -1, 1), getGradientAxis(gradientBounds, 225.0));
		assertEqualLines(new Line2D.Double( 0,-1,  0, 1), getGradientAxis(gradientBounds, 270.0));
		assertEqualLines(new Line2D.Double(-1,-1,  1, 1), getGradientAxis(gradientBounds, 315.0));
	}
	
	private void assertEqualLines(Line2D l1, Line2D l2) {
		double delta = 0.0000001;
		assertEquals(l1.getX1(), l2.getX1(), delta);
		assertEquals(l1.getX2(), l2.getX2(), delta);
		assertEquals(l1.getY1(), l2.getY1(), delta);
		assertEquals(l1.getY2(), l2.getY2(), delta);
	}
}
