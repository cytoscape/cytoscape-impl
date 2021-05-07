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


public class TriangleNodeShape extends AbstractNodeShape {
	private final GeneralPath path; 

	public TriangleNodeShape() {
		super(GraphGraphics.SHAPE_TRIANGLE);
		path = new GeneralPath(); 
	}
		
	public Shape getShape(float xMin, float yMin, float xMax, float yMax) {
		path.reset();

		path.moveTo((xMin + xMax) / 2.0f, yMin);
		path.lineTo(xMax, yMax);
		path.lineTo(xMin, yMax);

		path.closePath();

		return path;
	}
}

