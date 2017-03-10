package org.ivis.util;

/**
 * This class implements a double-precision dimension.
 *
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class DimensionD
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Geometry of dimension
	 */
	public double width;
	public double height;

// -----------------------------------------------------------------------------
// Section: Constructors and Initialization
// -----------------------------------------------------------------------------
	/**
	 * Empty constructor
	 */
	public DimensionD()
	{
		this.height = 0;
		this.width = 0;
	}

	/**
	 * Constructor
	 */
	public DimensionD(double width, double height)
	{
		this.height = height;
		this.width = width;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	public double getWidth()
	{
		return width;
	}

	public void setWidth(double width)
	{
		this.width = width;
	}

	public double getHeight()
	{
		return height;
	}

	public void setHeight(double height)
	{
		this.height = height;
	}
}