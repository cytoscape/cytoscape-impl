package org.cytoscape.ding.impl.cyannotator.api;

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

	public void drawArrow(Graphics g);
}
