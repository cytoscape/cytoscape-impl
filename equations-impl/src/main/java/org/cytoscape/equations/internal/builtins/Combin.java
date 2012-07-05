/*
  File: Combin.java

  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.equations.internal.builtins;


import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionUtil;


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

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns of combinations of n objects, with k chosen at any one time."; }

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
