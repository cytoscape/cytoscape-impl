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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.Function;
import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.IdentDescriptor;
import org.cytoscape.equations.Interpreter;

import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;


class Framework {
	static private class BadReturnFunction implements Function {
		public String getName() { return "BAD"; }
		public String getFunctionSummary() { return "Returns an invalid type at runtime."; }
		public String getUsageDescription() { return "Call this with \"BAD()\"."; }
		public Class getReturnType() { return Double.class; }
		public Class validateArgTypes(final Class[] argTypes) { return argTypes.length == 0 ? Double.class : null; }
		public Object evaluateFunction(final Object[] args) { return new Integer(1); }
		public List<Class<?>> getPossibleArgTypes(final Class[] leadingArgs) { return null; }
	}

	private static final EquationCompiler compiler;

	static {
		compiler = new EquationCompilerImpl(new EquationParserImpl());
		compiler.getParser().registerFunction(new BadReturnFunction());
	}

	/**
	 *  Execute a test that should succeed at compile time and runtime.
	 *  @return true if the test compiled and ran and produced the expected result
	 */
	static boolean executeTest(final String equation, final Map<String, Object> variablesAndValues, final Object expectedResult) {
		final Map<String, Class<?>> varNameToTypeMap = new HashMap<String, Class<?>>();
		for (final String variableName : variablesAndValues.keySet())
			varNameToTypeMap.put(variableName, variablesAndValues.get(variableName).getClass());
		
		try {
			if (!compiler.compile(equation, varNameToTypeMap)) {
				System.err.println("Error while compiling \"" + equation + "\": " + compiler.getLastErrorMsg());
				return false;
			}
		} catch (final Exception e) {
			System.err.println("Error while compiling \"" + equation + "\": " + e.getMessage());
			return false;
		}

		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		try {
			for (final String variableName : variablesAndValues.keySet())
				nameToDescriptorMap.put(variableName, new IdentDescriptor(variablesAndValues.get(variableName)));
		} catch (final Exception e) {
			System.err.println("Error while processing variables for \"" + equation + "\": " + e.getMessage());
			return false;
		}

		final Interpreter interpreter = new InterpreterImpl();
		final Object actualResult;
		try {
			actualResult = interpreter.execute(compiler.getEquation(), nameToDescriptorMap);
		} catch (final Exception e) {
			System.err.println("caught runtime error: " + e.getMessage());
			return false;
		}

		if (!areEqual(actualResult, expectedResult)) {
			System.err.println("[" + equation + "] expected: " + expectedResult + ", found: " + actualResult);
			return false;
		}

		return true;
	}

	static boolean executeTest(final String equation, final Object expectedResult) {
		final Map<String, Object> variablesAndValues = new HashMap<String, Object>();
		return executeTest(equation, variablesAndValues, expectedResult);
	}

	/**
	 *  Excecute a test that should fail at either compile time or runtime.
	 *  @return true if the test fails at compile time or runtime, otherwise false
	 *
	 */
	static boolean executeTestExpectFailure(final String equation, final Map<String, Object> variablesAndValues) {
		final Map<String, Class<?>> varNameToTypeMap = new HashMap<String, Class<?>>();
		for (final String variableName : variablesAndValues.keySet())
			varNameToTypeMap.put(variableName, variablesAndValues.get(variableName).getClass());
		
		try {
			if (!compiler.compile(equation, varNameToTypeMap)) {
				System.err.println("Error while compiling \"" + equation + "\": " + compiler.getLastErrorMsg());
				return true;
			}
		} catch (final Exception e) {
			System.err.println("Error while compiling \"" + equation + "\": " + e.getMessage());
			return true;
		}

		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		try {
			for (final String variableName : variablesAndValues.keySet())
				nameToDescriptorMap.put(variableName, new IdentDescriptor(variablesAndValues.get(variableName)));
		} catch (final Exception e) {
			System.err.println("Error while processing variables for \"" + equation + "\": " + e.getMessage());
			return true;
		}

		final Interpreter interpreter = new InterpreterImpl();
		try {
			final Object result = interpreter.execute(compiler.getEquation(), nameToDescriptorMap);
			// We should never get here!
			return false;
		} catch (final Exception e) {
			return true;
		}
	}

	static boolean executeTestExpectFailure(final String equation) {
		final Map<String, Object> variablesAndValues = new HashMap<String, Object>();
		return executeTestExpectFailure(equation, variablesAndValues);
	}

	/**
	 *  @return the unbiased exponent of a double-precision IEEE floating point number
	 */
	private static long getExponent(final double f) {
		final long EXPONENT_MASK = 0x7FFFF00000000000L;
		final long bits = Double.doubleToLongBits(f) & EXPONENT_MASK;
		final int BIAS = 1023;
		final int BIT_OFFSET = 52;
		return (bits >> BIT_OFFSET) - BIAS;
	}

	private static boolean almostEqual(final double x1, final double x2) {
		if (x1 == x2)
			return true;

		if (Math.signum(x1) != Math.signum(x2))
			return false;

		if (getExponent(x1) != getExponent(x2))
			return false;

		final double absX1 = Math.abs(x1);
		final double absX2 = Math.abs(x2);

		if (x1 != 0.0)
			return Math.abs(x1 - x2) / Math.abs(x1) < 1.0e-12;
		else
			return Math.abs(x1 - x2) / Math.abs(x2) < 1.0e-12;
	}

	private static boolean areEqual(final Object o1, final Object o2) {
		if (o1 instanceof Double && o2 instanceof Double)
			return almostEqual((Double)o1, (Double)o2);
		else if (o1 instanceof List && o2 instanceof List) {
			final List l1 = (List)o1;
			final List l2 = (List)o2;
			if (l1.size() != l2.size())
				return false;

			for (int i = 0; i < l1.size(); ++i) {
				if (!areEqual(l1.get(i), l2.get(i)))
					return false;
			}
			return true;
		}
		else
			return o1.equals(o2);
	}
}
