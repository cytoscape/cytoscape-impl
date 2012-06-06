package org.cytoscape.ding.impl.cyannotator.api;

import java.awt.Graphics;
import java.awt.Paint;
import java.awt.geom.Point2D;

import org.cytoscape.model.CyNode;

public interface ArrowAnnotation extends Annotation {
	public enum ArrowType {
		CIRCLE ("Circle"),
		CLOSED ("Closed Arrow"),
		DIAMOND ("Diamond"),
		OPEN ("Open Arrow"),
		TRIANGLE ("Triangular Head"),
		TSHAPE ("T-Shape");

		private final String name;
		ArrowType (String name) { 
			this.name = name; 
		}
		public String arrowName() { return this.name; }

		public String toString() { return this.name; }
	}

	public enum ArrowDirection { FORWARD, BACKWARD; }

	public Annotation getSource();
	public void setSource(Annotation source);

	public Object getTarget();
	public void setTarget(Annotation target); // Object must be one of: Annotation, CyNode, or Point
	public void setTarget(CyNode target); // Object must be one of: Annotation, CyNode, or Point
	public void setTarget(Point2D target); // Object must be one of: Annotation, CyNode, or Point2D

	public double getLineWidth();
	public void setLineWidth(double width);

	public double getArrowSize();
	public void setArrowSize(double width);

	public Paint getArrowColor();
	public void setArrowColor(Paint color);

	public ArrowType getArrowType();
	public void setArrowType(ArrowType type);

	public ArrowDirection getArrowDirection();
	public void setArrowDirection(ArrowDirection direction);

	public void drawArrow(Graphics g);
}
