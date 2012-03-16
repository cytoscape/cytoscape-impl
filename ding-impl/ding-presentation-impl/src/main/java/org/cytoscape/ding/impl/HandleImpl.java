package org.cytoscape.ding.impl;

import java.awt.geom.Point2D;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.Handle;
import org.cytoscape.ding.impl.editor.EditMode;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.CyNetworkView;

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

		final Double sX = sourceView.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double sY = sourceView.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);
		final Double tX = targetView.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double tY = targetView.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		final Point2D newPoint = new Point2D.Double();
		
		if (EditMode.isDirectMode() == false) {
			
			// Original edge vector v = (vx, vy).  (from source to target)
			final double vx = tX-sX;
			final double vy = tY-sY;
			
			// rotate
			double newX = vx*cosTheta - vy*sinTheta;
			double newY = vx*sinTheta + vy*cosTheta;
			
			// New rotated vector v' = (newX, newY).
			// Resize vector
			newX = newX*ratio;
			newY = newY*ratio;
			
			// ... And this is the new handle position.
			final double handleX = newX+sX;
			final double handleY = newY+sY;
			newPoint.setLocation(handleX, handleY);
			
//			System.out.println("(Sx, Sy) = (" + sX + ", " + sY + ")");
//			System.out.println("(Tx, Ty) = (" + tX + ", " + tY + ")");
//			System.out.println("(handleX, handleY) = (" + handleX + ", " + handleY + ")");
//			System.out.println("** theta degree = " + Math.acos(cosTheta)* 180 / Math.PI);
		} else {
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
		final Double sX = sourceView.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double sY = sourceView.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		// Location of target node
		final Double tX = targetView.getVisualProperty(DVisualLexicon.NODE_X_LOCATION);
		final Double tY = targetView.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION);

		// Location of handle
		final double hX = absolutePoint.getX();
		final double hY = absolutePoint.getY();

		// Vector v1
		// Distance from source to target (Edge length)
		double dist1 = Math.sqrt(Math.pow(tX - sX, 2) + Math.pow(tY - sY, 2));
		
		// Vector v2
		// Distance from source to current handle
		double dist2 = Math.sqrt(Math.pow(hX - sX, 2) + Math.pow(hY - sY, 2));
		
		// Ratio of vector lengths
		ratio = dist2/dist1; 
				
		// Dot product of v1 and v2
		double dotProduct = ((tX-sX) * (hX-sX)) + ((tY-sY) * (hY-sY));
		double dist2Squared = Math.pow(dist2, 2);
		
		// vp is a component vector parallel to v2.
		// vp = k*v2
		final double r = dotProduct/dist2Squared;
		
		// Now, we can get the length of the vp
		final double lengthVp = Math.sqrt(Math.pow(r * (hX-sX), 2) + Math.pow(r * (hY-sY), 2));
		
		// Now we can get the Cos(theta)
		cosTheta = lengthVp/dist1;
		
//		// Theta is the angle between v1 and v2
		double theta = Math.acos(cosTheta);
		
		// Adjust angle if >PI/2
		if(dotProduct<0) {
			// PI/2< theta <=PI
			theta = Math.PI-theta;
			cosTheta = Math.cos(theta);
		}
		sinTheta = Math.sin(theta);
		
//		System.out.println("## Dot prod = " + dotProduct);
//		System.out.println("** cos = " + cosTheta);
//		System.out.println("** sin = " + sinTheta);
		
		// Check direction of rotation
		final double slopeE = (tY-sY)/(tX-sX);		
		
		// Rotate to opposite direction
		if((sX<=tX && sY <= tY) || (sX<tX && sY > tY)) {
			if (slopeE * hX > hY)
				sinTheta = -sinTheta;
		} else {
			if (slopeE * hX < hY)
				sinTheta = -sinTheta;
		}

//		System.out.println("** (Sx, Sy) = (" + sX + ", " + sY + ")");
//		System.out.println("** (Tx, Ty) = (" + tX + ", " + tY + ")");
//		System.out.println("!Handle ID = " + this.hashCode());
//		System.out.println("** edge Distance = " + dist1);
//		System.out.println("** Handle Distance = " + dist2);
//		System.out.println("** distance R2 = " + ratio);
//		System.out.println("** cos = " + cosTheta);
//		System.out.println("** sin = " + sinTheta);
//		System.out.println("** theta rad = " + theta);
//		System.out.println("** theta degree = " + (theta* 180 / Math.PI));
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
