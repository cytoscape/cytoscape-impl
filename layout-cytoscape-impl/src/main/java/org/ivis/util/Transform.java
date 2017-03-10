package org.ivis.util;

/**
 * This class is for transforming certain world coordinates to device ones.
 *  
 * Following example transformation translates (shifts) world coordinates by
 * (10,20), scales objects in the world to be twice as tall but half as wide
 * in device coordinates. In addition it flips the y coordinates.
 * 
 *			(wox,woy): world origin (x,y)
 *			(wex,wey): world extension x and y
 *			(dox,doy): device origin (x,y)
 *			(dex,dey): device extension x and y
 *
 *										(dox,doy)=(10,20)
 *											*--------- dex=50
 *											|
 *			 wey=50							|
 *				|							|
 *				|							|
 *				|							|
 *				*------------- wex=100		|
 *			(wox,woy)=(0,0)					dey=-100
 *
 * In most cases, we will set all values to 1.0 except dey=-1.0 to flip the y
 * axis.
 * 
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class Transform
{
// ---------------------------------------------------------------------
// Section: Instance variables.
// ---------------------------------------------------------------------
	
	/* World origin and extension */
	private double lworldOrgX;
	private double lworldOrgY;
	private double lworldExtX;
	private double lworldExtY;

	/* Device origin and extension */
	private double ldeviceOrgX;
	private double ldeviceOrgY;
	private double ldeviceExtX;
	private double ldeviceExtY;

// ---------------------------------------------------------------------
// Section: Constructors and initialization.
// ---------------------------------------------------------------------

	/**
	 * Default constructor.
	 */
	public Transform()
	{
		this.init();
	}

	/**
	 * This method initializes an object of this class.
	 */
	void init()
	{
		lworldOrgX = 0.0;
		lworldOrgY = 0.0;
		ldeviceOrgX = 0.0;
		ldeviceOrgY = 0.0;
		lworldExtX = 1.0;
		lworldExtY = 1.0;
		ldeviceExtX = 1.0;
		ldeviceExtY = 1.0;
	}

// ---------------------------------------------------------------------
// Section: Get/set methods for instance variables.
// ---------------------------------------------------------------------
	
	/* World related */
	
	public double getWorldOrgX()
	{
		return this.lworldOrgX;
	}

	public void setWorldOrgX(double wox)
	{
		this.lworldOrgX = wox;
	}
	
	public double getWorldOrgY()
	{
		return this.lworldOrgY;
	}

	public void setWorldOrgY(double woy)
	{
		this.lworldOrgY = woy;
	}
	
	public double getWorldExtX()
	{
		return this.lworldExtX;
	}

	public void setWorldExtX(double wex)
	{
		this.lworldExtX = wex;
	}
	
	public double getWorldExtY()
	{
		return this.lworldExtY;
	}

	public void setWorldExtY(double wey)
	{
		this.lworldExtY = wey;
	}

	/* Device related */
	
	public double getDeviceOrgX()
	{
		return this.ldeviceOrgX;
	}

	public void setDeviceOrgX(double dox)
	{
		this.ldeviceOrgX = dox;
	}
	
	public double getDeviceOrgY()
	{
		return this.ldeviceOrgY;
	}

	public void setDeviceOrgY(double doy)
	{
		this.ldeviceOrgY = doy;
	}
	
	public double getDeviceExtX()
	{
		return this.ldeviceExtX;
	}

	public void setDeviceExtX(double dex)
	{
		this.ldeviceExtX = dex;
	}
	
	public double getDeviceExtY()
	{
		return this.ldeviceExtY;
	}

	public void setDeviceExtY(double dey)
	{
		this.ldeviceExtY = dey;
	}	

// ---------------------------------------------------------------------
// Section: x or y coordinate transformation
// ---------------------------------------------------------------------

	/**
	 * This method transforms an x position in world coordinates to an x
	 * position in device coordinates.
	 */
	public double transformX(double x)
	{
		double xDevice;
		double worldExtX = this.lworldExtX;

		if (worldExtX != 0.0)
		{
			xDevice = this.ldeviceOrgX +
				((x - this.lworldOrgX) * this.ldeviceExtX / worldExtX);
		}
		else
		{
			xDevice = 0.0;
		}

		return(xDevice);
	}

	/**
	 * This method transforms a y position in world coordinates to a y
	 * position in device coordinates.
	 */
	public double transformY(double y)
	{
		double yDevice;
		double worldExtY = this.lworldExtY;

		if (worldExtY != 0.0)
		{
			yDevice = this.ldeviceOrgY +
				((y - this.lworldOrgY) * this.ldeviceExtY / worldExtY);
		}
		else
		{
			yDevice = 0.0;
		}

		return(yDevice);
	}

	/**
	 * This method transforms an x position in device coordinates to an x
	 * position in world coordinates.
	 */
	public double inverseTransformX(double x)
	{
		double xWorld;
		double deviceExtX = this.ldeviceExtX;

		if (deviceExtX != 0.0)
		{
			xWorld = this.lworldOrgX +
				((x - this.ldeviceOrgX) * this.lworldExtX / deviceExtX);
		}
		else
		{
			xWorld = 0.0;
		}

		return(xWorld);
	}

	/**
	 * This method transforms a y position in device coordinates to a y
	 * position in world coordinates.
	 */
	public double inverseTransformY(double y)
	{
		double yWorld;
		double deviceExtY = this.ldeviceExtY;

		if (deviceExtY != 0.0)
		{
			yWorld = this.lworldOrgY +
				((y - this.ldeviceOrgY) * this.lworldExtY / deviceExtY);
		}
		else
		{
			yWorld = 0.0;
		}

		return(yWorld);
	}

// ---------------------------------------------------------------------
// Section: point, dimension and rectagle transformation
// ---------------------------------------------------------------------

	/**
	 * This method transforms the input point from the world coordinate system
	 * to the device coordinate system.
	 */
	public PointD transformPoint(PointD inPoint)
	{
		PointD outPoint =
			new PointD(this.transformX(inPoint.x),
				this.transformY(inPoint.y));
		
		return(outPoint);
	}

	/**
	 * This method transforms the input dimension from the world coordinate 
	 * system to the device coordinate system.
	 */
	public DimensionD transformDimension(DimensionD inDimension)
	{
		DimensionD outDimension =
			new DimensionD(
				this.transformX(inDimension.width) -
					this.transformX(0.0),
				this.transformY(inDimension.height) -
					this.transformY(0.0));
		
		return outDimension;
	}

	/**
	 * This method transforms the input rectangle from the world coordinate
	 * system to the device coordinate system.
	 */
	public RectangleD transformRect(RectangleD inRect)
	{
		RectangleD outRect = new RectangleD();
		
		DimensionD inRectDim =
			new DimensionD(inRect.width, inRect.height);
		DimensionD outRectDim = this.transformDimension(inRectDim);
		outRect.setWidth(outRectDim.width);
		outRect.setHeight(outRectDim.height);
		
		outRect.setX(this.transformX(inRect.x));
		outRect.setY(this.transformY(inRect.y));
		
		return(outRect);
	}

	/**
	 * This method transforms the input point from the device coordinate system
	 * to the world coordinate system.
	 */
	public PointD inverseTransformPoint(PointD inPoint)
	{
		PointD outPoint =
			new PointD(this.inverseTransformX(inPoint.x),
				this.inverseTransformY(inPoint.y));
		
		return(outPoint);
	}

	/** 
	 * This method transforms the input dimension from the device coordinate 
	 * system to the world coordinate system.
	 */
	public DimensionD inverseTransformDimension(DimensionD inDimension)
	{ 
		DimensionD outDimension =
			new DimensionD(
				this.inverseTransformX(inDimension.width -
					this.inverseTransformX(0.0)),
				this.inverseTransformY(inDimension.height -
					this.inverseTransformY(0.0)));
		
		return(outDimension);
	}

	/**
	 * This method transforms the input rectangle from the device coordinate
	 * system to the world coordinate system. The result is in the passed 
	 * output rectangle object.
	 */
	public RectangleD inverseTransformRect(RectangleD inRect)
	{
		RectangleD outRect = new RectangleD();
		
		DimensionD inRectDim =
			new DimensionD(inRect.width, inRect.height);
		DimensionD outRectDim =
			this.inverseTransformDimension(inRectDim);
		outRect.setWidth(outRectDim.width);
		outRect.setHeight(outRectDim.height);
		
		outRect.setX(this.inverseTransformX(inRect.x));
		outRect.setY(this.inverseTransformY(inRect.y));
		
		return(outRect);
	}

// ---------------------------------------------------------------------
// Section: Remaining methods.
// ---------------------------------------------------------------------

	/**
	 * This method adjusts the world extensions of this transform object
	 * such that transformations based on this transform object will 
	 * preserve the aspect ratio of objects as much as possible.
	 */
	public void adjustExtToPreserveAspectRatio()
	{
		double deviceExtX = this.ldeviceExtX;
		double deviceExtY = this.ldeviceExtY;

		if (deviceExtY != 0.0 &&
			deviceExtX != 0.0)
		{
			double worldExtX = this.lworldExtX;
			double worldExtY = this.lworldExtY;

			if (deviceExtY * worldExtX < deviceExtX * worldExtY)
			{
				this.setWorldExtX((deviceExtY > 0.0) ?
					deviceExtX * worldExtY / deviceExtY :
					0.0);
			}
			else
			{
				this.setWorldExtY((deviceExtX > 0.0) ?
					deviceExtY * worldExtX / deviceExtX :
					0.0);
			}
		}
	}

	/**
	 * This method is for testing purposes only!
	 */
	public static void main(String[] args)
	{
		Transform trans = new Transform();
		
		trans.setWorldOrgX(0.0);
		trans.setWorldOrgY(0.0);
		trans.setWorldExtX(100.0);
		trans.setWorldExtY(50.0);
		
		trans.setDeviceOrgX(10.0);
		trans.setDeviceOrgY(20.0);
		trans.setDeviceExtX(50.0);
		trans.setDeviceExtY(-100.0);
		
		RectangleD rectWorld = new RectangleD();
		
		rectWorld.x = 12.0;
		rectWorld.y = -25.0;
		rectWorld.width = 150.0;
		rectWorld.height = 150.0;

		PointD pointWorld =
			new PointD(rectWorld.x, rectWorld.y);
		DimensionD dimWorld =
			new DimensionD(rectWorld.width, rectWorld.height);
		
		PointD pointDevice = trans.transformPoint(pointWorld);
		DimensionD dimDevice = trans.transformDimension(dimWorld);
		RectangleD rectDevice = trans.transformRect(rectWorld);
		
		// The transformed location & dimension should be (16,70) & (75,-300)
	}
}