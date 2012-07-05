/*
  File: BinOpNode.java

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
 *  A node in the parse tree representing a binary operator.
 */
public class BinOpNode extends AbstractNode {
	private final Token operator;
	private final TreeNode lhs, rhs;

	public BinOpNode(final int sourceLocation, final Token operator, final TreeNode lhs, final TreeNode rhs) {
		super(sourceLocation);

		if (lhs == null)
			throw new IllegalArgumentException("left operand must not be null.");
		if (rhs == null)
			throw new IllegalArgumentException("right operand must not be null.");

		this.operator = operator;
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public String toString() { return "BinOpNode: " + operator; }

	public Class getType() { return operator.isComparisonOperator() ? Boolean.class : lhs.getType(); }

	/**
	 *  @return the left operand
	 */
	public TreeNode getLeftChild() { return lhs; }

	/**
	 *  @return the right operand
	 */
	public TreeNode getRightChild() { return rhs; }

	public Token getOperator() { return operator; }

	public void genCode(final Stack<CodeAndSourceLocation> codeStack) {
		rhs.genCode(codeStack);
		lhs.genCode(codeStack);

		switch (operator) {
		case CARET:
			codeStack.push(new CodeAndSourceLocation(Instruction.FPOW, getSourceLocation()));
			break;
		case PLUS:
			codeStack.push(new CodeAndSourceLocation(Instruction.FADD, getSourceLocation()));
			break;
		case MINUS:
			codeStack.push(new CodeAndSourceLocation(Instruction.FSUB, getSourceLocation()));
			break;
		case DIV:
			codeStack.push(new CodeAndSourceLocation(Instruction.FDIV, getSourceLocation()));
			break;
		case MUL:
			codeStack.push(new CodeAndSourceLocation(Instruction.FMUL, getSourceLocation()));
			break;
		case EQUAL:
			codeStack.push(new CodeAndSourceLocation(determineOpCode(Instruction.BEQLF, Instruction.BEQLS, Instruction.BEQLB),
			                                         getSourceLocation()));
			break;
		case NOT_EQUAL:
			codeStack.push(new CodeAndSourceLocation(determineOpCode(Instruction.BNEQLF, Instruction.BNEQLS, Instruction.BNEQLB),
			                                         getSourceLocation()));
			break;
		case GREATER_THAN:
			codeStack.push(new CodeAndSourceLocation(determineOpCode(Instruction.BGTF, Instruction.BGTS, Instruction.BGTB),
			                                         getSourceLocation()));
			break;
		case LESS_THAN:
			codeStack.push(new CodeAndSourceLocation(determineOpCode(Instruction.BLTF, Instruction.BLTS, Instruction.BLTB),
			                                         getSourceLocation()));
			break;
		case GREATER_OR_EQUAL:
			codeStack.push(new CodeAndSourceLocation(determineOpCode(Instruction.BGTEF, Instruction.BGTES, Instruction.BGTEB),
			                                         getSourceLocation()));
			break;
		case LESS_OR_EQUAL:
			codeStack.push(new CodeAndSourceLocation(determineOpCode(Instruction.BLTEF, Instruction.BLTES, Instruction.BLTEB),
			                                         getSourceLocation()));
			break;
		case AMPERSAND:
			codeStack.push(new CodeAndSourceLocation(Instruction.SCONCAT, getSourceLocation()));
			break;
		default:
			throw new IllegalStateException(getSourceLocation() + ": unknown operator: " + operator + ".");
		}
	}

	/**
	 *  Picks one of three opcodes based on operand types.
	 *  (N.B.: We assume that the LHS and RHS operands are of the same type!)
	 */
	private Instruction determineOpCode(final Instruction floatOpCode, final Instruction stringOpCode, final Instruction booleanOpCode) {
		final Class operandType = lhs.getType();
		if (operandType == Double.class)
			return floatOpCode;
		else if (operandType == String.class)
			return stringOpCode;
		else if (booleanOpCode != null && operandType == Boolean.class)
			return booleanOpCode;

		throw new IllegalStateException(lhs.getSourceLocation() + ": invalid LHS operand type for comparison: " + operandType + ".");
	}
}
