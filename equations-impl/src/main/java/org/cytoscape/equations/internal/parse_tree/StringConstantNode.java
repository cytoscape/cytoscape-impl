package org.cytoscape.equations.internal.parse_tree;

/*
 * #%L
 * Cytoscape Equations Impl (equations-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


import java.util.Stack;

import org.cytoscape.equations.CodeAndSourceLocation;
import org.cytoscape.equations.AbstractNode;
import org.cytoscape.equations.TreeNode;


/**
 *  A node in the parse tree representing an integer constant.
 */
public class StringConstantNode extends AbstractNode {
	private final String value;

	public StringConstantNode(final int sourceLocation, final String value) {
		super(sourceLocation);

		this.value = value;
	}

	public String toString() { return "StringConstantNode: " + value; }

	public Class getType() { return String.class; }

	/**
	 *  @return null, This type of node never has any children!
	 */
	public TreeNode getLeftChild() { return null; }

	/**
	 *  @return null, This type of node never has any children!
	 */
	public TreeNode getRightChild() { return null; }

	public String getValue() { return value; }

	public void genCode(final Stack<CodeAndSourceLocation> codeStack) {
		codeStack.push(new CodeAndSourceLocation(value, getSourceLocation()));
	}
}
