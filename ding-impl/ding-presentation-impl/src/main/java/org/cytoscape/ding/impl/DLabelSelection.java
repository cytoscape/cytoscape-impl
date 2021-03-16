package org.cytoscape.ding.impl;

import static org.cytoscape.ding.DVisualLexicon.NODE_LABEL_POSITION;

import java.util.HashSet;
import java.util.Set;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.presentation.property.values.Position;

/**
 * A wrapper to store the node and label information when a label is selected by
 * a mouse press event.
 * 
 * @author jingchen
 */
public class DLabelSelection {

	private View<CyNode> node; // node that associated with selected label.
	private Rectangle rectangle; // bound of the label

	// Original upper left corner of the Label rectangle
	private int originalRectX;
	private int originalRectY;

	private ObjectPosition previousValue;
	private Double previousRotation;
	private double currentRotation = Math.toRadians(0d);

	private boolean nodeWasSelected;

	/**
	 * 
	 * @param selectedNode    node view that associates with the selected label
	 * @param labelRectangle  bound of the label in the screen.
	 * @param oldPosition     the previous value of the LabelPosistion property.
	 *                        null if it was not set before.
	 * @param oldRotation     the previous value of the LabelRotation property in
	 *                        degrees. null if it was not set before.
	 * @param nodeWasSelected specify if the node was selected when the label was
	 *                        selected.
	 */
	public DLabelSelection(View<CyNode> selectedNode, Rectangle labelRectangle, ObjectPosition oldPosition, Double oldRotation, boolean nodeWasSelected) {
		this.node = selectedNode;
		this.rectangle = labelRectangle;
		this.originalRectX = (int) rectangle.getX();
		this.originalRectY = (int) rectangle.getY();
		if (oldPosition != null)
			this.previousValue = oldPosition;
		else
			this.previousValue = selectedNode.getVisualProperty(NODE_LABEL_POSITION);

		this.previousRotation = oldRotation;
		if (oldRotation != null)
			this.currentRotation = Math.toRadians(oldRotation);
		this.nodeWasSelected = nodeWasSelected;
	}

	public View<CyNode> getNode() {
		return node;
	}

	// TODO: Adjust rectangle based on rotation
	public Shape getRectangle() {
		Point2D anchor = getAnchorPosition();
		AffineTransform transform = AffineTransform.getRotateInstance(currentRotation, anchor.getX(), anchor.getY());
		return transform.createTransformedShape(rectangle);
	}

	public ObjectPosition getPreviousPosition() {
		return previousValue;
	}

	public Double getPreviousRotation() {
		return previousRotation;
	}

	public double getCurrentRotation() {
		return currentRotation;
	}

	public boolean getNodeWasSelected() {
		return nodeWasSelected;
	}

	// move the label rectangle to a new position with the offsets of the original
	// position.
	public void moveRectangle(int offsetX, int offsetY) {
		rectangle.setLocation(originalRectX + offsetX, originalRectY + offsetY);
	}

  // Adjust the angle when we know the delta
	public double adjustAngle(double angle) {
		if (previousRotation != null)
			currentRotation = previousRotation - angle;
		else
			currentRotation = Math.toRadians(0d) - angle;

    return angle;
  }

	// move the label rectangle to a new position with the offsets of the original
	// position.
	public double adjustAngle(double x1, double y1, int x2, int y2) {
		// First line
		// double centerX = rectangle.getX()+rectangle.getWidth()/2;
		// double centerY = rectangle.getY()+rectangle.getHeight()/2;
		Point2D anchor = getAnchorPosition();
		double angle1 = Math.atan2(anchor.getY() - y1, anchor.getX() - x1);
		// System.out.println("currentRotation = "+Math.toDegrees(currentRotation));
		// System.out.println("angle1 = "+Math.toDegrees(angle1));
		double angle2 = Math.atan2(anchor.getY() - (double) y2, anchor.getX() - (double) x2);
		// System.out.println("angle2 = "+Math.toDegrees(angle2));
    return adjustAngle(angle1-angle2);
	}

  @Override
  public boolean equals(Object sel) {
    if (sel instanceof DLabelSelection) {
      if (((DLabelSelection)sel).getNode().equals(node))
        return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return node.hashCode();
  }

  public String toString() {
    return node.toString();
  }

  public static Set<DLabelSelection> addLabelSelection(Set<DLabelSelection> selectedLabels, DLabelSelection newSelection) {
    if (newSelection == null) return selectedLabels;
    if (selectedLabels == null) selectedLabels = new HashSet<>();
    selectedLabels.add(newSelection);
    return selectedLabels;
  }

  public static DLabelSelection get(Set<DLabelSelection> selectedLabels, DLabelSelection label) {
    for (DLabelSelection lbl: selectedLabels) {
      if (lbl.equals(label))
        return lbl;
    }
    return null;
  }

	private Point2D getAnchorPosition() {
		// Get the center
		double centerX = rectangle.getX() + rectangle.getWidth() / 2;
		double centerY = rectangle.getY() + rectangle.getHeight() / 2;
		double x2 = rectangle.getX() + rectangle.getWidth();
		double y2 = rectangle.getY() + rectangle.getHeight();

		if (previousValue == null)
			return new Point2D.Double(centerX, centerY);

		Position anchor = previousValue.getAnchor();

		switch (anchor) {
		case CENTER:
			return new Point2D.Double(centerX, centerY);
		case EAST:
			return new Point2D.Double(x2, centerY);
		case NONE:
			return new Point2D.Double(centerX, centerY);
		case NORTH:
			return new Point2D.Double(centerX, y2);
		case NORTH_EAST:
			return new Point2D.Double(x2, y2);
		case NORTH_WEST:
			return new Point2D.Double(rectangle.getX(), y2);
		case SOUTH:
			return new Point2D.Double(centerX, rectangle.getY());
		case SOUTH_EAST:
			return new Point2D.Double(x2, rectangle.getY());
		case SOUTH_WEST:
			return new Point2D.Double(rectangle.getX(), rectangle.getY());
		case WEST:
			return new Point2D.Double(rectangle.getX(), centerY);
		}
		return new Point2D.Double(centerX, centerY);
	}
}
