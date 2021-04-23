package org.cytoscape.equations.internal.builtins;

import java.util.List;

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
import org.cytoscape.equations.internal.Categories;


public class Value extends AbstractFunction {
	public Value() {
		super(new ArgDescriptor[] { new ArgDescriptor(ArgType.ANY, "text_or_number", "A number or a string representing a number.") });
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "VALUE"; }
	
	@Override
	public String getCategoryName() { return Categories.TEXT; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Converts a string to a number."; }

	public Class<?> getReturnType() { return Double.class; }

	/**
	 *  @param args the function arguments which must be either one or two objects of type Double
	 *  @return the result of the function evaluation which is the logarithm of the first argument
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the arguments is not of type Double
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		if(args[0] instanceof Number) {
			return ((Number)args[0]).doubleValue();
		}
 	 	if(args[0] instanceof Boolean) {
 	 		return (Boolean)args[0] ? 1.0 : 0.0;
 	 	}
		if(args[0] instanceof List) {
			throw new IllegalArgumentException("List argument to VALUE() cannot be converted to a number.");
		}

		try {
			return Double.parseDouble((String)args[0]);
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("text argument \"" + args[0] + "\"of VALUE() function is not a valid number.");
		}
	}
}
