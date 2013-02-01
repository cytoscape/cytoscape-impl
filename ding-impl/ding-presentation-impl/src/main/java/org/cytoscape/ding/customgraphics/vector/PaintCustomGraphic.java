package org.cytoscape.ding.customgraphics.vector;

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

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.PaintedShape;
import org.cytoscape.graph.render.stateful.PaintFactory;

public class PaintCustomGraphic implements PaintedShape {
	private Shape shape;
	private PaintFactory pf;

	public PaintCustomGraphic(Shape shape, PaintFactory factory) {
		this.shape = shape;
		this.pf = factory;
	}

	public Rectangle2D getBounds2D() { return shape.getBounds2D(); } 

	public Paint getPaint(Rectangle2D bounds) {
		return pf.getPaint(bounds);
	}

	public Paint getPaint() {
		return pf.getPaint(shape.getBounds2D());
	}

	public Shape getShape() { return shape; }

	public Stroke getStroke() { return null; }
	public Paint getStrokePaint() { return null; }

	public CustomGraphicLayer transform(AffineTransform xform) {
		Shape s = xform.createTransformedShape(shape);
		return new PaintCustomGraphic(s, pf);
	}
	
}
