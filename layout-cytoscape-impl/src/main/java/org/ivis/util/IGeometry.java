package org.ivis.util;

import java.awt.geom.Line2D;
import java.awt.Point;
import java.util.ArrayList;

/**
 * This class maintains a list of static geometry related utility methods.
 *
 * @author Ugur Dogrusoz
 * @author Esat Belviranli
 * @author Shatlyk Ashyralyev
 * @author Can Cagdas Cengiz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
abstract public class IGeometry
{
// -----------------------------------------------------------------------------
// Section: Class methods
// -----------------------------------------------------------------------------

	/**
	 * This method calculates *half* the amount in x and y directions of the two
	 * input rectangles needed to separate them keeping their respective
	 * positioning, and returns the result in the input array. An input
	 * separation buffer added to the amount in both directions. We assume that
	 * the two rectangles do intersect.
	 */
	public static void calcSeparationAmount(RectangleD rectA,
		RectangleD rectB,
		double[] overlapAmount,
		double separationBuffer)
	{
		assert rectA.intersects(rectB);

		double[] directions = new double[2];

		IGeometry.decideDirectionsForOverlappingNodes(rectA, rectB, directions);
		
		overlapAmount[0] = Math.min(rectA.getRight(), rectB.getRight()) -
			Math.max(rectA.x, rectB.x);
		overlapAmount[1] = Math.min(rectA.getBottom(), rectB.getBottom()) -
			Math.max(rectA.y, rectB.y);
		
		// update the overlapping amounts for the following cases:
		
		if ( (rectA.getX() <= rectB.getX()) && (rectA.getRight() >= rectB.getRight()) )
		/* Case x.1:
		 *
		 * rectA
		 * 	|                       |
		 * 	|        _________      |
		 * 	|        |       |      |
		 * 	|________|_______|______|
		 * 			 |       |
		 *           |       |
		 *        rectB
		 */
		{
			overlapAmount[0] += Math.min((rectB.getX() - rectA.getX()),
				(rectA.getRight() - rectB.getRight()));
		}
		else if( (rectB.getX() <= rectA.getX()) && (rectB.getRight() >= rectA.getRight()))
		/* Case x.2:
		 *
		 * rectB
		 * 	|                       |
		 * 	|        _________      |
		 * 	|        |       |      |
		 * 	|________|_______|______|
		 * 			 |       |
		 *           |       |
		 *        rectA
		 */
		{
			overlapAmount[0] += Math.min((rectA.getX() - rectB.getX()),
				(rectB.getRight() - rectA.getRight()));
		}
		
		if ( (rectA.getY() <= rectB.getY()) && (rectA.getBottom() >= rectB.getBottom()) )
		/* Case y.1:
		 *          ________ rectA
		 *         |
		 *         |
		 *   ______|____  rectB
		 *         |    |
		 *         |    |
		 *   ______|____|
		 *         |
		 *         |
		 *         |________
		 *
		 */
		{
			overlapAmount[1] += Math.min((rectB.getY() - rectA.getY()),
				(rectA.getBottom() - rectB.getBottom()));
		}
		else if ((rectB.getY() <= rectA.getY()) && (rectB.getBottom() >= rectA.getBottom()) )
		/* Case y.2:
		 *          ________ rectB
		 *         |
		 *         |
		 *   ______|____  rectA
		 *         |    |
		 *         |    |
		 *   ______|____|
		 *         |
		 *         |
		 *         |________
		 *
		 */
		{
			overlapAmount[1] += Math.min((rectA.getY() - rectB.getY()),
				(rectB.getBottom() - rectA.getBottom()));
		}
		
		// find slope of the line passes two centers
        double slope =
			Math.abs((double)(rectB.getCenterY() - rectA.getCenterY()) /
        		(rectB.getCenterX() - rectA.getCenterX()));
        
        // if centers are overlapped
        if ((rectB.getCenterY() == rectA.getCenterY()) &&
			(rectB.getCenterX() == rectA.getCenterX()) )
        {
        	// assume the slope is 1 (45 degree)
        	slope = 1.0;
        }
        
		// change y
        double moveByY = slope * overlapAmount[0];
        // change x
        double moveByX =  overlapAmount[1] / slope;
        
        // now we have two pairs:
        // 1) overlapAmount[0], moveByY
        // 2) moveByX, overlapAmount[1]
     
        // use pair no:1
        if (overlapAmount[0] < moveByX)
        {
        	moveByX = overlapAmount[0];
        }
        // use pair no:2
        else
        {
        	moveByY = overlapAmount[1];
        }

		// return half the amount so that if each rectangle is moved by these
		// amounts in opposite directions, overlap will be resolved
		
        overlapAmount[0] = -1 * directions[0] * ((moveByX / 2) + separationBuffer);
        overlapAmount[1] = -1 * directions[1] * ((moveByY / 2) + separationBuffer);
	}

	/**
     * This method decides the separation direction of overlapping nodes
     * 
     * if directions[0] = -1, then rectA goes left
     * if directions[0] = 1,  then rectA goes right
     * if directions[1] = -1, then rectA goes up
     * if directions[1] = 1,  then rectA goes down
     */
    private static void decideDirectionsForOverlappingNodes(RectangleD rectA,
		RectangleD rectB,
		double[] directions)
    {
    	if (rectA.getCenterX() < rectB.getCenterX())
    	{
    		directions[0] = -1;
    	}
    	else
    	{
    		directions[0] = 1;
    	}
    	
    	if (rectA.getCenterY() < rectB.getCenterY())
    	{
    		directions[1] = -1;
    	}
    	else
    	{
    		directions[1] = 1;
    	}
    }
    
	/**
	 * This method calculates the intersection (clipping) points of the two
	 * input rectangles with line segment defined by the centers of these two
	 * rectangles. The clipping points are saved in the input double array and
	 * whether or not the two rectangles overlap is returned.
	 */
	public static boolean getIntersection(RectangleD rectA,
		RectangleD rectB,
		double[] result)
	{
		//result[0-1] will contain clipPoint of rectA, result[2-3] will contain clipPoint of rectB

		double p1x = rectA.getCenterX();
		double p1y = rectA.getCenterY();		
		double p2x = rectB.getCenterX();
		double p2y = rectB.getCenterY();
		
		//if two rectangles intersect, then clipping points are centers
		if (rectA.intersects(rectB))
		{
			result[0] = p1x;
			result[1] = p1y;
			result[2] = p2x;
			result[3] = p2y;
			return true;
		}
		
		//variables for rectA
		double topLeftAx = rectA.getX();
		double topLeftAy = rectA.getY();
		double topRightAx = rectA.getRight();
		double bottomLeftAx = rectA.getX();
		double bottomLeftAy = rectA.getBottom();
		double bottomRightAx = rectA.getRight();
		double halfWidthA = rectA.getWidthHalf();
		double halfHeightA = rectA.getHeightHalf();
		
		//variables for rectB
		double topLeftBx = rectB.getX();
		double topLeftBy = rectB.getY();
		double topRightBx = rectB.getRight();
		double bottomLeftBx = rectB.getX();
		double bottomLeftBy = rectB.getBottom();
		double bottomRightBx = rectB.getRight();
		double halfWidthB = rectB.getWidthHalf();
		double halfHeightB = rectB.getHeightHalf();

		//flag whether clipping points are found
		boolean clipPointAFound = false;
		boolean clipPointBFound = false;
		

		// line is vertical
		if (p1x == p2x)
		{
			if(p1y > p2y)
			{
				result[0] = p1x;
				result[1] = topLeftAy;
				result[2] = p2x;
				result[3] = bottomLeftBy;
				return false;
			}
			else if(p1y < p2y)
			{
				result[0] = p1x;
				result[1] = bottomLeftAy;
				result[2] = p2x;
				result[3] = topLeftBy;
				return false;
			}
			else
			{
				//not line, return null;
			}
		}
		// line is horizontal
		else if (p1y == p2y)
		{
			if(p1x > p2x)
			{
				result[0] = topLeftAx;
				result[1] = p1y;
				result[2] = topRightBx;
				result[3] = p2y;
				return false;
			}
			else if(p1x < p2x)
			{
				result[0] = topRightAx;
				result[1] = p1y;
				result[2] = topLeftBx;
				result[3] = p2y;
				return false;
			}
			else
			{
				//not valid line, return null;
			}
		}
		else
		{
			//slopes of rectA's and rectB's diagonals
			double slopeA = rectA.height / rectA.width;
			double slopeB = rectB.height / rectB.width;
			
			//slope of line between center of rectA and center of rectB
			double slopePrime = (p2y - p1y) / (p2x - p1x);
			int cardinalDirectionA;
			int cardinalDirectionB;
			double tempPointAx;
			double tempPointAy;
			double tempPointBx;
			double tempPointBy;
			
			//determine whether clipping point is the corner of nodeA
			if((-slopeA) == slopePrime)
			{
				if(p1x > p2x)
				{
					result[0] = bottomLeftAx;
					result[1] = bottomLeftAy;
					clipPointAFound = true;
				}
				else
				{
					result[0] = topRightAx;
					result[1] = topLeftAy;
					clipPointAFound = true;
				}
			}
			else if(slopeA == slopePrime)
			{
				if(p1x > p2x)
				{
					result[0] = topLeftAx;
					result[1] = topLeftAy;
					clipPointAFound = true;
				}
				else
				{
					result[0] = bottomRightAx;
					result[1] = bottomLeftAy;
					clipPointAFound = true;
				}
			}
			
			//determine whether clipping point is the corner of nodeB
			if((-slopeB) == slopePrime)
			{
				if(p2x > p1x)
				{
					result[2] = bottomLeftBx;
					result[3] = bottomLeftBy;
					clipPointBFound = true;
				}
				else
				{
					result[2] = topRightBx;
					result[3] = topLeftBy;
					clipPointBFound = true;
				}
			}
			else if(slopeB == slopePrime)
			{
				if(p2x > p1x)
				{
					result[2] = topLeftBx;
					result[3] = topLeftBy;
					clipPointBFound = true;
				}
				else
				{
					result[2] = bottomRightBx;
					result[3] = bottomLeftBy;
					clipPointBFound = true;
				}
			}
			
			//if both clipping points are corners
			if(clipPointAFound && clipPointBFound)
			{
				return false;
			}
			
			//determine Cardinal Direction of rectangles
			if(p1x > p2x)
			{
				if(p1y > p2y)
				{
					cardinalDirectionA = getCardinalDirection(slopeA, slopePrime, 4);
					cardinalDirectionB = getCardinalDirection(slopeB, slopePrime, 2);
				}
				else
				{
					cardinalDirectionA = getCardinalDirection(-slopeA, slopePrime, 3);
					cardinalDirectionB = getCardinalDirection(-slopeB, slopePrime, 1);
				}
			}
			else
			{
				if(p1y > p2y)
				{
					cardinalDirectionA = getCardinalDirection(-slopeA, slopePrime, 1);
					cardinalDirectionB = getCardinalDirection(-slopeB, slopePrime, 3);
				}
				else
				{
					cardinalDirectionA = getCardinalDirection(slopeA, slopePrime, 2);
					cardinalDirectionB = getCardinalDirection(slopeB, slopePrime, 4);
				}
			}
			//calculate clipping Point if it is not found before
			if(!clipPointAFound)
			{
				switch(cardinalDirectionA)
				{
					case 1:
						tempPointAy = topLeftAy;
						tempPointAx = p1x + ( -halfHeightA ) / slopePrime;
						result[0] = tempPointAx;
						result[1] = tempPointAy;
						break;
					case 2:
						tempPointAx = bottomRightAx;
						tempPointAy = p1y + halfWidthA * slopePrime;
						result[0] = tempPointAx;
						result[1] = tempPointAy;
						break;
					case 3:
						tempPointAy = bottomLeftAy;
						tempPointAx = p1x + halfHeightA / slopePrime;
						result[0] = tempPointAx;
						result[1] = tempPointAy;
						break;
					case 4:
						tempPointAx = bottomLeftAx;
						tempPointAy = p1y + ( -halfWidthA ) * slopePrime;
						result[0] = tempPointAx;
						result[1] = tempPointAy;
						break;
				}
			}
			if(!clipPointBFound)
			{
				switch(cardinalDirectionB)
				{
					case 1:
						tempPointBy = topLeftBy;
						tempPointBx = p2x + ( -halfHeightB ) / slopePrime;
						result[2] = tempPointBx;
						result[3] = tempPointBy;
						break;
					case 2:
						tempPointBx = bottomRightBx;
						tempPointBy = p2y + halfWidthB * slopePrime;
						result[2] = tempPointBx;
						result[3] = tempPointBy;
						break;
					case 3:
						tempPointBy = bottomLeftBy;
						tempPointBx = p2x + halfHeightB / slopePrime;
						result[2] = tempPointBx;
						result[3] = tempPointBy;
						break;
					case 4:
						tempPointBx = bottomLeftBx;
						tempPointBy = p2y + ( -halfWidthB ) * slopePrime;
						result[2] = tempPointBx;
						result[3] = tempPointBy;
						break;
				}
			}
			
		}
		
		return false;
	}
	
	/**
	 * This method returns in which cardinal direction does input point stays
	 * 1: North
	 * 2: East
	 * 3: South
	 * 4: West
	 */
	private static int getCardinalDirection(double slope,
		double slopePrime,
		int line)
	{
		if (slope > slopePrime)
		{
			return line;
		}
		else
		{
			return 1 + line % 4;
		}
	}
	
	/**
	 * This method calculates the intersection of the two lines defined by
	 * point pairs (s1,s2) and (f1,f2).
	 */
	public static Point getIntersection(Point s1, Point s2, Point f1, Point f2)
	{
		int x1 = s1.x;
		int y1 = s1.y;
		int x2 = s2.x;
		int y2 = s2.y;
		int x3 = f1.x;
		int y3 = f1.y;
		int x4 = f2.x;
		int y4 = f2.y;

		int x, y; // intersection point

		int a1, a2, b1, b2, c1, c2; // coefficients of line eqns.

		int denom;

		a1 = y2 - y1;
		b1 = x1 - x2;
		c1 = x2 * y1 - x1 * y2;  // { a1*x + b1*y + c1 = 0 is line 1 }

		a2 = y4 - y3;
		b2 = x3 - x4;
		c2 = x4 * y3 - x3 * y4;  // { a2*x + b2*y + c2 = 0 is line 2 }

		denom = a1 * b2 - a2 * b1;

		if (denom == 0)
		{
			return null;
		}

		x = (b1 * c2 - b2 * c1) / denom;
		y = (a2 * c1 - a1 * c2) / denom;

		return new Point(x, y);
	}

	/**
	 * This method finds and returns the angle of the vector from the + x-axis
	 * in clockwise direction (compatible w/ Java coordinate system!).
	 */
	public static double angleOfVector(double Cx, double Cy,
		double Nx, double Ny)
	{
		double C_angle;

		if (Cx != Nx)
		{
			C_angle = Math.atan((Ny - Cy) / (Nx - Cx));

			if (Nx < Cx)
			{
				C_angle += Math.PI;
			}
			else if (Ny < Cy)
			{
				C_angle += TWO_PI;
			}
		}
		else if (Ny < Cy)
		{
			C_angle = ONE_AND_HALF_PI; // 270 degrees
		}
		else
		{
			C_angle = HALF_PI; // 90 degrees
		}

//		assert 0.0 <= C_angle && C_angle < TWO_PI;

		return C_angle;
	}

	/**
	 * This method converts the given angle in radians to degrees.
	 */
	public static double radian2degree(double rad)
	{
		return 180.0 * rad / Math.PI;
	}

	/**
	 * This method checks whether the given two line segments (one with point
	 * p1 and p2, the other with point p3 and p4) intersect at a point other
	 * than these points.
	 */
	public static boolean doIntersect(PointD p1, PointD p2,
		PointD p3, PointD p4)
	{
		boolean result = Line2D.linesIntersect(p1.x, p1.y,
			p2.x, p2.y, p3.x, p3.y,
			p4.x, p4.y);

		return result;
	}

	/**
	* Given a vector as a PointD object, returns the normalized form of
	* the vector in [-1, 1] scale.  
	*/
	public static PointD normalizeVector(PointD v)
	{
		double denom = (v.x * v.x) + (v.y * v.y);
		denom = Math.sqrt(denom);
		double x = v.x / denom;
		double y = v.y / denom;
		return new PointD(x, y);		
	}
	
	/**
	* Returns the projection of a given vector on the x-y 
	* Cartesian plane. 
	*/
	public static PointD getXYProjection(double magnitude, PointD direction)
	{
		double sin = direction.y/(Math.sqrt((direction.x*direction.x)+(direction.y*direction.y)));
		double cos = direction.x/(Math.sqrt((direction.x*direction.x)+(direction.y*direction.y)));
		double x = magnitude * cos;
		double y = magnitude * sin;
		return new PointD(x,y);
	}
	
	/**
	 * This method is a dot product operator
	 */
	public static double dot(PointD p1, PointD p2)
	{
	    return p1.x*p2.x + p1.y*p2.y;
	}
	
	/**
	* Gather up one-dimensional extents of the projection of the polygon
	* onto this axis.
	*/
	public static double [] gatherPolygonProjectionExtents( ArrayList<PointD> p, PointD v) 
	{
	 
		double [] out = new double [2];
		
	    // Initialize extents to a single point, the first vertex
	    out[0] = IGeometry.dot(v, p.get(0));	// min
	    out[1] = IGeometry.dot(v, p.get(0));	// max
	    
	    // Now scan all the rest, growing extents to include them
	    for (int i = 1; i < p.size(); ++i)
	    {
	        double d = IGeometry.dot(v, p.get(i));
	        
	        if (d < out[0]) 
	        	out[0] = d;
	        else if (d > out[1]) 
	        	out[1] = d;
	    }
	    
	    
	    return out;
	}
	
	/**
	 * This method tests if two convex polygons overlap. 
	 * Method is based on the separating axis theorem. It only
	 * uses only the edges of the first polygon (polygon "p1")
	 * to build the list of candidate separating axes.
	*/
	public static Object [] findSeparatingAxis (ArrayList<PointD> p1, 
										ArrayList<PointD> p2) 
	{
		Object [] overlapInfo;
		
		int aVertCount;
		int bVertCount;
		
		double minOverlap = Double.NEGATIVE_INFINITY;
		PointD minVector = new PointD(0,0);
		overlapInfo = new Object [2];
		
		aVertCount = p1.size();
		bVertCount = p2.size();
		
		
	    // Iterate over all the edges
		int prev = aVertCount-1;
	    
		for (int cur = 0 ; cur < aVertCount ; ++cur)
	    {
	 
	        // Get edge vector.  
	        PointD edge; 
	        double ex = p1.get(cur).x - p1.get(prev).x;
	        double ey = p1.get(cur).y - p1.get(prev).y;
	        edge = new PointD(ex, ey);
	       
	        // Rotate vector 90 degrees (doesn't matter which way) to get
	        // candidate separating axis.
	        PointD v;
	        double vx = edge.y;
	        double vy = -edge.x;
	        v = IGeometry.normalizeVector(new PointD(vx,vy));
			
	        if ((v.y < 0.0))
			{				
				v.x = -(v.x);
				v.y = -(v.y);				
			}
	        	        
	        // Gather extents of both polygons projected onto this axis
	        double [] p1Bounds; 
	        double [] p2Bounds;
	        p1Bounds = IGeometry.gatherPolygonProjectionExtents(p1, v);
	        p2Bounds = IGeometry.gatherPolygonProjectionExtents(p2, v);
	 	        
	        // Is this a separating axis?
	        if (p1Bounds[1] < p2Bounds[0]) 
	        {
	        	
	        	overlapInfo[0] = 0.0;
	        	overlapInfo[1] = minVector;
	        	return overlapInfo;
	        }	        	
	        else
	        {
	        	// negative overlap
	        	double overlap;
	        	overlap = p2Bounds[0] - p1Bounds[1] ; 
	        	if (Math.abs(overlap) < Math.abs(minOverlap))
	        	{
	        		minVector = v;
	        		minOverlap = overlap;	        		
	        	}
	        }
	        
	        if (p2Bounds[1] < p1Bounds[0])
	        {		        	
	        	overlapInfo[0] = 0.0;
	        	overlapInfo[1] = minVector;
	        	return overlapInfo;        	
	        }
	        else
	        {
	        	// positive overlap
	        	double overlap;
	        	overlap = p2Bounds[1] - p1Bounds[0]; 
	        	if (Math.abs(overlap) < Math.abs(minOverlap))
	        	{
	        		minVector = v;
	        		minOverlap = overlap;
	        	}	
	        }
	        // Next edge
	        prev = cur;
	    }
		
		// Failed to find a separating axis
		overlapInfo[0] = minOverlap;
    	overlapInfo[1] = minVector;
    	
    	return overlapInfo;	    
	}	
	
	/**
	 * This method returns false if there is a separating axis 
	 * between the polygons. If there is no separating axis, 
	 * method returns true. 
	 */	
	public static Object [] convexPolygonOverlap (ArrayList<PointD> p1, 
			ArrayList<PointD> p2) 
	{		
		Object [] overlapInfo1;
		Object [] overlapInfo2;
		
		// Using P1's edges for a separating axis
		overlapInfo1 = IGeometry.findSeparatingAxis(p1, p2);
		if ((double) overlapInfo1[0] == 0.0)
			return overlapInfo1;
		
		// Now swap roles, and use P2's edges
		overlapInfo2 = IGeometry.findSeparatingAxis(p2, p1);
		if ((double) overlapInfo2[0] == 0.0)
			return overlapInfo2;
		
		// No separating axis found.  They must overlap.
		// Return the minimum magnitude vector.
		
		if (Math.abs((double) overlapInfo1[0]) < Math.abs((double) overlapInfo2[0]))
		{		
			overlapInfo1[0] = -((double) overlapInfo1[0]);			
			return overlapInfo1;
		}
		else
		{				
			return overlapInfo2;
		}
	}
	
//	public static void calcPolygonSeparationAmount(ArrayList<PointD> p1,
//			ArrayList<PointD> p2, 
//			double [] overlapAmount)
//	{
//		Object [] info = convexPolygonOverlap(p1, p2);
//		double minOverlap = (double) info[0];
//		
//		if(minOverlap > Double.NEGATIVE_INFINITY)
//		{
//			System.out.println("minoverlap: "+minOverlap);
//			PointD minVector = (PointD) info[1];
//
//			PointD temp = IGeometry.getXYProjection(minOverlap,
//				minVector);
//		
//			overlapAmount[0] = temp.x;			
//			overlapAmount[1] = temp.y;
//		}
//		else
//		{
//			overlapAmount[0] = 0.0;
//			overlapAmount[1] = 0.0;
//		}
//		System.out.println("overlap igeo: "+overlapAmount[0]);
//	}
	
	// TODO may not produce correct test results, since parameter order of
	// RectangleD constructor is changed
	private static void testClippingPoints()
	{
		RectangleD rectA = new RectangleD(5, 6, 2, 4);
		RectangleD rectB;
		
		rectB = new RectangleD(0, 4, 1, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(1, 4, 1, 2);
		findAndPrintClipPoints(rectA, rectB);
	
		rectB = new RectangleD(1, 3, 3, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------		
		rectB = new RectangleD(2, 3, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(3, 3, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(3, 2, 4, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------		
		rectB = new RectangleD(6, 3, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------		
		rectB = new RectangleD(9, 2, 4, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(9, 3, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(8, 3, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new RectangleD(11, 3, 3, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(11, 4, 1, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(10, 4, 1, 4);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new RectangleD(10, 5, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(9, 4.5, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(10, 5.8, 0.4, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------		
		rectB = new RectangleD(11, 6, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new RectangleD(10, 7.8, 0.4, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(9, 7.5, 1, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(10, 7, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new RectangleD(10, 9, 2, 6);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(11, 9, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(12, 8, 4, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new RectangleD(7, 9, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(8, 9, 4, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(10, 9, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new RectangleD(6, 10, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new RectangleD(3, 8, 4, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(3, 9, 2, 2);
		findAndPrintClipPoints(rectA, rectB);

		rectB = new RectangleD(2, 8, 4, 4);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new RectangleD(2, 8, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(1, 8, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
	
		rectB = new RectangleD(1, 8.5, 1, 4);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new RectangleD(3, 7, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(1, 7.5, 1, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(3, 7.8, 0.4, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new RectangleD(1, 6, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------		
		rectB = new RectangleD(3, 5.8, 0.4, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(1, 5, 1, 3);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(1, 4, 3, 3);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new RectangleD(4, 4, 3, 3);
//		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new RectangleD(5, 6, 2, 4);
//		findAndPrintClipPoints(rectA, rectB);
	}
	
	private static void findAndPrintClipPoints(RectangleD rectA, RectangleD rectB)
	{
		System.out.println("---------------------");
		double[] clipPoints = new double[4];
		
		System.out.println("RectangleA  X: " + rectA.x + "  Y: " + rectA.y + "  Width: " + rectA.width + "  Height: " + rectA.height);
		System.out.println("RectangleB  X: " + rectB.x + "  Y: " + rectB.y + "  Width: " + rectB.width + "  Height: " + rectB.height);
		IGeometry.getIntersection(rectA, rectB, clipPoints);

		System.out.println("Clip Point of RectA X:" + clipPoints[0] + " Y: " + clipPoints[1]);
		System.out.println("Clip Point of RectB X:" + clipPoints[2] + " Y: " + clipPoints[3]);	
	}

	/**
	 * This method takes two line segments (two points for each line) 
	 * and calculates their intersection. If they do not intersect 
	 * it returns a point with coordinates (-1, -1).
	 */		
	public static PointD findIntersectionOfTwoLineSegments(PointD p1A, 
			PointD p2A, PointD p1B, PointD p2B)
	{
		// variables to use
		PointD intersectionPt = new PointD();
		double slopeA;
		double slopeB;
		double constantA;
		double constantB; 
		
		// calculate equations of lines A and B
		slopeA = (p2A.y - p1A.y) / (p2A.x - p1A.x);
		slopeB = (p2B.y - p1B.y) / (p2B.x - p1B.x);
		constantA = p1A.y - (slopeA * p1A.x);
		constantB = p1B.y - (slopeB * p1B.x);
		//System.out.println("NumA:"+(p2A.y - p1A.y) + " DenomA:"+ (p2A.x - p1A.x)+ " NumB:"+(p2B.y - p1B.y)+" DenomB:"+(p2B.x - p1B.x));
		//System.out.println("p2A.y:"+p2A.y+" p1A.y:"+p1A.y+" p2A.x:"+p2A.x+" p1A.x"+p1A.x);
		//System.out.println("Slopes are: "+slopeA + "," + slopeB);
		if (slopeA == Double.NEGATIVE_INFINITY || slopeA == Double.POSITIVE_INFINITY)
		{
			intersectionPt.x = p1A.x;
			intersectionPt.y = (slopeB * p1A.x) + constantB;
		}
		else if (slopeB == Double.NEGATIVE_INFINITY || 
				slopeB == Double.POSITIVE_INFINITY)
		{
			intersectionPt.x = p1B.x;
			intersectionPt.y = (slopeA * p1B.x) + constantA;
		}
		else if (Double.isNaN(slopeA) || Double.isNaN(slopeB))
		{
			return new PointD(-1,-1);
		}
		else
		{
		// calculate intersection point for lines A and B
			intersectionPt.x = (constantB - constantA) / 
					(slopeA - slopeB);

			intersectionPt.y = (slopeA * intersectionPt.x) +
					constantA;
		}
		// check if the intersection point is an element 
		// of the line segment
		if (isPointOnLineSegment(p1A, p2A, intersectionPt))
		{
			if (isPointOnLineSegment(p1B, p2B, intersectionPt))
			{
				return intersectionPt;
			}
		}
		/*
		if (((p1A.x >= intersectionPt.x) && (p2A.x <= intersectionPt.x)) || 
				((p1A.x <= intersectionPt.x) && (p2A.x >= intersectionPt.x)))
		{			
			if (((p1A.y >= intersectionPt.y) && (p2A.y <= intersectionPt.y)) || 
					((p1A.y <= intersectionPt.y) && (p2A.y >= intersectionPt.y)))
			{
				return intersectionPt;
			}
		}
		*/
		// if the lines do not intersect, return (-1,-1)
		intersectionPt.x = -1.0;
		intersectionPt.y = -1.0;
		return intersectionPt;
	}
	
	/**
	 * This method takes an ArrayList of polygon points that is sorted
	 * according to adjacent edges and two points for a line segment to 
	 * calculate their intersection. If they do not intersect it returns 
	 * a point with coordinates (-1, -1).
	 */	
	public static boolean getPolygonIntersection (ArrayList<PointD> polygonA,
			ArrayList<PointD> polygonB, double[] result)
	{
		// 
		PointD clipPtA;
		PointD clipPtB;
		result[0] = -1.0;
		result[1] = -1.0;
		result[2] = -1.0;
		result[3] = -1.0;
		Object [] overlap;
		
		if (polygonA.size() > 3 && polygonB.size() > 3)
		{
			PointD centerA = getPolygonCenter(polygonA);
			PointD centerB = getPolygonCenter(polygonB);
			/*
			overlap = convexPolygonOverlap(polygonA, polygonB);
			//if two polygon intersect, then clipping points are centers
			if ((double) overlap[0] == 0)
			{
				result[0] = centerA.x;
				result[1] = centerA.y;
				result[2] = centerB.x;
				result[3] = centerB.y;
				return false;
			}
			*/
			clipPtA = findIntersectionOfTwoLineSegments
					(centerA, centerB, 
							polygonA.get(0), polygonA.get(polygonA.size()-1));
			
			for (int i = 1; i < polygonA.size(); i++)
			{	
				if (clipPtA.x != -1.0 && clipPtA.y != -1.0)
				{
					break;
				}
				clipPtA = findIntersectionOfTwoLineSegments
						(centerA, centerB, 
								polygonA.get(i-1), polygonA.get(i));				
			}
			
			clipPtB = findIntersectionOfTwoLineSegments
					(centerA, centerB, 
							polygonB.get(0), polygonB.get(polygonB.size()-1));
			
			for (int i = 1; i < polygonB.size(); i++)
			{	
				if (clipPtB.x != -1.0 && clipPtB.y != -1.0)
				{
					break;
				}
				clipPtB = findIntersectionOfTwoLineSegments
						(centerA, centerB, 
								polygonB.get(i-1), polygonB.get(i));				
			}
			
			result[0] = clipPtA.x;
			result[1] = clipPtA.y;
			result[2] = clipPtB.x;
			result[3] = clipPtB.y;
			
			// if the polygons are overlapping, then the clip points are the centers
			if ((result[0] == -1.0 && result[1] == -1.0)
					|| (result[2] == -1.0 && result[3] == -1.0))
			{
				result[0] = centerA.x;
				result[1] = centerA.y;
				result[2] = centerB.x;
				result[3] = centerB.y;	
				return true; // TEST true
			}

			// Test
			/*
			System.out.println("CLIPPING POINTS");
			System.out.println("PolygonA:");
			for (Object o : polygonA)
			{
				PointD p = (PointD) o;
				System.out.print(" (" + p.x + "," + p.y+ "),");
			}
			System.out.println();
			System.out.println("PolygonB:");
			for (Object o : polygonB)
			{
				PointD p = (PointD) o;
				System.out.print(" (" + p.x + "," + p.y+ "),");
			}
			System.out.println();
			System.out.println("Clipping Points:");
			System.out.println(result[0] + "," + result[1] + " and " +result[2]+","+result[3]);*/
			return false; //may be true //TEST
		}
		else
		{
			System.out.println("What am i doing here?"); //test
			return false;
		}
	}	

	/**
	 * This method takes an ArrayList of polygon points and calculates its 
	 * center. Returns a point that is the position of the center.
	 * The algorithm is based on the explanations in the following link.
	 * http://paulbourke.net/geometry/polygonmesh/
	 */	
	public static PointD getPolygonCenter(ArrayList<PointD> polygon)
	{
		double [] centroid = {0, 0};
		double signedArea = 0.0;
		double x0 = 0.0; // Current vertex X
		double y0 = 0.0; // Current vertex Y
		double x1 = 0.0; // Next vertex X
		double y1 = 0.0; // Next vertex Y
		double a = 0.0;  // Partial signed area

		// For all vertices except last
		if (polygon.size() < 3)
		{
			System.out.println("Not a valid polygon");
		}
		else
		{
			for (int i = 0; i < polygon.size() - 1; i++)
			{
				x0 = polygon.get(i).x;
			    y0 = polygon.get(i).y;
			    x1 = polygon.get(i + 1).x;
			    y1 = polygon.get(i + 1).y;
			    a = (x0 * y1) - (x1 * y0);
			    signedArea += a;
			    centroid[0] += (x0 + x1) * a;
			    centroid[1] += (y0 + y1) * a;
			}
	
			// Do last vertex
			x0 = polygon.get(polygon.size() - 1).x;
			y0 = polygon.get(polygon.size() - 1).y;
			x1 = polygon.get(0).x;
			y1 = polygon.get(0).y;
			
			a = (x0 * y1) - (x1 * y0);
			signedArea += a;
			centroid[0] += (x0 + x1)*a;
			centroid[1] += (y0 + y1)*a;
	
			signedArea *= 0.5;
			centroid[0] /= (6.0 * signedArea);
			centroid[1] /= (6.0 * signedArea);
		}

		return new PointD (centroid[0],centroid[1]);
	}
	
	public static boolean isPointOnLineSegment(PointD linePointA, PointD linePointB, PointD point) 
	{	
		double ac = Math.sqrt(((linePointA.x - point.x) * (linePointA.x - point.x)) + 
				((linePointA.y - point.y) * (linePointA.y - point.y)));
		double bc = Math.sqrt(((linePointB.x - point.x) * (linePointB.x - point.x)) + 
				((linePointB.y - point.y) * (linePointB.y - point.y)));
		double ab = Math.sqrt(((linePointA.x - linePointB.x) * (linePointA.x - linePointB.x)) + 
				((linePointA.y - linePointB.y) * (linePointA.y - linePointB.y)));
		
		if (Math.abs(ac + bc - ab) < TOLERANCE)
			return true;
		else
			return false;
	}
	
	public static void testLineSegment()
	{
		PointD linePointA, linePointB, point;
/*		
		linePointA = new PointD(4,2);
		linePointB = new PointD(1,2);
		point = new PointD(2.5, 2);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
		
		point = new PointD(0, 2);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
		
		point = new PointD(6, 2);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
		
		point = new PointD(3, 4);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));


		linePointA = new PointD(1,1);
		linePointB = new PointD(6,3);
		point = new PointD(3.5, 2);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
		
		point = new PointD(11, 5);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
		
		point = new PointD(4, 1);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
		
		point = new PointD(4, 4);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
		
		*/
		
		linePointA = new PointD(2,2);
		linePointB = new PointD(2,4);
		point = new PointD(2, 3);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
		
		point = new PointD(2, 3.5);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
		
		point = new PointD(2, 1);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
		
		point = new PointD(2, 5);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
				
		point = new PointD(5, 5);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
				
		point = new PointD(5, 3);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
				
		point = new PointD(2, 4);
		System.out.println(isPointOnLineSegment(linePointA, linePointB, point));
				
		PointD intersect, center;
		
		intersect = findIntersectionOfTwoLineSegments(new PointD(0,1), new PointD(4,5), new PointD(2,1), new PointD(2,5));
		System.out.println(intersect.x + ", " + intersect.y);
		
		ArrayList<PointD> polygon = new ArrayList<PointD>();
		polygon.add(new PointD(2,2));
		polygon.add(new PointD(2,4)); 
		polygon.add(new PointD(4,4));		
		polygon.add(new PointD(4,2)); 

		
		center = getPolygonCenter(polygon);
		System.out.println(center.x + ", " + center.y);
		
		
		double [] result = {0.0,0.0,0.0,0.0};
		ArrayList<PointD> p1 = new ArrayList<PointD>();
		ArrayList<PointD> p2 = new ArrayList<PointD>();
		ArrayList<PointD> p3 = new ArrayList<PointD>();
		
		p1.add(new PointD(2,2));
		p1.add(new PointD(1,3));
		p1.add(new PointD(2,4));
		p1.add(new PointD(4,4));
		p1.add(new PointD(5,3));
		p1.add(new PointD(4,2));
		
		p2.add(new PointD(3,3));
		p2.add(new PointD(3,5));
		p2.add(new PointD(5,5));
		p2.add(new PointD(5,3));
		
		p3.add(new PointD(6,4));
		p3.add(new PointD(6,6));
		p3.add(new PointD(8,6));
		p3.add(new PointD(8,4));
		
		getPolygonIntersection(p1, p2, result);
		System.out.println("[0]:"+result[0]+" [1]:"+result[1] +" [2]:"+result[2]+ " [3]:"+result[3]);
		
		getPolygonIntersection(p1, p3, result);
		System.out.println("[0]:"+result[0]+" [1]:"+result[1] +" [2]:"+result[2]+ " [3]:"+result[3]);
	}
	
	// ****** Following method is used in SBGN-PD Layout *******
	/**
	 * Calculates the angle between 3 points in given order. Returns its
	 * absolute value.
	 */
	public static double calculateAngle(PointD targetPnt, PointD centerPnt,
			PointD node)
	{

		PointD point1 = new PointD(targetPnt.x - centerPnt.x, targetPnt.y
				- centerPnt.y);
		PointD point2 = new PointD(node.x - centerPnt.x, node.y - centerPnt.y);

		if (Math.abs(point1.x) < 0)
			point1.x = 0.0001;
		if (Math.abs(point1.y) < 0)
			point1.y = 0.0001;

		double angleValue = (point1.x * point2.x + point1.y * point2.y)
				/ (Math.sqrt(point1.x * point1.x + point1.y * point1.y) * Math
						.sqrt(point2.x * point2.x + point2.y * point2.y));

		return Math.abs(Math.toDegrees(Math.acos(angleValue)));
	}
	
	/*
	 * Main method for testing purposes.
	 */
	public static void main(String [] args)
	{
		//testClippingPoints();	
		testLineSegment();
	}

// -----------------------------------------------------------------------------
// Section: Class Constants
// -----------------------------------------------------------------------------
	/**
	 * Some useful pre-calculated constants
	 */
	public static final double HALF_PI = 0.5 * Math.PI;
	public static final double ONE_AND_HALF_PI = 1.5 * Math.PI;
	public static final double TWO_PI = 2.0 * Math.PI;
	public static final double THREE_PI = 3.0 * Math.PI;
	public static final double TOLERANCE = 0.00001;
}