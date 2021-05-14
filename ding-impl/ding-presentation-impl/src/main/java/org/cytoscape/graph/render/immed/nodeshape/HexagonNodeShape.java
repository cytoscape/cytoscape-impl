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


public class HexagonNodeShape extends LegacyCustomNodeShape {
	private static final double[] coords = new double[12]; 

	static {
		// 1x1 square centered around 0,0
		final double min = -0.5;
		final double max = 0.5;

		// defines a right triangle found within the hexagon
		final double x = (max - min)/4.0;             // horizontal
		final double z = x * 2.0;                     // hypotenuse
		final double y = z * Math.sin(Math.PI/3.0);   // vertical
		
		// X coordinates              Y coordinates
		coords[0]  = min;             coords[1]  = min + z;
		coords[2]  = min + x;         coords[3]  = min + z + y; 
		coords[4]  = min + x + z;     coords[5]  = min + z + y; 
		coords[6]  = max;             coords[7]  = min + z; 
		coords[8]  = min + x + z;     coords[9]  = max - z - y; 
		coords[10] = min + x;         coords[11] = max - z - y; 
	}

	public HexagonNodeShape() {
		super(coords, GraphGraphics.SHAPE_HEXAGON);
	}
}

