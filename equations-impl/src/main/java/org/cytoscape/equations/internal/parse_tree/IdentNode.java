package org.cytoscape.equations.internal.parse_tree;

/*
 * #%L
 * Cytoscape Equations Impl (equations-impl)
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


import java.util.Stack;

import org.cytoscape.equations.AbstractNode;
import org.cytoscape.equations.CodeAndSourceLocation;
import org.cytoscape.equations.TreeNode;
import org.cytoscape.equations.internal.interpreter.Instruction;


/**
 *  A node in the parse tree representing an attribute reference.
 */
public class IdentNode extends AbstractNode {
	private final String attribName;
	private final Object defaultValue;
	private final Class type;

	public IdentNode(final int sourceLocation, final String attribName, Object defaultValue, final Class type) {
		super(sourceLocation);
		if (type == null)
			throw new IllegalArgumentException("\"type\" must not be null.");
		
		if(defaultValue != null && defaultValue.getClass() != type) {
			if(type == Integer.class && defaultValue instanceof Double && isConvertableToInt((Double)defaultValue)) {
				defaultValue = ((Double)defaultValue).intValue();
			} else if(type == Long.class && defaultValue instanceof Double && isConvertableToInt((Double)defaultValue)) {
				defaultValue = ((Double)defaultValue).longValue();
			} else {
				throw new IllegalArgumentException("default value is not compatible with attribute type, expected: " + type.getSimpleName());
			}
		}
		
		this.attribName = attribName;
		this.defaultValue = defaultValue;
		this.type = type;
	}

	private static boolean isConvertableToInt(double d) {
		return d % 1 == 0.0;
	}
	
	public String toString() {
		return "IdentNode: " + attribName + (defaultValue == null ? "" : " default=" + defaultValue);
	}

	public Class getType() { return type; }

	/**
	 *  @return null, This type of node never has any children!
	 */
	public TreeNode getLeftChild() { return null; }

	/**
	 *  @return null, This type of node never has any children!
	 */
	public TreeNode getRightChild() { return null; }

	public String getAttribName() { return attribName; }
	public Object getDefaultValue() { return defaultValue; }

	public void genCode(final Stack<CodeAndSourceLocation> codeStack) {
		if (defaultValue != null)
			codeStack.push(new CodeAndSourceLocation(defaultValue, -1));
		codeStack.push(new CodeAndSourceLocation(attribName, -1));
		codeStack.push(new CodeAndSourceLocation(defaultValue == null ? Instruction.AREF : Instruction.AREF2, getSourceLocation()));
	}
}
