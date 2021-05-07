package org.cytoscape.equations.internal.builtins;

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


import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionUtil;
import org.cytoscape.equations.internal.Categories;


public class Combin extends AbstractFunction {
	public Combin() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.INT, "n", "The total number of objects."),
				new ArgDescriptor(ArgType.INT, "k", "The size of the selected subset."),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "COMBIN"; }
	
	@Override
	public String getCategoryName() { return Categories.NUMERIC; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns number of combinations of n objects, with k chosen at any one time."; }

	public Class<?> getReturnType() { return Double.class; }

	/**
	 *  @param args the function arguments which must be either one or two objects of type Double
	 *  @return the result of the function evaluation which is the logarithm of the first argument
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the arguments is not of type Double
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final long N = FunctionUtil.getArgAsLong(args[0]);
		final long K = FunctionUtil.getArgAsLong(args[1]);

		if (N < 0)
			throw new IllegalArgumentException("1st argument to COMBIN() must not be negative.");
		if (K < 0)
			throw new IllegalArgumentException("2nd argument to COMBIN() must not be negative.");
		if (N < K)
			throw new IllegalArgumentException("2nd argument to COMBIN() must not be less than 1st argument.");

		// Check boundary cases:
		if (K < 0L || K > N)
			return (Long)0L;

		return (Long)combinations(N, K);
	}

	/**
	 *  Calculate the binomial coefficient C(n,k) while doing our best to avoid overflow.
	 */
	private long combinations(final long n, final long k) {
		if (k == 0 || n == k)
			return 1L;

		final long c1 = combinations(n - 1L, k);
		final long c2 = combinations(n - 1L, k - 1L);
		final long sum = c1 + c2;
		if (sum < c1 || sum < c2)
			throw new ArithmeticException("overflow in call to COMBIN().");
		return sum;
	}
}
