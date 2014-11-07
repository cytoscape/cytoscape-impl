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


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionUtil;


public class Text extends AbstractFunction {
	public Text() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.FLOAT, "value", "Any number."),
				new ArgDescriptor(ArgType.OPT_STRING, "format", "How to format the first argument using the conventions of the Java DecimalFormat class."),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "TEXT"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns a number formatted as text."; }

	public Class<?> getReturnType() { return String.class; }

	/**
	 *  @param args the function arguments which must be either one or two objects of type Double
	 *  @return the result of the function evaluation which is the first argument formatted as a string
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the arguments is not of type Boolean
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final Double doubleValue = FunctionUtil.getArgAsDouble(args[0]);
		final Number value;
		
		// use double if value can't be represented by BigDecimal
		if(doubleValue.isInfinite() || doubleValue.isNaN())
			value = doubleValue;
		else value = new BigDecimal(doubleValue.toString());

		if (args.length == 1)
			return value.toString();
		else {
			final String format = FunctionUtil.getArgAsString(args[1]);
			if (!isValidFormat(format))
				throw new IllegalArgumentException("\"" + format +"\" is not a valid format string for the TEXT() function.");

			final DecimalFormat decimalFormat;
			try {
				decimalFormat = new DecimalFormat(format, new DecimalFormatSymbols(Locale.US));
			} catch (final Exception e) {
				throw new IllegalStateException("we should *never* get here.");
			}

			try {
				return decimalFormat.format(value).toString();
			} catch (final Exception e) {
				throw new IllegalStateException("we should *never* get here (2): " + e);
			}
		}
	}

	private boolean isValidFormat(final String format) {
		try {
			new DecimalFormat(format);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}
}
