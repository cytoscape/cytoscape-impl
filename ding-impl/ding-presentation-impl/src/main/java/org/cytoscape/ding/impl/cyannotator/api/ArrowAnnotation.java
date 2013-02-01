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

import java.awt.Graphics;
import java.awt.Paint;
import java.awt.geom.Point2D;

import org.cytoscape.model.CyNode;

public interface ArrowAnnotation extends Annotation {
	public enum ArrowType {
		CIRCLE ("Circle"),
		CLOSED ("Closed Arrow"),
		CONCAVE ("Concave Arrow"),
		DIAMOND ("Diamond"),
		OPEN ("Open Arrow"),
		NONE ("No Arrow"),
		TRIANGLE ("Triangular Head"),
		TSHAPE ("T-Shape");

		private final String name;
		ArrowType (String name) { 
			this.name = name; 
		}
		public String arrowName() { return this.name; }

		public String toString() { return this.name; }
	}

	public enum ArrowEnd { SOURCE, TARGET; }

	public Annotation getSource();
	public void setSource(Annotation source);

	public Object getTarget();
	public void setTarget(Annotation target); // Object must be one of: Annotation, CyNode, or Point
	public void setTarget(CyNode target); // Object must be one of: Annotation, CyNode, or Point
	public void setTarget(Point2D target); // Object must be one of: Annotation, CyNode, or Point2D

	public double getLineWidth();
	public void setLineWidth(double width);

	public Paint getLineColor();
	public void setLineColor(Paint color);

	public double getArrowSize(ArrowEnd end);
	public void setArrowSize(ArrowEnd end, double width);

	public Paint getArrowColor(ArrowEnd end);
	public void setArrowColor(ArrowEnd end, Paint color);

	public ArrowType getArrowType(ArrowEnd end);
	public void setArrowType(ArrowEnd end, ArrowType type);

	public ArrowType[] getSupportedArrows();

	// public void drawArrow(Graphics g, boolean isPrinting);
}
