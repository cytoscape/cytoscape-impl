package org.cytoscape.equations.internal;

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
import java.util.Map;
import java.util.Stack;

import org.cytoscape.equations.CodeAndSourceLocation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.TreeNode;


public class EquationCompilerImpl implements EquationCompiler {
	private final EquationParser parser;
	private Equation equation;
	private String errorMsg;

	public EquationCompilerImpl(final EquationParser parser) {
		this.parser = parser;
	}

	public boolean compile(final String equation, final Map<String, Class<?>> variableNameToTypeMap) {
		this.equation = null;
		this.errorMsg = null;

		if (!parser.parse(equation, variableNameToTypeMap)) {
			errorMsg = parser.getErrorMsg();
			return false;
		}

		final TreeNode parseTree = parser.getParseTree();

		final Stack<CodeAndSourceLocation> codeStack = new Stack<CodeAndSourceLocation>();
		try {
			parseTree.genCode(codeStack);
		} catch (final IllegalStateException e) {
			errorMsg = e.getCause().toString();
			return false;
		}

		final Object[] code = new Object[codeStack.size()];
		final int[] sourceLocations = new int[codeStack.size()];
		for (int i = code.length - 1; i >= 0; --i) {
			final CodeAndSourceLocation codeAndSourceLocation = codeStack.pop();
			code[i]            = codeAndSourceLocation.getCode();
			sourceLocations[i] = codeAndSourceLocation.getSourceLocation();
		}
		this.equation = new Equation(equation, parser.getVariableReferences(),
		                             parser.getDefaultVariableValues(), code, sourceLocations,
		                             parser.getType());

		errorMsg = null;
		return true;
	}

	public String getLastErrorMsg() { return errorMsg; }

	public Equation getEquation() { return equation; }

	public EquationParser getParser() { return parser; }

	/**
	 *  A factory method that returns an Equation that always fails at runtime.
	 *
	 *  @param equation      an arbitrary string that is usually a syntactically invalid equation
	 *  @param type          the return type of the error equation
	 *  @param errorMessage  the runtime error message that the returned equation will produce
	 */
	public Equation getErrorEquation(final String equation, final Class<?> type, final String errorMessage) {
		final Map<String, Class<?>> variableNameToTypeMap = new HashMap<String, Class<?>>();
		if (!compile("=ERROR(\"" + escapeQuotes(errorMessage) + "\")", variableNameToTypeMap))
			throw new IllegalStateException("internal error in Equation.getErrorEquation().  This should *never* happen.");

		final Equation errorEquation = getEquation();
		return new Equation(equation, errorEquation.getVariableReferences(),
		                    errorEquation.getDefaultVariableValues(), errorEquation.getCode(),
		                    errorEquation.getSourceLocations(), type);
	}

	private static  String escapeQuotes(final String s) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < s.length(); ++i) {
			final char ch = s.charAt(i);
			if (ch == '"')
				builder.append('\\');
			builder.append(ch);
		}

		return builder.toString();
	}
}
