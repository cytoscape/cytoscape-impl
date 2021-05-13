package org.cytoscape.ding.impl;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.Objects;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ObjectPosition;


public class LabelSelection {

	private final View<CyNode> node; // node that associated with selected label.
	
	private final ObjectPosition originalPosition;
	private final double originalAngleDeg;
	
	private boolean angleChanged = false;
	private double angleRad;
	private double offsetX;
	private double offsetY;
	private double labelAnchorX;
	private double labelAnchorY;
	private Shape shape;

	
	public LabelSelection(
			View<CyNode> node, 
			Shape shape, 
			ObjectPosition originalPosition,
			double labelAnchorX, 
			double labelAnchorY,
			double angleDegress
	) {
		this.node = node;
		this.shape = shape; // may be rotated
		this.angleRad = Math.toRadians(angleDegress);
		this.originalAngleDeg = angleDegress;
		this.originalPosition = originalPosition;
		this.offsetX = originalPosition.getOffsetX();
		this.offsetY = originalPosition.getOffsetY();
		this.labelAnchorX = labelAnchorX;
		this.labelAnchorY = labelAnchorY;
	}

	
	public View<CyNode> getNode() {
		return node;
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
		var t = AffineTransform.getTranslateInstance(dx, dy);
		shape = t.createTransformedShape(shape);
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
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof LabelSelection) {
			return Objects.equals(((LabelSelection)other).node.getSUID(), node.getSUID());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return node.getSUID().hashCode();
	}
}
