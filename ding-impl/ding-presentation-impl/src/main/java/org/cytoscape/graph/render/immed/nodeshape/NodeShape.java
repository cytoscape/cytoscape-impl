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


import java.awt.Shape;


/**
 * An interface defining the methods necessary to render a node shape in GraphGraphics.
 */
public interface NodeShape {
	/**
	 * A legacy method to interact cleanly with the current implementation of
	 * GraphGraphics.  
	 * @return the byte associated with this node shape.
	 */
	byte getType();

	/**
	 * Returns a Shape object scaled to fit within the bounding box defined by the
	 * input parameters.
	 */
	Shape getShape(final float xMin,final float yMin, final float xMax, final float yMax);

	/**
	 * Computes the intersection of the node shape with and edge.  The edge is defined
	 * by the point at the center of the bounding box defined by xMin, yMin, xMax, yMax 
	 * and the point defined by ptX and ptY.  If the edge intersects with the shape then
	 * the point at which the edge and shape itersect is stored in returnVal, where the
	 * X location is in element 0 and the Y location is in element 1.
	 */
	boolean computeEdgeIntersection(final float xMin, final float yMin, final float xMax,
	                                final float yMax, final float ptX, final float ptY, 
	                                final float[] returnVal);
}

