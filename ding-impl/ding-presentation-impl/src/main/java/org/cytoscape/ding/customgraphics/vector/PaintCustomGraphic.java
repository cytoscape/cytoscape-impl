package org.cytoscape.ding.customgraphics.vector;

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

	public Rectangle getBounds() { return shape.getBounds(); } 

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
