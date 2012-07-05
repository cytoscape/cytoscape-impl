/*
  File: Mode.java

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


import java.util.Arrays;

import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionUtil;


public class Mode extends AbstractFunction {
	public Mode() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.FLOATS, "numbers", "One or more numbers or lists of numbers."),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "MODE"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns the mode of a list of numbers."; }

	public Class<?> getReturnType() { return Double.class; }

	/**
	 *  @param args the function arguments which must be a list followed by a numeric argument
	 *  @return the result of the function evaluation which is the maximum of the elements in the single list argument or the maximum of the one or more double arguments
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the members of the single List argument cannot be converted to a number
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final double[] x;
		try {
			x = FunctionUtil.getDoubles(args);
		} catch (final Exception e) {
			throw new IllegalArgumentException("non-numeric argument or list element in a call to MODE().");
		}

		Arrays.sort(x);

		double mode = 0.0;
		int highestFrequencySoFar = 0;
		double currentValue = -x[0];
		int currentFrequency = 0;
		for (double d : x) {
			if (d == currentValue)
				++currentFrequency;
			else {
				if (currentFrequency > highestFrequencySoFar) {
					highestFrequencySoFar = currentFrequency;
					mode = currentValue;
				}
				currentFrequency = 1;
				currentValue = d;
			}
		}

		if (highestFrequencySoFar < 2)
			throw new IllegalArgumentException("the are no duplicates in the list of numbers supplied to MODE().");

		return mode;
	}
}
