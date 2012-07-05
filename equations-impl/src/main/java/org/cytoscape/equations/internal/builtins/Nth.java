/*
  File: Nth.java

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


import java.util.List;

import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionUtil;


public class Nth extends AbstractFunction {
	public Nth() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.STRICT_ANY_LIST, "list", "A list of objects."),
				new ArgDescriptor(ArgType.INT, "index", "An index into the list."),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "NTH"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns the n-th entry in a list."; }

	public Class<?> getReturnType() { return Object.class; }

	/**
	 *  @param args the function arguments which must be a list followed by a number
	 *  @return the n-th entry (1-based) in the list
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if the index is out of range
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final List list = (List)args[0];
		final int index;
		try {
			index = (int)FunctionUtil.getArgAsLong(args[1]);
		} catch (final Exception e) {
			throw new IllegalArgumentException("can't convert \"" + args[1] + "\" to an integer in a call to NTH().");
		}

		if (index <= 0 || index > list.size())
			throw new IllegalArgumentException("illegal list index in call to NTH().");

		final Object listElement = list.get(index - 1);
		final Object retVal = FunctionUtil.translateObjectType(listElement);
		if (retVal == null)
			throw new IllegalArgumentException("bad list element type: " + listElement.getClass() + " in a call to NTH().");

		return retVal;
	}
}
