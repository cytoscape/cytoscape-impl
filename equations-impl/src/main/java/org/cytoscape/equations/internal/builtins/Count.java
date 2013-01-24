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


import java.util.List;

import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;


public class Count extends AbstractFunction {
	public Count() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.ANY_LIST, "list", "One or more lists or individual items.")
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "COUNT"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns the number of numeric values in a list."; }

	public Class<?> getReturnType() { return Long.class; }

	/**
	 *  @param args the function arguments which can be anything
	 *  @return the result of the function evaluation which is the count of the arguments that are numbers for scalar arguments plus the count of list entries that are numbers for List arguments
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		int count = 0;
		for (final Object arg : args) {
			if (arg instanceof List) {
				final List list = (List)arg;
				for (final Object listEntry : list) {
					if (listEntry.getClass() == Double.class || listEntry.getClass() == Long.class)
						++count;
					else if (listEntry.getClass() == String.class && isValidDouble((String)listEntry))
						++count;
				}
			}
			else {
				if (arg.getClass() == Double.class || arg.getClass() == Long.class)
					++count;
				else if (arg.getClass() == String.class && isValidDouble((String)arg))
					++count;
			}
		}

		return (long)count;
	}

	/**
	 *  @return true if "s" contains the string representation of a valid, finite, non-NaN double
	 */
	private boolean isValidDouble(final String s) {
		try {
			final double d = Double.parseDouble(s);
			return !Double.isNaN(d) && !Double.isInfinite(d);
		} catch (final NumberFormatException e) {
			return false;
		}
	}
}
