package org.cytoscape.ding.impl;

import java.awt.Shape;
import java.util.Objects;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;


public class LabelSelection {

	private final View<CyNode> node; // node that associated with selected label.
	private final Shape shape;

	
	public LabelSelection(View<CyNode> node, Shape boundingShape) {
		this.node = node;
		this.shape = boundingShape; // may be rotated
	}

	
	public View<CyNode> getNode() {
		return node;
	}
	
	public Shape getShape() {
		return shape;
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
