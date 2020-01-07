package org.cytoscape.ding.impl;

import java.awt.Rectangle;

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
	
	private boolean nodeWasSelected;
	
	/**
	 * 
	 * @param selectedNode  node view that associates with the selected label 
	 * @param labelRectangle  bound of the label in the screen.
	 * @param oldPosition   the previous value of the LabelPosistion property. null if it was not set before. 
	 * @param nodeWasSelected specify if the node was selected when the label was selected.
	 */
	public DLabelSelection (View<CyNode> selectedNode, Rectangle labelRectangle ,ObjectPosition oldPosition, boolean nodeWasSelected) {
		this.node = selectedNode;
		this.rectangle = labelRectangle;
		this.originalRectX = (int)rectangle.getX();
		this.originalRectY = (int)rectangle.getY();
		this.previousValue = oldPosition;
		this.nodeWasSelected = nodeWasSelected;
	}

	public View<CyNode> getNode() {
		return node;
	}

	public Rectangle getRectangle() {
		return rectangle;
	}

	public ObjectPosition getPreviousPosition() {return previousValue;}
	public boolean getNodeWasSelected () { return nodeWasSelected;}
	
	// move the label rectangle to a new position with the offsets of the original position.
	public void moveRectangle ( int offsetX, int offsetY) {
		rectangle.setLocation(originalRectX + offsetX, originalRectY + offsetY);
	}
	
}
