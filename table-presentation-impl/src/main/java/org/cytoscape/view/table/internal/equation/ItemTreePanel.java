package org.cytoscape.view.table.internal.equation;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class ItemTreePanel<T> extends ItemPanel {

	private JTree tree;
	
	public ItemTreePanel(String title) {
		super(title);
	}
	
	public void clearSelection() {
		tree.getSelectionModel().clearSelection();
	}
	
	@SuppressWarnings("unchecked")
	public T getSelectedValue() {
		var node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if(node == null)
			return null;
		return (T) node.getUserObject();
	}
	
	public JTree getTree() {
		if(tree == null) {
			tree = new JTree();
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.setRootVisible(false); 
			tree.setShowsRootHandles(true);
		}
		return tree;
	}

	@Override
	public JTree getContent() {
		return getTree();
	}
}
