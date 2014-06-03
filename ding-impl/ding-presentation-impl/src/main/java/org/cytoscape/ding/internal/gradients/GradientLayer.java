package org.cytoscape.ding.internal.gradients;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public abstract class GradientLayer implements CustomGraphicLayer {
	
	protected final List<ControlPoint> controlPoints;
	protected final Color[] colors;
	protected final float[] positions;

	protected Paint paint;

	public GradientLayer(final List<ControlPoint> controlPoints) {
		this.controlPoints = controlPoints != null ? controlPoints : new ArrayList<ControlPoint>();
		
		colors = new Color[controlPoints.size()];
		positions = new float[controlPoints.size()];
		int i = 0;
		
		for (ControlPoint cp : controlPoints) {
			colors[i] = cp.color;
			positions[i] = cp.position;
			i++;
		}
	}

	protected Point2D scale(Point2D point, Rectangle2D bound) { 
		double xvalue = point.getX() * bound.getWidth() + bound.getX();
		double yvalue = point.getY() * bound.getHeight() + bound.getY();
		
		return new Point2D.Float((float)xvalue, (float)yvalue);
	}
}
