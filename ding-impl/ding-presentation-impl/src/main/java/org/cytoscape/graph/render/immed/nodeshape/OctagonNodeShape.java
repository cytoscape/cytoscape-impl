package org.cytoscape.graph.render.immed.nodeshape;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2009 - 2021 The Cytoscape Consortium
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


import org.cytoscape.graph.render.immed.GraphGraphics;

import java.awt.geom.GeneralPath;
import java.awt.Shape;


public class OctagonNodeShape extends AbstractNodeShape {
	private static final float SQRT2 = (float)Math.sqrt(2.0);
	private static final float SQRT2plus2 = 2.0f + SQRT2; 

	private final GeneralPath path; 

	public OctagonNodeShape() {
		super(GraphGraphics.SHAPE_OCTAGON);
		path = new GeneralPath(); 
	}
		
	public Shape getShape(float xMin, float yMin, float xMax, float yMax) {
		// If bounding box is square, then these eqns will create an
		// equilateral octagon.  If not, the sides will be scaled nicely.
		final float xx = (xMax - xMin)/SQRT2plus2;
		final float xz = xx * SQRT2; 

		final float yx = (yMax - yMin)/SQRT2plus2;
		final float yz = yx * SQRT2; 

		path.reset();
		
		path.moveTo( xMin,           yMin + yx );
		path.lineTo( xMin,           yMin + yx + yz ); 
		path.lineTo( xMin + xx,      yMax );
		path.lineTo( xMin + xx + xz, yMax );
		path.lineTo( xMax,           yMin + yx + yz ); 
		path.lineTo( xMax,           yMin + yx ); 
		path.lineTo( xMin + xx + xz, yMin ); 
		path.lineTo( xMin + xx,      yMin ); 

		path.closePath();

		return path;
	}
}

