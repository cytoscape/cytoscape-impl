package org.cytoscape.internal.view;

import java.awt.Color;

import javax.swing.tree.DefaultMutableTreeNode;

import org.cytoscape.model.CyNetwork;

/**
 * Custom Tree node in the JTreeTable
 * 
 */
final class NetworkTreeNode extends DefaultMutableTreeNode {

	private final static long serialVersionUID = 1213748836736485L;

	private final CyNetwork network;
	
	private Color nodeColor = Color.red;

	
	NetworkTreeNode(final Object userobj, final CyNetwork network) {
		super(userobj.toString());
		this.network = network;
	}

	CyNetwork getNetwork() {
		return network;
	}
	
	/*
	 * These are necessary to avoid deadlock in the renderer.
	 */

	Color getNodeColor() {
		return nodeColor;
	}
	
	void setNodeColor(final Color newColor) {
		if(newColor != null)
			this.nodeColor = newColor;
	}
}