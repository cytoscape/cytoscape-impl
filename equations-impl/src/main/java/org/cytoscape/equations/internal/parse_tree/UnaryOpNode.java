/*
  File: UnaryOpNode.java

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
import org.cytoscape.equations.AbstractNode;
import org.cytoscape.equations.TreeNode;
import org.cytoscape.equations.internal.Token;
import org.cytoscape.equations.internal.interpreter.Instruction;


/**
 *  A node in the parse tree representing a unary operator application.
 */
public class UnaryOpNode extends AbstractNode {
	private final Token operator;
	private final TreeNode operand;

	public UnaryOpNode(final int sourceLocation, final Token operator, final TreeNode operand) {
		super(sourceLocation);

		if (operand == null)
			throw new IllegalArgumentException("operand must not be null.");

		this.operator = operator;
		this.operand = operand;
	}

	public String toString() { return "UnaryOpNode: " + operator; }

	public Class getType() { return operand.getType(); }

	/**
	 *  @return the operand
	 */
	public TreeNode getLeftChild() { return operand; }

	/**
	 *  @return null, This type of node never has any left children!
	 */
	public TreeNode getRightChild() { return null; }

	public Token getOperator() { return operator; }

	public void genCode(final Stack<CodeAndSourceLocation> codeStack) {
		operand.genCode(codeStack);

		switch (operator) {
		case PLUS:
			codeStack.push(new CodeAndSourceLocation(Instruction.FUPLUS, getSourceLocation()));
			break;
		case MINUS:
			codeStack.push(new CodeAndSourceLocation(Instruction.FUMINUS, getSourceLocation()));
			break;
		default:
			throw new IllegalStateException("invalid unary operation: " + operator + ".");
		}
	}
}
