/*
 Copyright (c) 2009, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.graph.render.immed.nodeshape;


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

