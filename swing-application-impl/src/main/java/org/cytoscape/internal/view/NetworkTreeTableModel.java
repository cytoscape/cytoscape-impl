package org.cytoscape.internal.view;


import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.AbstractTreeTableModel;
import org.cytoscape.util.swing.TreeTableModel;


final class NetworkTreeTableModel extends AbstractTreeTableModel {
	private final NetworkPanel networkPanel;
	String[] columns = { "Network", "Nodes", "Edges" };
	Class[] columns_class = { TreeTableModel.class, String.class, String.class };

	public NetworkTreeTableModel(NetworkPanel networkPanel, Object root) {
		super(root);
		this.networkPanel = networkPanel;
	}

	public Object getChild(Object parent, int index) {
		Enumeration tree_node_enum = ((DefaultMutableTreeNode) getRoot()).breadthFirstEnumeration();

		while (tree_node_enum.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_node_enum.nextElement();

			if (node == parent)
				return node.getChildAt(index);
		}

		return null;
	}

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

	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int column) {
		return columns[column];
	}

	public Class getColumnClass(int column) {
		return columns_class[column];
	}

	public Object getValueAt(final Object node, final int column) {
		if (column == 0)
			return ((DefaultMutableTreeNode) node).getUserObject();
		else if (column == 1) {
			CyNetwork cyNetwork = this.networkPanel.netmgr.getNetwork(((NetworkTreeNode) node).getNetworkID());

			return "" + cyNetwork.getNodeCount() + "("
				+ cyNetwork.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true).size() + ")";
		} else if (column == 2) {
			CyNetwork cyNetwork = this.networkPanel.netmgr.getNetwork(((NetworkTreeNode) node).getNetworkID());

			return "" + cyNetwork.getEdgeCount() + "("
				+ cyNetwork.getDefaultEdgeTable().getMatchingRows(CyNetwork.SELECTED, true).size() + ")";
		}

		return "";
	}

	public void setValueAt(Object aValue, Object node, int column) {
		if (column == 0) {
			((DefaultMutableTreeNode) node).setUserObject(aValue);
		} else
			JOptionPane.showMessageDialog(this.networkPanel, "Error: assigning value at in NetworkPanel");
		// This function is not used to set node and edge values.
	}
}