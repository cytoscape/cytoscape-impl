package org.cytoscape.internal.view;


import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.AbstractTreeTableModel;
import org.cytoscape.util.swing.TreeTableModel;


final class NetworkTreeTableModel extends AbstractTreeTableModel {
	
	private static final String[] COLUMNS = { "Network", "Nodes", "Edges" };
	private static final Class<?>[] COLUMN_CLASSES = { TreeTableModel.class, String.class, String.class };

	private final NetworkPanel networkPanel;
	
	NetworkTreeTableModel(NetworkPanel networkPanel, Object root) {
		super(root);
		this.networkPanel = networkPanel;
	}

	public Object getChild(Object parent, int index) {
		Enumeration<?> tree_node_enum = ((DefaultMutableTreeNode) getRoot()).breadthFirstEnumeration();

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
		
		if(network == null) {
			// This is root network node
			return null;
		}
		
		if (column == 0)
			return node.getUserObject();
		else if (column == 1) {
			final CyNetwork cyNetwork = this.networkPanel.netmgr.getNetwork(node.getNetwork().getSUID());
			if(cyNetwork == null)
				return null;
			
			return "" + cyNetwork.getNodeCount() + "("
				+ cyNetwork.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true).size() + ")";
		} else if (column == 2) {
			final CyNetwork cyNetwork = this.networkPanel.netmgr.getNetwork(((NetworkTreeNode) node).getNetwork().getSUID());
			if(cyNetwork == null)
				return null;
			
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