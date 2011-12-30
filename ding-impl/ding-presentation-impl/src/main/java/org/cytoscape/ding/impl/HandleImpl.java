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

//	private static final double MIN = Double.MIN_VALUE;
//	private static final double MAX = Double.MAX_VALUE;

	private double x;
	private double y;
	
	private Point2D ratio;

	public HandleImpl(DGraphView graphView, DEdgeView view, final double x,
			final double y) {
		this.x = x;
		this.y = y;		
	}

//	@Override
//	public double getXFraction(DGraphView graphView, DEdgeView view) {
//		
//		System.out.println("!!!!!! Get X called: " + x);
//		if(ratio == null)
//			return x;
//		else
//			return convertToAbsolute(true);
//	}
//
//	@Override
//	public double getYFraction() {
//		System.out.println("!!!!!! Get Y called: " + y);
//		if(ratio == null)
//			return y;
//		else
//			return convertToAbsolute(false);
//	}
	
	@Override
	public Point2D getPoint(DGraphView graphView, DEdgeView view) {
		return convertToAbsolute(graphView, view);
	}


	private Point2D convertToAbsolute(DGraphView graphView, DEdgeView view) {
		final CyNode source = view.getModel().getSource();
		final CyNode target = view.getModel().getTarget();
		final View<CyNode> sourceView = graphView.getNodeView(source);
		final View<CyNode> targetView = graphView.getNodeView(target);

		final Double sourceX = sourceView
				.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double sourceY = sourceView
				.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		final Double targetX = targetView
				.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double targetY = targetView
				.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		final Point2D newPoint = new Point2D.Double();
		if(ratio != null) {
		double newX = getAbsolute(sourceX, targetX, ratio.getX());
		double newY = getAbsolute(sourceY, targetY, ratio.getY());
		newPoint.setLocation(newX, newY);
		} else {
			newPoint.setLocation(x, y);
		}
		
		return newPoint;
	}

	private Point2D convertToRatio(DGraphView graphView, DEdgeView view, final Point2D absolutePoint) {
		final Point2D relativePoint = new Point2D.Float();
		final CyNode source = view.getModel().getSource();
		final CyNode target = view.getModel().getTarget();
		final View<CyNode> sourceView = graphView.getNodeView(source);
		final View<CyNode> targetView = graphView.getNodeView(target);

		final Double sourceX = sourceView
				.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double sourceY = sourceView
				.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		final Double targetX = targetView
				.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double targetY = targetView
				.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		final double xRatio = getRatio(sourceX, targetX, absolutePoint.getX());
		final double yRatio = getRatio(sourceY, targetY, absolutePoint.getY());
		
		relativePoint.setLocation(xRatio, yRatio);
		
		return relativePoint;
	}

	private double getRatio(double p1, double p2, double p) {
		final double distance = Math.abs(p2-p1);
		if(distance == 0)
			return 0.5;
		else
			return (p-p1)/distance;			
	}
	
	private double getAbsolute(double p1, double p2, double r) {
		final double distance = Math.abs(p2-p1);
		return p1 + (distance * r);
	}

	@Override
	public void setPoint(DGraphView graphView, DEdgeView view, double x, double y) {
		this.x = x;
		this.y = y;

		ratio = convertToRatio(graphView, view, new Point2D.Double(x, y));		
	}

}
