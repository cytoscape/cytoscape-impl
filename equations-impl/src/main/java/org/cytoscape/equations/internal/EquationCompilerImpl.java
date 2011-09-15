/*
  File: EquationCompilerImpl.java

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
package org.cytoscape.equations.internal;


import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.cytoscape.equations.CodeAndSourceLocation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.Node;


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

		final Node parseTree = parser.getParseTree();

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
			throw new IllegalStateException("internal error in Equation.getErrorEquation().  This should *never* happen!");

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
