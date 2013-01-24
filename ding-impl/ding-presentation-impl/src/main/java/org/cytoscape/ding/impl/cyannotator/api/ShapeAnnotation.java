package org.cytoscape.ding.impl.cyannotator.api;

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
import java.awt.Shape;
import java.awt.geom.Rectangle2D;


public interface ShapeAnnotation extends Annotation {

	public enum ShapeType {
		RECTANGLE ("Rectangle"),
		ROUNDEDRECTANGLE ("Rounded Rectangle"),
		ELLIPSE ("Ellipse"),
		TRIANGLE ("Triangle"),
		PENTAGON ("Pentagon"),
		STAR5 ("5-Pointed Star"),
		HEXAGON ("Hexagon"),
		STAR6 ("6-Pointed Star");
	
		private final String name;
		ShapeType (String name) { 
			this.name = name; 
		}
	
		public String shapeName() {
			return this.name;
		}

		public String toString() {
			return this.name;
		}
	} 

	// These two methods provide a way to get the shapes
	// supported by this implementation
	public ShapeType[] getSupportedShapes();

	public void setSize(double width, double height);

	public ShapeType getShapeType();
	public void setShapeType(ShapeType type);

	public double getBorderWidth();
	public void setBorderWidth(double width);

	public Paint getBorderColor();
	public Paint getFillColor();

	public void setBorderColor(Paint border);
	public void setFillColor(Paint fill);

	public Shape getShape();
}
