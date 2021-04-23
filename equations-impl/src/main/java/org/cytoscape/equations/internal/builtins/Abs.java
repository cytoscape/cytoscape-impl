package org.cytoscape.equations.internal.builtins;

import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.internal.Categories;


public class Abs extends AbstractFunction {
	public Abs() {
		super(new ArgDescriptor[] { new ArgDescriptor(ArgType.INT, "number", "Any numeric value.") });
		
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "ABS"; }
	

	@Override
	public String getCategoryName() { return Categories.NUMERIC; }
	
	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns the absolute value of a number."; }

	public Class<?> getReturnType() { return Double.class; }

	/**
	 *  @param args the function arguments which must be either one object of type Double or Long
	 *  @return the result of the function evaluation which is the natural logarithm of the first argument
	 */
	public Object evaluateFunction(final Object[] args) {
		final double number;
		if (args[0] instanceof Double)
			number = (Double)args[0];
		else // Assume we are dealing with an integer.
			number = (Long)args[0];

		return Math.abs(number);
	}
}
