/*
 File: ManagerModel.java 
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.app.internal.ui;

import javax.swing.event.TreeModelListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fairly similar to DefaultTreeModel but I wanted to add some extra
 * functionality to removing/adding nodes and use my own node objects
 */
public class ManagerModel implements TreeModel {
	private static final Logger logger = LoggerFactory.getLogger(ManagerModel.class);
	private TreeNode root;

	protected EventListenerList listenerList = new EventListenerList();

	public ManagerModel(TreeNode Root) {
		root = Root;
	}

	
	public void removeNodesFromParent(java.util.List<TreeNode> children) {
		for (TreeNode child: children) 
			recursiveRemoveNodeFromParent(child);
	}
	
	/**
	 * Removes given node from it's parent.
	 * 
	 * @param child
	 */
	public void removeNodeFromParent(TreeNode child) {
		recursiveRemoveNodeFromParent(child);
	}

	// not recursive anymore
	private void recursiveRemoveNodeFromParent(TreeNode child) {
		TreeNode Parent = child.getParent();
		TreeNode[] Children = new TreeNode[] { child };
		int[] ChildIndicies = new int[] { getIndexOfChild(Parent, child) };
		Parent.removeChild(child);
		nodesRemoved(Parent, ChildIndicies, Children);

		// parent is empty, remove it from it's parent
//		if (Parent.getChildCount() == 0 && !Parent.equals(root)) {
//			recursiveRemoveNodeFromParent(Parent);
//		}
	}

	/**
	 * Adds node to given parent, fires event
	 * 
	 * @param parent
	 * @param child
	 */
	public void addNodeToParent(TreeNode parent, TreeNode child) {
		parent.addChild(child);
		TreeNode[] Children = new TreeNode[] { child };
		int[] ChildIndicies = new int[] { getIndexOfChild(parent, child) };
		nodesAdded(parent, ChildIndicies, Children);
	}

	/**
	 * Fires necessary event, use addNodeToParent
	 * 
	 * @param parent
	 * @param childIndicies
	 * @param addedChildren
	 */
	protected void nodesAdded(TreeNode parent, int[] childIndicies,
			TreeNode[] addedChildren) {
		fireTreeNodesAdded(parent, getPathToRoot(parent), childIndicies,
				addedChildren);
	}

	/**
	 * Fires necessary event, use removeNodeFromParent
	 * 
	 * @param parent
	 * @param childIndicies
	 * @param removedChildren
	 */
	protected void nodesRemoved(TreeNode parent, int[] childIndicies,
			TreeNode[] removedChildren) {
		fireTreeNodesRemoved(parent, getPathToRoot(parent), childIndicies,
				removedChildren);
	}

	/**
	 * Gets child TreeNode from given parent at given index.
	 * 
	 * @param parent
	 * @param index
	 */
	public Object getChild(Object parent, int index) {
		TreeNode treeNode = (TreeNode) parent;
		return treeNode.getChildAt(index);
	}

	/**
	 * Gets count of all children under given parent (not including leaves)
	 * 
	 * @param parent
	 */
	public int getChildCount(Object parent) {
		TreeNode treeNode = (TreeNode) parent;
		return treeNode.getChildCount();
	}

	/**
	 * Gets the index of the given child node from the given parent node.
	 * 
	 * @param parent
	 * @param child
	 */
	public int getIndexOfChild(Object parent, Object child) {
		TreeNode treeNode = (TreeNode) parent;
		return treeNode.getIndexOfChild((TreeNode) child);
	}

	/**
	 * Gets root TreeNode
	 * 
	 * @return root
	 */
	public Object getRoot() {
		return root;
	}

	/**
	 * Checks if given node is a leaf. True if it either has no children or is
	 * {@link TreeNode#isLeaf()} return true;
	 * 
	 * @param node
	 */
	public boolean isLeaf(Object node) {
		TreeNode treeNode = (TreeNode) node;
		return (treeNode.getChildCount() == 0 || treeNode.isLeaf());
	}

	/**
	 * Removes given listener
	 * 
	 * @param l
	 */
	public void removeTreeModelListener(TreeModelListener l) {
		listenerList.remove(TreeModelListener.class, l);
	}

	/**
	 * Adds given listener
	 * 
	 * @param l
	 */
	public void addTreeModelListener(TreeModelListener l) {
		listenerList.add(TreeModelListener.class, l);
	}

	/**
	 * 
	 * @return TreeModelListener[]
	 */
	public TreeModelListener[] getTreeModelListeners() {
		return (TreeModelListener[]) listenerList
				.getListeners(TreeModelListener.class);
	}

	protected void fireTreeNodesRemoved(Object source, Object[] path,
			int[] childIndicies, Object[] children) {
		TreeModelEvent Event = new TreeModelEvent(source, path, childIndicies,
				children);
		TreeModelListener[] listeners = getTreeModelListeners();
		for (int i = listeners.length - 1; i >= 0; i--) {
			listeners[i].treeNodesRemoved(Event);
		}
	}

	protected void fireTreeNodesAdded(Object source, Object[] path,
			int[] childIndicies, Object[] children) {
		TreeModelEvent Event = new TreeModelEvent(source, path, childIndicies,
				children);
		TreeModelListener[] listeners = getTreeModelListeners();
		for (int i = listeners.length - 1; i >= 0; i--) {
			listeners[i].treeNodesInserted(Event);
		}
	}

	// this will reset entire tree
	protected void fireTreeStructureChanged(Object source, Object[] path,
			int[] childIndicies, Object[] children) {
		TreeModelEvent Event = new TreeModelEvent(source, path, childIndicies,
				children);
		TreeModelListener[] listeners = getTreeModelListeners();
		for (int i = listeners.length - 1; i >= 0; i--) {
			listeners[i].treeStructureChanged(Event);
		}
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	public TreeNode[] getPathToRoot(TreeNode node) {
		return getPathToRoot(node, 0);
	}

	// copied from DefaultTreeModel
	protected TreeNode[] getPathToRoot(TreeNode node, int depth) {
		if (node == null) {
			if (depth == 0) {
				return null;
			}
			return new TreeNode[depth];
		}

		TreeNode[] path = getPathToRoot(node.getParent(), depth + 1);
		path[path.length - depth - 1] = node;
		return path;
	}

	/**
	 * Reloads entire tree, fires event
	 */
	public void reload() {
		int n = getChildCount(root);
		int[] ChildIndex = new int[n];
		Object[] Children = new Object[n];

		for (int i = 0; i < n; i++) {
			ChildIndex[i] = i;
			Children[i] = getChild(root, i);
		}
		fireTreeStructureChanged(this, new Object[] { root }, ChildIndex,
				Children);
	}

	/**
	 * Reloads given node, fires event.
	 * 
	 * @param node
	 */
	public void reload(TreeNode node) {
		int n = getChildCount(node);
		int[] ChildIndex = new int[n];
		Object[] Children = new Object[n];

		for (int i = 0; i < n; i++) {
			ChildIndex[i] = i;
			Children[i] = getChild(node, i);
		}
		fireTreeStructureChanged(this, new Object[] { node }, ChildIndex,
				Children);
	}

	/**
	 * NOT IMPLEMENTED
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		logger.warn("valueForPathChanged NOT IMPLEMENTED");
	}

}
