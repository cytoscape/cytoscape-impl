package org.ivis.util;

/**
 * This class implements general utility math methods.
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class IMath
{
// -----------------------------------------------------------------------------
// Section: Class methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns the sign of the input value.
	 */
	static public int sign(double value)
	{
		if (value > 0)
		{
			return 1;
		}
		else if (value < 0)
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
}