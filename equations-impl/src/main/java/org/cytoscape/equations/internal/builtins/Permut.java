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


public class Permut extends AbstractFunction {
	public Permut() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.INT, "n", "The total number of objects."),
				new ArgDescriptor(ArgType.INT, "k", "The size of the selected subset."),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "PERMUT"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns the number of permutations of size k of n objects."; }

	public Class<?> getReturnType() { return Double.class; }

	/**
	 *  @param args the function arguments which must be either one or two objects of type Double
	 *  @return the result of the function evaluation which is the logarithm of the first argument
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the arguments is not of type Double
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final long N = FunctionUtil.getArgAsLong(args[0]);
		if (N <= 0L)
			throw new IllegalArgumentException("first argument to PERMUT must be positive.");

		final long K = FunctionUtil.getArgAsLong(args[1]);
		if (K < 0L)
			throw new IllegalArgumentException("second argument to PERMUT must be nonnegative.");
		if (K > N)
			throw new IllegalArgumentException("second argument to PERMUT must be no greater than the first argument.");

		long retval = 1L;
		long multiplier = N;
		for (long i = 0; i < K; ++i) {
			final long next = retval * multiplier;
			if (next < retval)
				throw new ArithmeticException("overflow detected while calulating PERMUT(" + N + "," + K + ".");
			--multiplier;
			retval = next;
		}

		return (Long)retval;
	}
}
