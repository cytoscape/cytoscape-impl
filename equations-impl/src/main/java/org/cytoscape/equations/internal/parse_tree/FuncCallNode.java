/*
  File: FuncCallNode.java

  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.equations.internal.parse_tree;


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
			throw new IllegalArgumentException("function must not be null!");
		if (returnType == null)
			throw new IllegalArgumentException("return type must not be null!");
		if (args == null)
			throw new IllegalArgumentException("args must not be null!");

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
