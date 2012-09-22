package org.cytoscape.ding.customgraphics.paint;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.List;

import org.cytoscape.graph.render.stateful.PaintFactory;

public abstract class GradientPaintFactory implements PaintFactory {
	protected Color[] colorArray;
	protected float[] stopArray;
	
	protected Paint paint;

	public GradientPaintFactory(List<Color>colorList, List<Float>stopList) {
		colorArray = new Color[colorList.size()];
		stopArray = new float[colorList.size()];
		for (int index = 0; index < colorArray.length; index++) {
			colorArray[index] = colorList.get(index);
			stopArray[index] = stopList.get(index).floatValue();
		}
	}

	protected Point2D scale(Point2D point, Rectangle2D bound) {
		double xvalue = point.getX() * bound.getWidth() + bound.getX();
		double yvalue = point.getY() * bound.getHeight() + bound.getY();
		return new Point2D.Float((float)xvalue, (float)yvalue);
	}

}
