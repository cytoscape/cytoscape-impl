package org.cytoscape.ding.impl;

import java.awt.geom.Point2D;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.Handle;

/**
 * A simple implementation of edge handle.
 * 
 */
public class HandleImpl implements Handle {

	private static final String DELIMITER = ",";

	private double cosTheta = Double.NaN;
	private double sinTheta = Double.NaN;
	private double ratio = Double.NaN;

	// Original handle location
	private double x = 0;
	private double y = 0;
	
	public HandleImpl(final CyNetworkView graphView, final View<CyEdge> view, double x, double y) {
		defineHandle(graphView, view, x, y);
	}

	public HandleImpl(HandleImpl h) {
		this.x = h.x;
		this.y = h.y;
		this.cosTheta = h.cosTheta;
		this.sinTheta = h.sinTheta;
		this.ratio = h.ratio;
	}
	
	
	private static View<CyNode> getSourceNodeView(CyNetworkView graphView, View<CyEdge> edge) {
		if(graphView instanceof CyNetworkViewSnapshot)
			return ((CyNetworkViewSnapshot)graphView).getEdgeInfo(edge).getSourceNodeView();
		else
			return graphView.getNodeView(edge.getModel().getSource());
	}
	
	private static View<CyNode> getTargetNodeView(CyNetworkView graphView, View<CyEdge> edge) {
		if(graphView instanceof CyNetworkViewSnapshot)
			return ((CyNetworkViewSnapshot)graphView).getEdgeInfo(edge).getTargetNodeView();
		else
			return graphView.getNodeView(edge.getModel().getTarget());
	}
	

	@Override
	public Point2D calculateHandleLocation(final CyNetworkView graphView, final View<CyEdge> view) {
		if (Double.isNaN(sinTheta) || Double.isNaN(cosTheta)) {
			defineHandle(graphView, view, x, y);
		}
		
		View<CyNode> sourceView = getSourceNodeView(graphView, view);
		View<CyNode> targetView = getTargetNodeView(graphView, view);
		
		double sX = sourceView.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		double sY = sourceView.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);
		double tX = targetView.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		double tY = targetView.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		Point2D newPoint = convert(sX, sY, tX, tY);
		return newPoint;
	}

	
	@Override
	public void defineHandle(final CyNetworkView graphView, final View<CyEdge> view, double x, double y) {
		if(!Double.isNaN(x))
			this.x = x;
		
		if(!Double.isNaN(y))
			this.y = y;
		
		if(graphView != null && view != null)
			convertToRatio(graphView, view, new Point2D.Double(this.x, this.y));
	}

	private void convertToRatio(final CyNetworkView graphView, View<CyEdge> view, final Point2D absolutePoint) {
		final View<CyNode> sourceView = getSourceNodeView(graphView, view);
		final View<CyNode> targetView = getTargetNodeView(graphView, view);

		// Location of source node
		final double sX = sourceView.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final double sY = sourceView.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);
		
		// Location of target node
		final double tX = targetView.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final double tY = targetView.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);	
		
		// Location of handle
		final double hX = absolutePoint.getX();
		final double hY = absolutePoint.getY();

		// Vector v1
		// Distance from source to target (Edge length)
		final double v1x = tX - sX;
		final double v1y = tY - sY;
		// final double dist1 = Math.sqrt(Math.pow(v1x, 2) + Math.pow(v1y, 2));
		final double dist1 = Point2D.Double.distance(sX, sY, tX, tY);

		if (dist1 == 0.0) {
			// If the source and target are at the same location, use
			// reasonable defaults.
			ratio = 0;
			cosTheta = 0;
			sinTheta = 0;
		} else {
			// Vector v2
			// Distance from source to current handle
			final double v2x = hX - sX;
			final double v2y = hY - sY;
			// final double dist2 = Math.sqrt(Math.pow(v2x, 2) + Math.pow(v2y, 2));
			final double dist2 = Point2D.Double.distance(sX, sY, hX, hY);
	
			// Ratio of vector lengths
			ratio = dist2 / dist1;
	
			// Dot product of v1 and v2
			final double dotProduct = (v1x * v2x) + (v1y * v2y);
			cosTheta = dotProduct / (dist1 * dist2);
	
			// Avoid rounding problem
			if (cosTheta > 1.0d)
				cosTheta = 1.0d;
	
			// Theta is the angle between v1 and v2
			double theta = Math.acos(cosTheta);
			sinTheta = Math.sin(theta);
	
	//		 System.out.println("\n\n## Dot prod = " + dotProduct);
	//		 System.out.println("** cos = " + cosTheta);
	//		 System.out.println("** sin = " + sinTheta);
	//		 System.out.println("** theta = " + theta);
	//		 System.out.println("** (Hx, Hy) = (" + hX + ", " + hY + ")");
			final Point2D validate = convert(sX, sY, tX, tY);
			if (Math.abs(validate.getX() - hX) > 2 || Math.abs(validate.getY() - hY) > 2)
				sinTheta = -sinTheta;

			// Validate
			if (Double.isNaN(theta) || Double.isNaN(sinTheta))
				throw new IllegalStateException("Invalid angle: " + theta + ". Cuased by cos(theta) = " + cosTheta);
		}
	}

	/**
	 * Rotate and scale the vector to the handle position
	 * 
	 * @param sX
	 * @param sY
	 * @param tX
	 * @param tY
	 * @return
	 */
	private Point2D convert(double sX, double sY, double tX, double tY) {
		final Point2D newPoint = new Point2D.Double();
		// Original edge vector v = (vx, vy). (from source to target)
		final double vx = tX - sX;
		final double vy = tY - sY;

		// rotate
		double newX = vx * cosTheta - vy * sinTheta;
		double newY = vx * sinTheta + vy * cosTheta;

		// New rotated vector v' = (newX, newY).
		// Resize vector
		newX = newX * ratio;
		newY = newY * ratio;

		// ... And this is the new handle position.
		final double handleX = newX + sX;
		final double handleY = newY + sY;
		newPoint.setLocation(handleX, handleY);

		return newPoint;
	}

	/**
	 * Serialized string is "cos,sin,ratio".
	 */
	@Override
	public String getSerializableString() {
		return cosTheta + DELIMITER + sinTheta + DELIMITER + ratio;
	}

	private void setCos(final double cos) {
		this.cosTheta = cos;
	}

	private void setSin(final double sin) {
		this.sinTheta = sin;
	}

	private void setRatio(final double ratio) {
		this.ratio = ratio;
	}

	/**
	 * @param strRepresentation
	 * @return returns null for invalid inputs.
	 */
	public static Handle parseSerializableString(final String strRepresentation) {
		// Validate
		if (strRepresentation == null)
			return null;

		final String[] parts = strRepresentation.split(DELIMITER);
		if (parts.length == 2) {
			return process2xHandles(parts);
		} else if (parts.length != 3)
			return null;

		try {
			final double cos = Double.valueOf(parts[0]);
			final double sin = Double.valueOf(parts[1]);
			final double ratio = Double.valueOf(parts[2]);

			HandleImpl handle = new HandleImpl(null, null, 0, 0);
			handle.setSin(sin);
			handle.setCos(cos);
			handle.setRatio(ratio);
			return handle;
		} catch (Exception ex) {
			return null;
		}
	}

	private static final Handle process2xHandles(final String[] parts) {
		try {
			final double x = Double.valueOf(parts[0]);
			final double y = Double.valueOf(parts[1]);
			return new HandleImpl(null, null, x, y);
		} catch (Exception ex) {
			return null;
		}
		
	}

	
	@Override
	public String toString() {
		return "handle x:" + x + " y:" + y + " cosTheta: " + cosTheta + " sinTheta: " + sinTheta + " ratio: " + ratio;
	}
	
}
