package org.cytoscape.internal.view;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.util.swing.JTreeTable;

final class TreeCellRenderer extends DefaultTreeCellRenderer {

	private final static long serialVersionUID = 1213748836751014L;
	
	private static final String NETWORK_ICON = "/images/network_16.png";
	private static final String NETWORK_LEAF_ICON = "/images/blank_icon_16.png";

	private static final Font TABLE_FONT = new Font("SansSerif", Font.PLAIN, 12);
	private static final Font TABLE_FONT_SELECTED = new Font("SansSerif", Font.BOLD, 12);
	private static final Font TABLE_FONT_ROOT = new Font("SansSerif", Font.ITALIC, 12);
	
	private static final Dimension CELL_SIZE = new Dimension(1200, 40);

	private final JTreeTable treeTable;

	TreeCellRenderer(final JTreeTable treeTable) {
		this.treeTable = treeTable;
		
		final Image iconImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource(NETWORK_ICON));
		final ImageIcon icon = new ImageIcon(iconImage);

		// If we don't provide a leaf Icon, a default one will be used.
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

		this.setPreferredSize(CELL_SIZE);
		this.setSize(CELL_SIZE);
		
		if (value instanceof NetworkTreeNode == false)
			return this;

		final NetworkTreeNode treeNode = (NetworkTreeNode) value;

		this.setForeground(NetworkPanel.FONT_COLOR);
		treeTable.setForeground(NetworkPanel.FONT_COLOR);
		this.setBackground(treeTable.getBackground());
		this.setBackgroundSelectionColor(treeTable.getSelectionBackground());

		if (selected) {
			this.setFont(TABLE_FONT_SELECTED);
			this.setBackgroundSelectionColor( new Color(0, 100, 255, 40));		
		} else {
			this.setFont(TABLE_FONT);
			this.setBackgroundNonSelectionColor(Color.white); 
		}

		if(treeNode.getNetwork() == null || (treeNode.getNetwork() instanceof CyRootNetwork) ) {
			setForeground(treeTable.getForeground());
			this.setFont(TABLE_FONT_ROOT);
			return this;
		}
			
		
		setForeground(treeNode.getNodeColor());
		try {
			setToolTipText(treeNode.getNetwork().getRow(treeNode.getNetwork()).get(CyNetwork.NAME, String.class));
		} catch (NullPointerException e) {
			// It's possible that the network got deleted but we haven't been
			// notified yet.
		}

		return this;
	}
}
