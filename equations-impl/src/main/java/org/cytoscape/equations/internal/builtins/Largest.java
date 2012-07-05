/*
  File: Largest.java

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
import org.cytoscape.equations.EquationUtil;
import org.cytoscape.equations.FunctionUtil;


public class Largest extends AbstractFunction {
	public Largest() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.STRICT_ANY_LIST, "list", "A list of numbers."),
				new ArgDescriptor(ArgType.INT, "k", "Specifies the rank of the number that will be selected."),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "LARGEST"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns the kth largest element of a list of numbers."; }

	public Class<?> getReturnType() { return Double.class; }

	/**
	 *  @param args the function arguments which must be a list followed by a numeric argument
	 *  @return the result of the function evaluation which is the maximum of the elements in the single list argument or the maximum of the one or more double arguments
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the arguments is not of type Double
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final List list = (List)args[0];
		if (list.isEmpty())
			throw new IllegalArgumentException("illegal empty list argument in call to LARGEST().");

		final double[] array = new double[list.size()];
		int i = 0;
		for (final Object listElement : list) {
			try {
				array[i++] = FunctionUtil.getArgAsDouble(listElement);
			} catch (final IllegalArgumentException e) {
				throw new IllegalArgumentException(FunctionUtil.getOrdinal(i)
				                                   + " list element in call to LARGEST() is not a number: "
				                                   + e.getMessage());
			}
		}

		final long k;
		try {
			k = FunctionUtil.getArgAsLong(args[1]);
		} catch (final Exception e) {
			throw new IllegalArgumentException("can't convert \"" + args[1] + "\" to an integer argument in a call to LARGEST().");
		}
		if (k <= 0)
			throw new IllegalArgumentException("invalid index " + args[1] + " in a call to LARGEST().");
		if (k > array.length)
			throw new IllegalArgumentException("index " + args[1] + " is too large for a list w/ " + array.length + " elements in a call to LARGEST().");

		return (Double)kthSmallest(array, array.length - (int)k);
	}

	/**
	 *  @return the kth smallest array element, with the 0th being the smallest, the 1st being the 2nd most smallest etc.
	 */
	private double kthSmallest(final double[] array, final int k) {
		int first = 0;
		int last = array.length - 1;

		for (;;) {
			final int middle = partition(array, first, last);

			if (middle == k)
				return array[k];

			if (middle < k)
				first = middle + 1;
			else // middle > k
				last = middle - 1;
		}

	}

	private int partition(final double[] array, final int first, final int last) {
		final int pivotIndex = medianOf3PiviotPosition(array, first, last);
		final double pivotValue = array[pivotIndex];
		swap(array, pivotIndex, last);
		int storeIndex = first;
		for (int i = first; i < last; ++i) {
			if (array[i] <= pivotValue) {
				swap(array, i, storeIndex);
				++storeIndex;
			}
		}
		swap(array, storeIndex, last);
		return storeIndex;
	}

	private int medianOf3PiviotPosition(final double[] array, int first, int last) {
		final int middle = (first + last) / 2;

		if (array[first] > array[last]) {
			int temp = first;
			first = last;
			last = temp;
		}

		if (array[middle] > array[last])
			return last;

		return array[first] > array[middle] ? first : middle;
	}

	private void swap(final double[] array, final int i, final int j) {
		final double temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}
}
