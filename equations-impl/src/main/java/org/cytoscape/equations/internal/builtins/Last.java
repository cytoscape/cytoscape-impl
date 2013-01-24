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


public class Last extends AbstractFunction {
	public Last() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.ANY_LIST, "list", "Any non-empty list."),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "LAST"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns the last entry in a list."; }

	public Class<?> getReturnType() { return Object.class; }

	/**
	 *  @param args the function arguments which must be a single list
	 *  @return the last entry in the list
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if the list is empty
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final List list = (List)args[0];
		if (list.isEmpty())
			throw new IllegalArgumentException("can't get the last argument of an empty list in a call to LAST().");

		final Object lastElement = list.get(list.size() - 1);
		final Object retVal = FunctionUtil.translateObjectType(lastElement);
		if (retVal == null)
			throw new IllegalArgumentException("bad list element type: " + lastElement.getClass() + " in a call to LAST().");

		return retVal;
	}
}
