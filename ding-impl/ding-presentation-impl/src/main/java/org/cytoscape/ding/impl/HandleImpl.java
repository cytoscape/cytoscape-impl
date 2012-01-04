package org.cytoscape.ding.impl;

import java.awt.geom.Point2D;

import org.cytoscape.ding.Handle;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;

/**
 * A simple implementation of edge handle.
 * 
 */
public class HandleImpl implements Handle {

	// Original handle location
	private double x;
	private double y;

	private double[] orthVector;
	private double originalDist;
	private double positionRatioOnEdge;

	public HandleImpl(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public Point2D getPoint(final DGraphView graphView, final DEdgeView view) {

		final CyNode source = view.getModel().getSource();
		final CyNode target = view.getModel().getTarget();
		final View<CyNode> sourceView = graphView.getNodeView(source);
		final View<CyNode> targetView = graphView.getNodeView(target);

		final Double sX = sourceView
				.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double sY = sourceView
				.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		final Double tX = targetView
				.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double tY = targetView
				.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		final Point2D newPoint = new Point2D.Double();
		if (orthVector != null) {
			final double newDist = Math.sqrt(Math.pow(tX - sX, 2) + Math.pow(tY - sY, 2));
			final double newRatio = newDist / originalDist;
			final double[] newOrth = new double[2];
			newOrth[0] = orthVector[0] * newRatio;
			newOrth[1] = orthVector[1] * newRatio;
			
			final double newX = newOrth[0] + positionRatioOnEdge * (tX - sX) + sX;
			final double newY = newOrth[1] + positionRatioOnEdge * (tY - sY) + sY;

			newPoint.setLocation(newX, newY);
		} else {
			newPoint.setLocation(x, y);
		}

		return newPoint;
	}
	
	@Override
	public void setPoint(DGraphView graphView, DEdgeView view, double x, double y) {
		this.x = x;
		this.y = y;

		convertToRatio(graphView, view, new Point2D.Double(x, y));
	}

	private void convertToRatio(DGraphView graphView, DEdgeView view,
			final Point2D absolutePoint) {
		orthVector = new double[2];

		final CyNode source = view.getModel().getSource();
		final CyNode target = view.getModel().getTarget();
		final View<CyNode> sourceView = graphView.getNodeView(source);
		final View<CyNode> targetView = graphView.getNodeView(target);

		final Double sX = sourceView
				.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double sY = sourceView
				.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		final Double tX = targetView
				.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double tY = targetView
				.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		final double hX = absolutePoint.getX();
		final double hY = absolutePoint.getY();

		final double oX;
		final double oY;
		final double k;

		// Solved the equation to find orthogonal vector manually...  Can be replaced once we find better 2D Vector library.
		k = -((tX - sY) * (sX - tX) + (tY - sY) * (sY - tY))
				/ (hX * tX - hX * sX + hY * tY - hY * sY + sX * sX - sX * tX
						+ sY * sY - sY * tY);
		oX = (tX - sX + k * sX) / k;
		oY = (tY - sY + k * sY) / k;

		orthVector[0] = hX - oX;
		orthVector[1] = hY - oY;

		originalDist = Math.sqrt(Math.pow(tX - sX, 2) + Math.pow(tY - sY, 2));
		positionRatioOnEdge = Math.sqrt(Math.pow(oX - sX, 2)
				+ Math.pow(oY - sY, 2))
				/ originalDist;
	}
}
