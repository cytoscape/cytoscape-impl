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


public class Trunc extends AbstractFunction {
	public Trunc() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.FLOAT, "number", "Any number."),
				new ArgDescriptor(ArgType.OPT_FLOAT, "num_digits", "The number of digits after the decimal point.")
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "TRUNC"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Truncates a number. (Rounds towards zero.)"; }

	public Class<?> getReturnType() { return Double.class; }

	/**
	 *  @param args the function arguments which must be either one or two objects of type Double
	 *  @return the result of the function evaluation which is the truncated first argument
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if the first argument cannot be converted to floating point and the
	 *          optional second argument cannot be converted to integer
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final double number;
		try {
			number = FunctionUtil.getArgAsDouble(args[0]);
		} catch (final Exception e) {
			throw new IllegalArgumentException("cannot convert \"" + args[0] +"\" to a number in a call to TRUNC().");
		}
		final double absNumber = Math.abs(number);

		final long numDigits;
		if (args.length == 1)
			numDigits = 0;
		else {
			try {
				numDigits = FunctionUtil.getArgAsLong(args[1]);
			} catch (final Exception e) {
				throw new IllegalArgumentException("cannot convert \"" + args[1] +"\" to an integer in a call to TRUNC().");
				
			}
		}

		final double shift = Math.pow(10.0, (double)numDigits);
		final double truncatedAbsNumber = Math.round(absNumber * shift - 0.5) / shift;

		return number > 0.0 ? truncatedAbsNumber : -truncatedAbsNumber;
	}
}
