package org.cytoscape.graph.render.immed.arrow;

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
 * Definition of Arrow shapes in this rendering engine.
 *
 */
public interface Arrow {
		
	/**
	 * The Shape of the main Arrow body.
	 */
	Shape getArrowShape();

	/**
	 * The Shape of the cap that joins the Arrow body with the edge.  This needs to
	 * be a distinct shape from the Arrow body because the cap needs to be the same
	 * color as the edge.
	 */
	Shape getCapShape(final double ratio);


	/**
	 * The distance that the arrow should be offset from the intersection with the node.
	 */
	double getTOffset();
}

