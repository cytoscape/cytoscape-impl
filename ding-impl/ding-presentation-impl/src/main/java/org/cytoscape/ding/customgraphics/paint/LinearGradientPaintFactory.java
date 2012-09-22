package org.cytoscape.ding.customgraphics.paint;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.List;

import org.cytoscape.graph.render.stateful.PaintFactory;

public class LinearGradientPaintFactory extends GradientPaintFactory {
	private Point2D start;
	private Point2D end;
	
	public LinearGradientPaintFactory(List<Color> colors, List<Float> stops) {
		super(colors, stops);
		this.start = new Point2D.Float(0f,0f);
		this.end = new Point2D.Float(1f,0f);
	}

	public LinearGradientPaintFactory(List<Color> colors, List<Float> stops, Point2D start, Point2D end) {
		super(colors, stops);
		this.start = start;
		this.end = end;
	}
	
	public Paint getPaint(Rectangle2D bound) {
		this.paint = new LinearGradientPaint(scale(start, bound), scale(end, bound), stopArray, colorArray);

		return paint;
	}
}
