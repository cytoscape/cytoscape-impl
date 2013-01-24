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
