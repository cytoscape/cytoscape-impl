package org.ivis.util;

/**
 * This class implements a double-precision point.
 *
 * @author Ugur Dogrusoz
 * @author Onur Sumer
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class PointD
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Geometry of point
	 */
	public double x;
	public double y;

// -----------------------------------------------------------------------------
// Section: Constructors and Initialization
// -----------------------------------------------------------------------------
	/**
	 * Empty constructor
	 */	
	public PointD()
	{
		this.x = 0.0;
		this.y = 0.0;
	}

	/**
	 * Constructor
	 */	
	public PointD(double x, double y)
	{
		this.x = x;
		this.y = y;		
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	public double getX() 
	{
		return x;
	}

	public void setX(double x) 
	{
		this.x = x;
	}

	public double getY() 
	{
		return y;
	}

	public void setY(double y) 
	{
		this.y = y;
	}

	
// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	public DimensionD getDifference(PointD pt)
	{
		return new DimensionD(this.x - pt.x, this.y - pt.y);
	}

	public PointD getCopy()
	{
		return new PointD(this.x, this.y);
	}

	public PointD translate(DimensionD dim)
	{
		this.x += dim.width;
		this.y += dim.height;
		
		return this;
	}
	
	
}