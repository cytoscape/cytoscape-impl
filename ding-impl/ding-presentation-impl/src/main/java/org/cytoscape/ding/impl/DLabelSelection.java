package org.cytoscape.ding.impl;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ObjectPosition;


/**
 * A wrapper to store the node and label information when a label is selected by a mouse press event.
 * 
 * @author jingchen
 *
 */
public class DLabelSelection {
	
	private View<CyNode> node;  // node that associated with selected label.
	private Rectangle rectangle;  // bound of the label
	
	//Original upper left corner of the Label rectangle
	private int originalRectX;
	private int originalRectY;
	
	private ObjectPosition previousValue;
  private Double previousRotation;
  private double currentRotation = Math.toRadians(0d);
	
	private boolean nodeWasSelected;
	
	/**
	 * 
	 * @param selectedNode  node view that associates with the selected label 
	 * @param labelRectangle  bound of the label in the screen.
	 * @param oldPosition   the previous value of the LabelPosistion property. null if it was not set before. 
	 * @param oldRotation   the previous value of the LabelRotation property in degrees. null if it was not set before. 
	 * @param nodeWasSelected specify if the node was selected when the label was selected.
	 */
	public DLabelSelection (View<CyNode> selectedNode, Rectangle labelRectangle ,ObjectPosition oldPosition, Double oldRotation, boolean nodeWasSelected) {
		this.node = selectedNode;
		this.rectangle = labelRectangle;
		this.originalRectX = (int)rectangle.getX();
		this.originalRectY = (int)rectangle.getY();
		this.previousValue = oldPosition;
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
    double centerX = rectangle.getX()+rectangle.getWidth()/2;
    double centerY = rectangle.getY()+rectangle.getHeight()/2;
    AffineTransform transform = AffineTransform.getRotateInstance(currentRotation, centerX, centerY);
		return transform.createTransformedShape(rectangle);
	}

	public ObjectPosition getPreviousPosition() {return previousValue;}
	public Double getPreviousRotation() {return previousRotation;}
	public double getCurrentRotation() {return currentRotation;}
	public boolean getNodeWasSelected () { return nodeWasSelected;}
	
	// move the label rectangle to a new position with the offsets of the original position.
	public void moveRectangle ( int offsetX, int offsetY) {
		rectangle.setLocation(originalRectX + offsetX, originalRectY + offsetY);
	}

	// move the label rectangle to a new position with the offsets of the original position.
	public void adjustAngle ( double x1, double y1, int x2, int y2) {
    // First line
    double centerX = rectangle.getX()+rectangle.getWidth()/2;
    double centerY = rectangle.getY()+rectangle.getHeight()/2;
    double angle1 = Math.atan2(centerY - y1, centerX - x1);
    // System.out.println("currentRotation = "+Math.toDegrees(currentRotation));
    // System.out.println("angle1 = "+Math.toDegrees(angle1));
    double angle2 = Math.atan2(centerY - (double)y2, centerX - (double)x2);
    // System.out.println("angle2 = "+Math.toDegrees(angle2));
    if (previousRotation != null)
      currentRotation = previousRotation-(angle1-angle2);
    else
      currentRotation = Math.toRadians(0d)-(angle1-angle2);
    // System.out.println("new angle = "+Math.toDegrees(currentRotation));
	}
	
}
