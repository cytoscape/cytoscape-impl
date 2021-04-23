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
import org.cytoscape.equations.internal.Categories;


public class Substitute extends AbstractFunction {
	public Substitute() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.STRING, "text", "The source text."),
				new ArgDescriptor(ArgType.STRING, "original", "The text that will be replaced."),
				new ArgDescriptor(ArgType.STRING, "replacement", "The replacement text."),
				new ArgDescriptor(ArgType.OPT_INT, "nth_appearance", "Which occurrence to replace.")
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "SUBSTITUTE"; }
	
	@Override
	public String getCategoryName() { return Categories.TEXT; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Replaces some text with other text."; }

	public Class<?> getReturnType() { return String.class; }

	/**
	 *  @param args the function arguments which must be either one or two objects of type String
	 *  @return the result of the function evaluation which is the natural logarithm of the first argument
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the arguments is not of type Boolean
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final String text        = FunctionUtil.getArgAsString(args[0]);
		final String original    = FunctionUtil.getArgAsString(args[1]);
		final String replacement = FunctionUtil.getArgAsString(args[2]);

		if (args.length == 3)
			return replaceAll(text, original, replacement);
		else { // Assume args.length == 4
			final int nthAppearance;
			try {
				nthAppearance = (int)FunctionUtil.getArgAsLong(args[3]);
			} catch (final Exception e) {
				throw new IllegalArgumentException("can't convert \"" + args[3] + "\" to a number in a call to SUBSTITUTE().");
			}

			if (nthAppearance <= 0)
				return text;
			final int startIndex;
			if ((startIndex = findNth(nthAppearance, text, original)) == -1)
				return text;

			return text.substring(0, startIndex) + replacement + text.substring(startIndex + original.length());
		}
	}

	/**
	 *  @return the 0-based starting position of the nth appearance of "needle" in "hayStack" or -1 if it can't be found
	 */
	private static int findNth(final int n, final String hayStack, final String needle) {
		int startOffset = 0;
		int offset = 0;
		for (int i = 0; i < n; ++i) {
			if ((offset = hayStack.indexOf(needle, startOffset)) == -1)
				return offset;
			startOffset = offset + needle.length();
		}

		return offset;
	}

	/**
	 *  @return a string where all occurrences of "original" found in "s" have been replaced with "replacement"
	 */
	private static String replaceAll(final String s, final String original, final String replacement) {
		final StringBuilder builder = new StringBuilder();

		int startOffset = 0;
		int startMatch;
		while ((startMatch = s.indexOf(original, startOffset)) != -1) {
			builder.append(s.substring(startOffset, startMatch));
			builder.append(replacement);
			startOffset = startMatch + original.length();
		}
		builder.append(s.substring(startOffset));

		return builder.toString();
	}
}
