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

