/*
 File: TreeNode.java 
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

import org.cytoscape.app.internal.DownloadableInfo;

import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class TreeNode extends DefaultMutableTreeNode {
	private Vector<TreeNode> children;

	private DownloadableInfo infoObj;

	private String title;

	private TreeNode parent;

	private boolean childAllowed;
	
	private boolean visible;

	/**
	 * Creates a TreeNode with given title, no parent and allows children.
	 * 
	 * @param Title
	 */
	public TreeNode(String Title) {
		init(Title, true);
	}

	/**
	 * Creates a TreeNode with given title, no parent and allows children if
	 * specified.
	 * 
	 * @param Title
	 * @param allowsChildren
	 */
	public TreeNode(String Title, boolean allowsChildren) {
		init(Title, allowsChildren);
	}

	/**
	 * Creates a TreeNode with given AppInfo object, no parent and does not
	 * allow children.
	 * 
	 * @param obj
	 */
	public TreeNode(DownloadableInfo obj) {
		init(obj.getName(), false);
		addObject(obj);
	}

	/**
	 * Creates a TreeNode with given AppInfo object, no parent and allows
	 * children if specified.
	 * 
	 * @param obj
	 * @param allowsChildren
	 */
	public TreeNode(DownloadableInfo obj, boolean allowsChildren) {
		init(obj.getName(), allowsChildren);
		addObject(obj);
	}

	private void init(String Title, boolean childrenOk) {
		children = new Vector<TreeNode>();
		this.title = Title;
		childAllowed = childrenOk;
	}

	public void setVisible(boolean vis) {
		visible = vis;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	/**
	 * Returns true if this node has no children or does not allow them.
	 * 
	 * @return
	 */
	public boolean isLeaf() {
		if (childAllowed)
			return false;
		else
			return true;
	}

	/**
	 * Returns true if otherNode is an ancestor of this node.
	 * 
	 * @param otherNode
	 * @return
	 */
	public boolean isNodeAncestor(TreeNode otherNode) {
		return recursiveParentLookup(this, otherNode);
	}

	/**
	 * Sets this nodes parent to newParent, child list does not change.
	 * 
	 * @param newParent
	 */
	public void setParent(TreeNode newParent) {
		parent = newParent;
	}

	/**
	 * Gets the parent of this node
	 * 
	 * @return TreeNode
	 */
	public TreeNode getParent() {
		return parent;
	}

	/**
	 * Adds a AppInfo object to this node.
	 * 
	 * @param info
	 */
	public void addObject(DownloadableInfo info) {
		infoObj = info;
	}

	/**
	 * Adds newChild to this node if children are allowed.
	 * 
	 * @param newChild
	 */
	public void addChild(TreeNode newChild) {
		if (!childAllowed)
			throw new IllegalStateException();
		if (newChild == null)
			throw new IllegalArgumentException();
		if (isNodeAncestor(newChild))
			throw new IllegalArgumentException("Cannot add ancestor node.");
		children.add(newChild);
		newChild.setParent(this);
	}

	/**
	 * Removes child from this node.
	 * 
	 * @param child
	 */
	public void removeChild(TreeNode child) {
		children.remove(child);
	}

	/**
	 * Clears the list of children for this node.
	 */
	public void removeChildren() {
		children.clear();
	}

	/**
	 * Adds all children to the child list of this node if children are allowed.
	 * 
	 * @param children
	 */
	public void addChildren(TreeNode[] children) {
		for (TreeNode c : children) {
			if (c.isVisible())
				addChild(c);
		}
	}

	/**
	 * Gets the list of children for this node
	 * 
	 * @return Vector<TreeNode>
	 */
	public Vector<TreeNode> getChildren() {
		return children;
	}

	/**
	 * Get total number of children for this node
	 * 
	 * @return int
	 */
	public int getChildCount() {
		return children.size();
	}

	/**
	 * Get child from this node's child list at given index.
	 * 
	 * @param index
	 * @return TreeNode
	 */
	public TreeNode getChildAt(int index) {
		return children.get(index);
	}

	/**
	 * Gets the index of child from this node's child list.
	 * 
	 * @param child
	 * @return int
	 */
	public int getIndexOfChild(TreeNode child) {
		return children.indexOf(child);
	}

	/**
	 * Gets the total (recursively) of all child nodes that are not leaves.
	 * 
	 * @return int
	 */
	public int getTreeCount() {
		return recursiveTotalSubCategorySize(this);
	}

	/**
	 * Gets the total (recursively) of all leaves under this node.
	 * 
	 * @return
	 */
	public int getLeafCount() {
		if (this.getChildCount() <= 0) 
			return 0;
		return recursiveTotalLeafSize(this);
	}

	/**
	 * Gets the title of this node.
	 * 
	 * @return String
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Gets the AppInfo object of this node.
	 * 
	 * @return AppInfo
	 */
	public DownloadableInfo getObject() {
		return this.infoObj;
	}

	/**
	 * Gets string representation of this node as 'title: total leaves'
	 */
	public String toString() {
		if (this.infoObj != null)
			return this.infoObj.toString();
		else
			return getTitle() + ": " + getLeafCount();
	}

	
//	public TreePath getPath(TreeNode node) {
//		return node.getPath(node);
//	}
	
//	 public javax.swing.tree.TreePath getPath(TreeNode node) {
//		    // Get node depth
//		    int depth = 0;
//		    for(TreeNode node = current; node != null; node.getParent())
//		        depth++;
//		    // Construct node path 
//		    // First scan helped us, now we can directly allocate array of exact
//		    // size => no extra objects created (be kind to your local gc, it has 
//		    // hard job cleaning that mess already), no collection reverse.
//		    // Price is doubling path construction time.
//		    // But in many situations you know the depth already. In such case
//		    // Only code below applies and time complexity is O(depth) not 
//		    // O(2*depth)
//		    TreeNode[] path = new TreeNode[depth];
//		    for(TreeNode node = current; node != null; node.getParent())
//		        path[--depth] = node; // reverse fill array
//		    return new TreePath(path);
	
	
	private int recursiveTotalSubCategorySize(TreeNode node) {
		int n = 0;
		n += node.getChildCount();
		for (TreeNode c : node.getChildren()) {
			n += recursiveTotalSubCategorySize(c);
		}
		return n;
	}

	private int recursiveTotalLeafSize(TreeNode node) {
		int n = 0;
		if (node.getChildCount() == 0) {
			n = 1;
		}
		for (TreeNode c : node.getChildren()) {
			n += recursiveTotalLeafSize(c);
		}
		return n;
	}

	private boolean recursiveParentLookup(TreeNode node, TreeNode ancestor) {
		boolean lookup = false;
		if (node.getParent() != null) {
			if (node.getParent().equals(ancestor))
				lookup = true;
			else
				lookup = recursiveParentLookup(node.getParent(), ancestor);
		}
		return lookup;
	}
}
