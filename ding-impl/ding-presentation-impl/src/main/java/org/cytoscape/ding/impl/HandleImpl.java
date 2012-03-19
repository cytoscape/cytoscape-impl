package org.cytoscape.ding.impl;

import java.awt.geom.Point2D;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.Handle;
import org.cytoscape.ding.impl.editor.EditMode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/**
 * A simple implementation of edge handle.
 * 
 */
public class HandleImpl implements Handle {
	
	private static final String DELIMITER =",";
	
	private Double cosTheta = 0.5d;
	private Double sinTheta = 0.5d;
	private Double ratio = 0.5;
	
	// Original handle location
	private double x = 0;
	private double y = 0;
	
	public HandleImpl(double x, double y) {
		this.x = x;
		this.y = y;
	}


	@Override
	public Point2D calculateHandleLocation(final CyNetworkView graphView, final View<CyEdge> view) {
		final CyNode source = view.getModel().getSource();
		final CyNode target = view.getModel().getTarget();
		
		final View<CyNode> sourceView = graphView.getNodeView(source);
		final View<CyNode> targetView = graphView.getNodeView(target);

		final double sX = sourceView.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final double sY = sourceView.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);
		final double tX = targetView.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final double tY = targetView.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		final Point2D newPoint;
		if (EditMode.isDirectMode() == false) {
			newPoint = convert(sX, sY, tX, tY);
		} else {
			newPoint = new Point2D.Double();
			if(x==0 && y==0) {
				// If default, use center
				newPoint.setLocation(tX - sX, tY - sY);
			} else
				newPoint.setLocation(x, y);
		}
		return newPoint;
	}

	
	@Override
	public void defineHandle(final CyNetworkView graphView, final View<CyEdge> view, double x, double y) {
		this.x = x;
		this.y = y;
		convertToRatio(graphView, view, new Point2D.Double(x, y));
	}

	private void convertToRatio(final CyNetworkView graphView, View<CyEdge> view, final Point2D absolutePoint) {
		final CyNode source = view.getModel().getSource();
		final CyNode target = view.getModel().getTarget();
		final View<CyNode> sourceView = graphView.getNodeView(source);
		final View<CyNode> targetView = graphView.getNodeView(target);

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
		final double dist1 = Math.sqrt(Math.pow(v1x, 2) + Math.pow(v1y, 2));
		
		// Vector v2
		// Distance from source to current handle
		final double v2x = hX - sX;
		final double v2y = hY - sY;
		final double dist2 = Math.sqrt(Math.pow(v2x, 2) + Math.pow(v2y, 2));
		
		// Ratio of vector lengths
		ratio = dist2/dist1; 
				
		// Dot product of v1 and v2
		final double dotProduct = (v1x * v2x) + (v1y * v2y);
		cosTheta = dotProduct/(dist1*dist2);
		
		// Theta is the angle between v1 and v2
		final double theta = Math.acos(cosTheta);
		sinTheta = Math.sin(theta);
		
//		System.out.println("\n\n## Dot prod = " + dotProduct);
//		System.out.println("** cos = " + cosTheta);
//		System.out.println("** sin = " + sinTheta);
//		System.out.println("** theta = " + theta);
//		System.out.println("** (Hx, Hy) = (" + hX + ", " + hY + ")");
		final Point2D validate = convert(sX, sY, tX, tY);
		if(Math.abs(validate.getX()-hX) > 2 || Math.abs(validate.getY()-hY) > 2)
			sinTheta = -sinTheta;
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
	private Point2D convert(double sX, double sY,double tX, double tY ) {
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
		if(cosTheta == null || sinTheta  == null || ratio == null)
			return null;
		return cosTheta.toString() + DELIMITER + sinTheta.toString() + DELIMITER + ratio;
	}
	
	private void setCos(final Double cos) {
		this.cosTheta = cos;
	}
	
	private void setSin(final Double sin) {
		this.sinTheta = sin;
	}
	
	private void setRatio(final Double ratio) {
		this.ratio = ratio;
	}
	
	/**
	 * @param strRepresentation
	 * @return returns null for invalid inputs.
	 */
	public static Handle parseSerializableString(final String strRepresentation) {
		// Validate
		if(strRepresentation == null)
			return null;
		
		final String[] parts = strRepresentation.split(DELIMITER);
		if(parts.length != 3)
			return null;
		
		try {
			final Double cos = Double.parseDouble(parts[0]);
			final Double sin = Double.parseDouble(parts[1]);
			final Double ratio = Double.parseDouble(parts[2]);
			
			HandleImpl handle = new HandleImpl(0, 0);
			handle.setSin(sin);
			handle.setCos(cos);
			handle.setRatio(ratio);
			return handle;
		} catch (Exception ex) {
			return null;
		}		
	}
}
