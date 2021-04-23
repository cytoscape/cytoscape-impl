package org.cytoscape.equations.internal.builtins;

import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionError;
import org.cytoscape.equations.FunctionUtil;
import org.cytoscape.equations.internal.Categories;


public class HarMean extends AbstractFunction {
	public HarMean() {
		super(new ArgDescriptor[] {
				new ArgDescriptor(ArgType.FLOATS, "numbers", "Two or more positive numbers."),
			});
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "HARMEAN"; }
	
	@Override
	public String getCategoryName() { return Categories.NUMERIC; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns the harmonic mean of a set of numbers."; }

	public Class<?> getReturnType() { return Double.class; }

	/**
	 *  @param args the function arguments which must be a list followed by a numeric argument
	 *  @return the result of the function evaluation which is the maximum of the elements in the single list argument or the maximum of the one or more double arguments
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the members of the single List argument cannot be converted to a number
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final double[] numbers;
		try {
			numbers = FunctionUtil.getDoubles(args);
		} catch (final FunctionError e) {
			throw new IllegalArgumentException("bad argument in a call to HARMEAN(): " + e.getMessage());
		}

		if (!isPositiveArray(numbers))
			throw new IllegalArgumentException("at least one argument to HARMEAN() is not positive.");

		if (numbers.length < 2)
			throw new IllegalArgumentException("illegal arguments in call to HARMEAN(): must have at least 2 numbers.");

		double sum = 0.0;
		for (double d : numbers)
			sum += 1.0 / d;

		return numbers.length / sum;
	}

	private boolean isPositiveArray(final double[] numbers) {
		for (final double d : numbers) {
			if (d <= 0.0)
				return false;
		}

		return true;
	}
}
