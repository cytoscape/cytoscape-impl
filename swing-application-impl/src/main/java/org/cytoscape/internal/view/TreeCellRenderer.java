package org.cytoscape.internal.view;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableEntry;

final class TreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * 
     */
    private final NetworkPanel networkPanel;

    /**
     * @param networkPanel
     */
    TreeCellRenderer(NetworkPanel networkPanel) {
	this.networkPanel = networkPanel;
    }

    private final static long serialVersionUID = 1213748836751014L;

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
	    boolean leaf, int row, boolean hasFocus) {
	super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

	setFont(NetworkPanel.TABLE_FONT);
	this.setForeground(NetworkPanel.FONT_COLOR);
	if (hasView(value)) {
	    setBackgroundNonSelectionColor(NetworkPanel.WITH_VIEW);
	    setBackgroundSelectionColor(NetworkPanel.WITH_VIEW_SELECTED);
	} else {
	    setBackgroundNonSelectionColor(NetworkPanel.WITHOUT_VIEW);
	    setBackgroundSelectionColor(NetworkPanel.WITHOUT_VIEW_SELECTED);
	}

	return this;
    }

    private boolean hasView(final Object value) {
	final NetworkTreeNode node = (NetworkTreeNode) value;
	final CyNetwork network = this.networkPanel.netmgr.getNetwork(node.getNetworkID());

	if (network != null)
	    setToolTipText(network.getCyRow().get(CyTableEntry.NAME, String.class));
	else
	    setToolTipText("Root");

	return this.networkPanel.networkViewManager.viewExists(node.getNetworkID());
    }
}