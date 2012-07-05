/*
  File: Trunc.java

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
