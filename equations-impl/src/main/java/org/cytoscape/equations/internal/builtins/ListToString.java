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
import org.cytoscape.equations.FunctionUtil;


public class ListToString extends AbstractFunction {
	public ListToString() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.STRICT_ANY_LIST, "list", "Any list."),
				new ArgDescriptor(ArgType.STRING, "separator", "The text that will be inserted between the individual list elements.")
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "LISTTOSTRING"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Converts a list to a string, given a separator."; }

	public Class<?> getReturnType() { return String.class; }

	/**
	 *  @param args the function arguments which must be either one object of type Double or Long
	 *  @return the result of the function evaluation which is the natural logarithm of the first argument
	 */
	public Object evaluateFunction(final Object[] args) {
		final List list = (List)args[0];
		final String separator = FunctionUtil.getArgAsString(args[1]);

		final StringBuilder result = new StringBuilder();
		int count = 0;
		for (final Object listEntry : list) {
			result.append(listEntry.toString());
			if (++count < list.size())
				result.append(separator);
		}

		return result.toString();
	}
}
