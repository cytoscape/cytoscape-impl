/*
  File: GeoMean.java

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


import java.util.ArrayList;
import java.util.List;

import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionUtil;


public class GeoMean extends AbstractFunction {
	public GeoMean() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.FLOATS, "numbers", "Two or more positive numbers."),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "GEOMEAN"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns the geometric mean of a set of numbers."; }

	public Class<?> getReturnType() { return Double.class; }

	/**
	 *  @param args the function arguments which must be a list followed by a numeric argument
	 *  @return the result of the function evaluation which is the maximum of the elements in the single list argument or the maximum of the one or more double arguments
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the members of the single List argument cannot be converted to a number
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final ArrayList<Double> a = new ArrayList<Double>();
		for (int i = 0; i < args.length; ++i) {
			if (args[i] instanceof List) {
				final List list = (List)(args[i]);
				for (final Object listElement : list) {
					try {
						final double d = FunctionUtil.getArgAsDouble(listElement);
						if (d <= 0.0)
							throw new IllegalArgumentException(FunctionUtil.getOrdinal(i) +
											   " argument in call to GEOMEAN() is not a list of positive numbers.");
						a.add(d);
					} catch (final IllegalArgumentException e) {
						throw new IllegalArgumentException(FunctionUtil.getOrdinal(i) +
										   " element in call to GEOMEAN() is not a list of numbers: "
										   + e.getMessage());
					}
				}
			} else {
				try {
					final double d = FunctionUtil.getArgAsDouble(args[i]);
					if (d <= 0.0)
						throw new IllegalArgumentException(FunctionUtil.getOrdinal(i) +
										   " element in call to GEOMEAN() is not a positive number.");
					
					a.add(d);
				} catch (final IllegalArgumentException e) {
					throw new IllegalArgumentException(FunctionUtil.getOrdinal(i) +
									   " element in call to GEOMEAN() is not a number: "
									   + e.getMessage());
				}
			}
		}

		if (a.size() < 2)
			throw new IllegalArgumentException("illegal arguments in call to GEOMEAN(): must have at least 2 numbers.");

		double sumOfLogs = 0.0;
		for (double d : a)
			sumOfLogs += Math.log(d);
		return Math.exp(sumOfLogs / a.size());
	}
}
