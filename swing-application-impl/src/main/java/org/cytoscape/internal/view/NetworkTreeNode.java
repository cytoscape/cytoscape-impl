package org.cytoscape.internal.view;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Custom Tree node in the JTreeTable
 * 
 */
final class NetworkTreeNode extends DefaultMutableTreeNode {

	private final static long serialVersionUID = 1213748836736485L;

	// Immutable network SUID. This can be null if the given tree node is not
	// associated with a network.
	private final Long networkID;

	
	NetworkTreeNode(final Object userobj, final Long id) {
		super(userobj.toString());
		networkID = id;
	}

	Long getNetworkID() {
		return networkID;
	}
}