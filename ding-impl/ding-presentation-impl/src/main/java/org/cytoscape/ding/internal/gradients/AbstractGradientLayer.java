package org.cytoscape.ding.internal.gradients;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;

public abstract class AbstractGradientLayer implements Cy2DGraphicLayer {
	
	protected final List<ControlPoint> controlPoints;
	protected final Color[] colors;
	protected final float[] positions;

	protected Paint paint;

	public AbstractGradientLayer(final List<ControlPoint> controlPoints) {
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
	
	@Override
	public void draw(final Graphics2D g, final Shape shape, final CyNetworkView networkView, 
			final View<? extends CyIdentifiable> view) {
		g.setPaint(getPaint(shape.getBounds2D()));
		g.fill(shape);
	}

	protected Point2D scale(Point2D point, Rectangle2D bound) { 
		double xvalue = point.getX() * bound.getWidth() + bound.getX();
		double yvalue = point.getY() * bound.getHeight() + bound.getY();
		
		return new Point2D.Float((float)xvalue, (float)yvalue);
	}
}
