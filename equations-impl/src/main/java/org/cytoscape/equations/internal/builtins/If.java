package org.cytoscape.equations.internal.builtins;

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


import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;


public class If extends AbstractFunction {
	public If() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.BOOL, "condition", "A logical test expression."),
				new ArgDescriptor(ArgType.ANY, "value_if_true", "The return value if the test expression evaluates to true."),
				new ArgDescriptor(ArgType.ANY, "value_if_false", "The return value if the test expression evaluates to false."),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "IF"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns one of two alternatives based on a boolean value."; }

	public Class<?> getReturnType() { return Object.class; }

	/**
	 *  @param args the function arguments
	 *  @return the result of the function evaluation which is either the 2nd or 3rd argument of the function
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the arguments is not of type Boolean
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final boolean condition = (Boolean)args[0];

		if (args[1].getClass() == args[2].getClass())
			return args[condition ? 1 : 2];
		else
			return args[condition ? 1 : 2].toString();
	}
}
