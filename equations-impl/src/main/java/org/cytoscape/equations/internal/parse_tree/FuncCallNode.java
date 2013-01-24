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
import org.cytoscape.equations.Function;
import org.cytoscape.equations.AbstractNode;
import org.cytoscape.equations.TreeNode;
import org.cytoscape.equations.internal.interpreter.Instruction;


/**
 *  A node in the parse tree representing a function call.
 */
public class FuncCallNode extends AbstractNode {
	private final Function func;
	final Class returnType;
	private final TreeNode[] args;

	public FuncCallNode(final int sourceLocation, final Function func, final Class returnType, final TreeNode[] args) {
		super(sourceLocation);

		if (func == null)
			throw new IllegalArgumentException("function must not be null.");
		if (returnType == null)
			throw new IllegalArgumentException("return type must not be null.");
		if (args == null)
			throw new IllegalArgumentException("args must not be null.");

		this.func       = func;
		this.returnType = returnType;
		this.args       = args;
	}

	public String toString() { return "FuncCallNode: call to " + func.getName().toUpperCase() + " with " + args.length + " args"; }

	public Class getType() { return returnType; }

	/**
	 *  @return null, This type of node never has any children!
	 */
	public TreeNode getLeftChild() { return null; }

	/**
	 *  @return null, This type of node never has any children!
	 */
	public TreeNode getRightChild() { return null; }

	/**
	 *  @return null, The return value for this node is only known at runtime!
	 */
	public Object getValue() { return null; }

	public void genCode(final Stack<CodeAndSourceLocation> codeStack) {
		for (int i = args.length - 1; i >= 0; --i)
			args[i].genCode(codeStack);
		codeStack.push(new CodeAndSourceLocation(args.length, -1));
		codeStack.push(new CodeAndSourceLocation(func, -1));
		codeStack.push(new CodeAndSourceLocation(Instruction.CALL, getSourceLocation()));
	}
}
