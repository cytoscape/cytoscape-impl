package org.cytoscape.ding.impl;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.Objects;

import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ObjectPosition;


public class LabelSelection {

	private final View<CyNode> node; // node that associated with selected label.
	private final View<CyEdge> edge; // edge that associated with selected label.
	
	private final ObjectPosition originalPosition;
	private final double originalAngleDeg;
	
	private boolean angleChanged = false;
	private double angleRad;
	private double offsetX;
	private double offsetY;
	private double labelAnchorX;
	private double labelAnchorY;
	private Shape shape;
	private double edgeSlope;
	private double edgeAngle;

	
	public LabelSelection(
			View<CyEdge> edge, 
			Shape shape, 
			ObjectPosition originalPosition,
			double labelAnchorX, 
			double labelAnchorY,
			double angleDegrees,
      double edgeSlope,
      double edgeAngle) {
    this.node = null;
    this.edge = edge;
		this.shape = shape; // may be rotated
		this.angleRad = Math.toRadians(angleDegrees);
		this.originalAngleDeg = angleDegrees;
		this.originalPosition = originalPosition;
		this.offsetX = originalPosition.getOffsetX();
		this.offsetY = originalPosition.getOffsetY();
		this.labelAnchorX = labelAnchorX;
		this.labelAnchorY = labelAnchorY;
    this.edgeSlope = edgeSlope;
    this.edgeAngle = edgeAngle;
  }


	public LabelSelection(
			View<CyNode> node, 
			Shape shape, 
			ObjectPosition originalPosition,
			double labelAnchorX, 
			double labelAnchorY,
			double angleDegrees
	) {
		this.node = node;
    this.edge = null;
		this.shape = shape; // may be rotated
		this.angleRad = Math.toRadians(angleDegrees);
		this.originalAngleDeg = angleDegrees;
		this.originalPosition = originalPosition;
		this.offsetX = originalPosition.getOffsetX();
		this.offsetY = originalPosition.getOffsetY();
		this.labelAnchorX = labelAnchorX;
		this.labelAnchorY = labelAnchorY;
	}

	
	public View<CyNode> getNode() {
		return node;
	}
	
	public View<CyEdge> getEdge() {
		return edge;
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public double getOriginalAngleDegrees() {
		return originalAngleDeg;
	}
	
	public double getAngleDegrees() {
		return angleChanged ? Math.toDegrees(angleRad) : originalAngleDeg;
	}
	
	public ObjectPosition getOriginalPosition() {
		return originalPosition;
	}
	
	public ObjectPosition getPosition() {
		ObjectPosition op = new ObjectPosition(originalPosition);
		op.setOffsetX(offsetX);
		op.setOffsetY(offsetY);
		return op;
	}
	
	
	public void translate(double dx, double dy) {
    // System.out.println("Translate by "+dx+","+dy);
		var t = AffineTransform.getTranslateInstance(dx, dy);
		shape = t.createTransformedShape(shape);

    if (edge != null && edgeSlope != 0) {
      // System.out.println("dx,dy = "+dx+","+dy);
      // This is an edge label move.  We need to essentially reverse the
      // transformation that we'll later do
      double[] xy1 = new double[2];
      double[] xy2 = new double[2];

      // Translate the current anchor to where the user sees it
      GraphRenderer.updateOffset(offsetX, offsetY, edgeSlope, edgeAngle, xy1);

      // System.out.println("xy1 = "+xy1[0]+","+xy1[1]);

      double newX = xy1[0]+dx; // This is what the user will see
      double newY = xy1[1]+dy; // This is what the user will see
      // System.out.println("newX,newY = "+newX+","+newY);

      GraphRenderer.reverseOffset(newX, newY, edgeSlope, edgeAngle, xy2);
      // System.out.println("xy2 = "+xy2[0]+","+xy2[1]);

      // GraphRenderer.updateOffset(xy2[0], xy2[1], edgeSlope, edgeAngle, xy1);
      // System.out.println("xy1 = "+xy1[0]+","+xy1[1]+" should equal "+newX+","+newY);

      dx = xy2[0]-offsetX;
      dy = xy2[1]-offsetY;
    } 

    labelAnchorX += dx;
    labelAnchorY += dy;
    offsetX += dx;
    offsetY += dy;
	}
	
	public void rotate(double rads) {
		var t = AffineTransform.getRotateInstance(rads, labelAnchorX, labelAnchorY);
		this.shape = t.createTransformedShape(shape);
		angleRad += rads;
		angleChanged = true;
	}
	
	public double getAnchorX() {
		return labelAnchorX;
	}
	
	public double getAnchorY() {
		return labelAnchorY;
	}

  public double getEdgeSlope() {
    return edgeSlope;
  }

  public double getEdgeAngle() {
    return edgeAngle;
  }
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof LabelSelection) {
      if (node != null)
        return Objects.equals(((LabelSelection)other).node.getSUID(), node.getSUID());
      else
        return Objects.equals(((LabelSelection)other).edge.getSUID(), edge.getSUID());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
    if (node != null)
      return node.getSUID().hashCode();
    else
      return edge.getSUID().hashCode();
	}
}
