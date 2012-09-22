package org.cytoscape.ding.customgraphics.paint;

import java.awt.Color;
import java.awt.RadialGradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.List;

import org.cytoscape.graph.render.stateful.PaintFactory;

public class RadialGradientPaintFactory extends GradientPaintFactory {
	private Point2D center;
	private float size;
	
	public RadialGradientPaintFactory(List<Color> colors, List<Float> stops) {
		super(colors, stops);
		this.center = new Point2D.Float(0.5f, 0.5f);
		this.size = .5f;
	}

	public RadialGradientPaintFactory(List<Color> colors, List<Float> stops, Point2D center, float size) {
		super(colors, stops);
		this.center = center;
		this.size = size;
	}
	

	public Paint getPaint(Rectangle2D bound) {
		double diameter = Math.min(bound.getWidth(), bound.getHeight());

		paint = new RadialGradientPaint(scale(center, bound), (float)(size*diameter), stopArray, colorArray);
		
		return paint;
	}
}
