package org.cytoscape.ding.impl.cyannotator.api;

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

	public double getBorderOpacity();
	public double getFillOpacity();

	public void setBorderOpacity(double opacity);
	public void setFillOpacity(double opacity);

	public Shape getShape();
}
