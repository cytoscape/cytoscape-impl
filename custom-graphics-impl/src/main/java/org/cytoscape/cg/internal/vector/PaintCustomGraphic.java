package org.cytoscape.cg.internal.vector;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.cytoscape.cg.internal.util.PaintFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.PaintedShape;

public class PaintCustomGraphic implements PaintedShape {

	private Shape shape;
	private PaintFactory pf;

	public PaintCustomGraphic(Shape shape, PaintFactory factory) {
		this.shape = shape;
		this.pf = factory;
	}

	@Override
	public Rectangle2D getBounds2D() {
		return shape.getBounds2D();
	}

	@Override
	public Paint getPaint(Rectangle2D bounds) {
		return pf.getPaint(bounds);
	}

	@Override
	public Paint getPaint() {
		return pf.getPaint(shape.getBounds2D());
	}

	@Override
	public Shape getShape() {
		return shape;
	}

	@Override
	public Stroke getStroke() {
		return null;
	}

	@Override
	public Paint getStrokePaint() {
		return null;
	}

	@Override
	public CustomGraphicLayer transform(AffineTransform xform) {
		var s = xform.createTransformedShape(shape);
		
		return new PaintCustomGraphic(s, pf);
	}
}
