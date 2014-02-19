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


import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.AbstractTreeTableModel;
import org.cytoscape.util.swing.TreeTableModel;
import org.cytoscape.view.model.CyNetworkViewManager;


final class NetworkTreeTableModel extends AbstractTreeTableModel {
	
	private static final String[] COLUMNS = { "Network", "Views", "Nodes", "Edges" };
	private static final Class<?>[] COLUMN_CLASSES = { TreeTableModel.class, String.class, String.class, String.class };

	private final NetworkPanel networkPanel;
	private final CyNetworkViewManager netViewMgr;
	
	NetworkTreeTableModel(NetworkPanel networkPanel, Object root, CyNetworkViewManager netViewMgr) {
		super(root);
		this.networkPanel = networkPanel;
		this.netViewMgr = netViewMgr;
	}

	@Override
	public Object getChild(Object parent, int index) {
		Enumeration<?> tree_node_enum = ((DefaultMutableTreeNode) getRoot()).breadthFirstEnumeration();

		while (tree_node_enum.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_node_enum.nextElement();

			if (node == parent)
				return node.getChildAt(index);
		}

		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		Enumeration tree_node_enum = ((DefaultMutableTreeNode) getRoot()).breadthFirstEnumeration();

		while (tree_node_enum.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_node_enum.nextElement();

			if (node == parent) {
				return node.getChildCount();
			}
		}

		return 0;
	}

	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	@Override
	public String getColumnName(final int columnIdx) {
		return COLUMNS[columnIdx];
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return COLUMN_CLASSES[column];
	}

	@Override
	public Object getValueAt(final Object value, final int column) {
		if(value instanceof NetworkTreeNode == false)
			return null;
		
		final NetworkTreeNode node = (NetworkTreeNode) value;
		final CyNetwork network = node.getNetwork();
		
		if (network == null) {
			// This is root network node
			return null;
		}
		
		if (column == 0) {
			return node.getUserObject();
		} else {
			final CyNetwork net = this.networkPanel.netMgr.getNetwork(node.getNetwork().getSUID());
			
			if (net == null)
				return null;
			
			if (column == 1) {
				return "" + netViewMgr.getNetworkViews(net).size();
			} else if (column == 2) {
				return "" + net.getNodeCount() + "("
				+ net.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true).size() + ")";
			} else if (column == 3) {
				return "" + net.getEdgeCount() + "("
					+ net.getDefaultEdgeTable().getMatchingRows(CyNetwork.SELECTED, true).size() + ")";
			}
		}

		return "";
	}

	@Override
	public void setValueAt(Object aValue, Object node, int column) {
		if (column == 0) {
			((DefaultMutableTreeNode) node).setUserObject(aValue);
		} else
			JOptionPane.showMessageDialog(this.networkPanel, "Error: assigning value at in NetworkPanel");
		// This function is not used to set node and edge values.
	}
}