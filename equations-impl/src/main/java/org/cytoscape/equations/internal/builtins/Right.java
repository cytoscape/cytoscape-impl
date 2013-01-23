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
import org.cytoscape.equations.FunctionUtil;


public class Right extends AbstractFunction {
	public Right() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.STRING, "text", "The source text."),
				new ArgDescriptor(ArgType.OPT_FLOAT, "number", "How many characters to extract. (Default is 1.)"),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "RIGHT"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns a suffix of a string."; }

	public Class<?> getReturnType() { return String.class; }

	/**
	 *  @param args the function arguments which must be either one or two objects of type Double
	 *  @return the result of the function evaluation which is the natural logarithm of the first argument
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the arguments is not of type Boolean
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final String text = FunctionUtil.getArgAsString(args[0]);
		final int count;
		if (args.length == 1)
			count = 1;
		else {
			try {
				count = (int)FunctionUtil.getArgAsLong(args[1]);
			} catch (final Exception e) {
				throw new IllegalArgumentException("can't convert \"" + args[1] + "\" to an integer in a call to RIGHT().");
			}
			if (count < 0)
				throw new IllegalArgumentException("illegal character count in a call to RIGHT().");
		}

		if (count >= text.length())
			return text;
		return text.substring(text.length() - count);
	}
}
