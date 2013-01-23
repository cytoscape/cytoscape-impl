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


public class Mod extends AbstractFunction {
	public Mod() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.FLOAT, "number", "The argument."),
				new ArgDescriptor(ArgType.FLOAT, "divisor", "A non-zero number."),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "MOD"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Calculates the remainder of division."; }

	public Class<?> getReturnType() { return Double.class; }

	/**
	 *  @param args the function arguments which must be two objects of type Double or Long
	 *  @return the result of the modulo function evaluation
	 *  @throws ArithmeticException thrown if the 2nd argument is zero
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final double number;
		try {
			number = FunctionUtil.getArgAsDouble(args[0]);
		} catch (final Exception e) {
			throw new IllegalArgumentException("can't convert \"" + args[0] + "\" to a number in a call to MOD().");
		}

		final double divisor;
		try {
			divisor = FunctionUtil.getArgAsDouble(args[1]);
		} catch (final Exception e) {
			throw new IllegalArgumentException("can't convert \"" + args[1] + "\" to a divisor in a call to MOD().");
		}
		if (divisor == 0.0)
			throw new ArithmeticException("division by zero in call to MOD().");

		final double result = number % divisor;
		if (Math.signum(result) != Math.signum(divisor))
			return -result;
		else
			return result;
	}
}

