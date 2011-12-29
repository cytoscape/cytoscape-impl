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

	private static final double MIN = Double.MIN_VALUE;
	private static final double MAX = Double.MAX_VALUE;

	private double x;
	private double y;

	private final DGraphView graphView;
	private final DEdgeView view;

	private Point2D absolute;
	private Point2D ratio;

	public HandleImpl(DGraphView graphView, DEdgeView view, final double x,
			final double y) {
		this.graphView = graphView;
		this.view = view;

		if (validate(x) && validate(y)) {
			this.x = x;
			this.y = y;
		} else {
			throw new IllegalArgumentException("Given value is out-of-range.");
		}
	}

	private boolean validate(final double value) {
		if (value >= MIN && value <= MAX)
			return true;
		else
			return false;
	}

	@Override
	public double getXFraction() {
		
		System.out.println("!!!!!! Get X called: " + x);
		return x;
	}

	@Override
	public double getYFraction() {
		return y;
	}

//	@Override
//	public void setXFraction(double x) {
//		if (validate(x))
//			this.x = x;
//		else
//			throw new IllegalArgumentException("Given value is out-of-range.");
//	}
//
//	@Override
//	public void setYFraction(double y) {
//		if (validate(y))
//			this.y = y;
//		else
//			throw new IllegalArgumentException("Given value is out-of-range.");
//	}

	private double convertToRelativePosition() {
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

		// graphView.getNodeView(node);
		return 0d;
	}

	private Point2D convertToRatio(final Point2D absolutePoint) {
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

		double xRatio = getRatio(sourceX, targetX, absolutePoint.getX());
		double yRatio = getRatio(sourceY, targetY, absolutePoint.getY());
		relativePoint.setLocation(xRatio, yRatio);
		
		return relativePoint;
	}

	private double getRatio(double p1, double p2, double p) {
		double ratio;
		
		final double distance;
		if (p1 < p2) {
			distance = p2-p1;
			if(p1<p) {
				ratio = (p-p1)/distance;
			} else {
				ratio = (p1-p)/distance;
			}
		} else {
			distance = p1-p2;
			if(p2<p) {
				ratio = (p-p2)/distance;
			} else {
				ratio = (p2-p)/distance;
			}
		}
		
		return Math.abs(ratio);
	}

	@Override
	public void setPoint(double x, double y) {
		if (validate(x))
			this.x = x;
		else
			throw new IllegalArgumentException("Given value is out-of-range.");
		
		if (validate(y))
			this.y = y;
		else
			throw new IllegalArgumentException("Given value is out-of-range.");
		ratio = convertToRatio(new Point2D.Double(x, y));
		
		System.out.println("#### Ratio = " + ratio);
	}

}
