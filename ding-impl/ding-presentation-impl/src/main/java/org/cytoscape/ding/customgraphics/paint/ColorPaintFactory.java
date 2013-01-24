package org.cytoscape.ding.customgraphics.paint;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.cytoscape.graph.render.stateful.PaintFactory;

public class ColorPaintFactory implements PaintFactory {

	private Color color;
	
	public ColorPaintFactory(final Color color) {
		this.color = color;
	}
	
	public void setColor(final Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return this.color;
	}
	

	public Paint getPaint(Rectangle2D arg0) {
		return color;
	}

}
