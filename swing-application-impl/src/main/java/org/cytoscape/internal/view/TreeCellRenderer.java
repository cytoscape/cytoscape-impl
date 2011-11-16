package org.cytoscape.internal.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.util.swing.JTreeTable;
import org.cytoscape.view.model.CyNetworkViewManager;

final class TreeCellRenderer extends DefaultTreeCellRenderer {

	private final static long serialVersionUID = 1213748836751014L;
	
	private static final String NETWORK_ICON = "/images/network_32.png";
	private static final String NETWORK_LEAF_ICON = "/images/network_l_32.png";

	static final Font TABLE_FONT = new Font("SansSerif", Font.PLAIN, 14);
	static final Font TABLE_FONT_SELECTED = new Font("SansSerif", Font.BOLD, 14);

	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;

	private final JTreeTable treeTable;

	TreeCellRenderer(final JTreeTable treeTable, final CyNetworkManager networkManager,
			final CyNetworkViewManager networkViewManager) {
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.treeTable = treeTable;
		
		final Image iconImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource(NETWORK_ICON));
		final ImageIcon icon = new ImageIcon(iconImage);
		final Image iconImageLeaf = Toolkit.getDefaultToolkit().getImage(getClass().getResource(NETWORK_LEAF_ICON));
		final ImageIcon iconLeaf = new ImageIcon(iconImageLeaf);
		
		this.setClosedIcon(icon);
		this.setOpenIcon(icon);
		this.setLeafIcon(iconLeaf);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof NetworkTreeNode == false)
			return this;

		final NetworkTreeNode treeNode = (NetworkTreeNode) value;

		this.setForeground(NetworkPanel.FONT_COLOR);
		treeTable.setForeground(NetworkPanel.FONT_COLOR);
		this.setBackground(treeTable.getBackground());
		this.setBackgroundSelectionColor(treeTable.getSelectionBackground());

		if (selected)
			this.setFont(TABLE_FONT_SELECTED);
		else
			this.setFont(TABLE_FONT);

		if(treeNode.getNetworkID() == null) {
			setForeground(treeTable.getForeground());
			return this;
		}
		
		if (hasView(treeNode))
			setForeground(treeTable.getForeground());
		else
			setForeground(Color.red);

		return this;
	}

	private boolean hasView(final NetworkTreeNode node) {
		final Long networkID = node.getNetworkID();
		final CyNetwork network = networkManager.getNetwork(networkID);

		if (network != null)
			setToolTipText(network.getCyRow().get(CyTableEntry.NAME, String.class));
		else {
			if (node.getRoot() == node)
				setToolTipText("Network Root");
		}

		return networkViewManager.viewExists(node.getNetworkID());
	}
}