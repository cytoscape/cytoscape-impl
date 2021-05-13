package org.cytoscape.graph.render.immed.nodeshape;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2009 - 2021 The Cytoscape Consortium
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


import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;


/**
 * An abstract implementation of NodeShape that provides a basic way of computing
 * the edge intersection. 
 */
abstract class AbstractNodeShape implements NodeShape {
	private final AffineTransform at = new AffineTransform();
	private final CubicCurve2D cubic = new CubicCurve2D.Float(0f,0f,0.3f,0.3f,0.7f,0.7f,1f,1f);
	private final QuadCurve2D quad = new QuadCurve2D.Float(0f,0f,0.5f,0.5f,1f,1f);
	private final byte type;
	
	AbstractNodeShape(byte type) {
		this.type = type;	
	}
		
	public byte getType() {
		return type;
	}

	/**
	 * A basic implementation that works by unwinding the path of the node shape
	 * into line segments and calculating whether each segment intersects the edge.
	 * For quadratic and cubic segments of the shape we flatten the curve and then 
	 * iterate over the resulting segments to determine an intersection. For some
	 * shapes there may be faster methods for calculatring the intersection, in which 
	 * case we encourage you to provide your own implementation of this method!
	 */
	public boolean computeEdgeIntersection(final float xMin, final float yMin, final float xMax,
	                                       final float yMax, final float ptX, final float ptY, 
	                                       final float[] returnVal)
	{
			final float centerX = (xMax + xMin)/2.0f;
			final float centerY = (yMax + yMin)/2.0f;

			final Shape shape = getShape(xMin,yMin,xMax,yMax); 
			final PathIterator pi = shape.getPathIterator(at);

			// where the path iterator places its coordinates as it iterates
			final float[] coords = new float[6];

			// the beginning point of the line we test at each step
			final float[] pt1 = new float[2];

			// the ending point of the line we test at each step
			final float[] pt2 = new float[2];

			// the location of the first SEG_MOVETO - necessary for SEG_CLOSE
			final float[] orig = new float[2];

			// the point identifying the intersection of the two line segments
			final float[] intersection = new float[2];

			// iterate over each segment
			while ( !pi.isDone() ) {
				final int segment = pi.currentSegment(coords);
				switch (segment) {
					
					// record the initial point
					case PathIterator.SEG_MOVETO:
						orig[0] = coords[0];
						orig[1] = coords[1];
						pt1[0] = coords[0];
						pt1[1] = coords[1];
						break;
					
					// record the next point and test
					case PathIterator.SEG_LINETO: 
						pt2[0] = coords[0];
						pt2[1] = coords[1];

						if ( segmentIntersection(intersection,centerX,centerY,ptX,ptY,
						                         pt1[0],pt1[1],pt2[0],pt2[1] ) ) { 
							returnVal[0] = intersection[0];
							returnVal[1] = intersection[1];
							return true;
						} else {
							pt1[0] = pt2[0];
							pt1[1] = pt2[1];
						}
						break;

					// record the quad and test the curve
					case PathIterator.SEG_QUADTO: 
						pt2[0] = coords[2];
						pt2[1] = coords[3];

						quad.setCurve(pt1[0],pt1[1],coords[0],coords[1],coords[2],coords[3]);

						if ( curveIntersection(intersection,centerX,centerY,ptX,ptY,quad) ) {
							returnVal[0] = intersection[0];
							returnVal[1] = intersection[1];
							return true;
						} else {
							pt1[0] = pt2[0];
							pt1[1] = pt2[1];
						}
						break;

					// record the cubic and test the curve
					case PathIterator.SEG_CUBICTO: 
						pt2[0] = coords[4];
						pt2[1] = coords[5];

						cubic.setCurve(pt1[0],pt1[1],coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);
						if ( curveIntersection(intersection,centerX,centerY,ptX,ptY,cubic) ) {
							returnVal[0] = intersection[0];
							returnVal[1] = intersection[1];
							return true;
						} else {
							pt1[0] = pt2[0];
							pt1[1] = pt2[1];
						}
						break;

					// use the last SEG_MOVETO point as the end point and test
					case PathIterator.SEG_CLOSE: 
						pt2[0] = orig[0];
						pt2[1] = orig[1];

						if ( segmentIntersection(intersection,centerX,centerY,ptX,ptY,
						                         pt1[0],pt1[1],pt2[0],pt2[1] ) ) { 
							returnVal[0] = intersection[0];
							returnVal[1] = intersection[1];
							return true;
						} 
						break;
				}
				pi.next();
			}
			return false;
	}


	/** 
	 * Computes the intersection of the line segment from (x1,y1) to (x2,y2)
	 * with the line segment from (x3,y3) to (x4,y4). If no intersection exists,
	 * returns false. Otherwise returns true, and returnVal[0] is set to be the
	 * X coordinate of the intersection point and returnVal[1] is set to be the
	 * Y coordinate of the intersection point. If more than one intersection
	 * point exists, "the intersection point" is defined to be the intersection
	 * point closest to (x1,y1). A note about overlapping line segments. Because
	 * of floating point numbers' inability to be totally accurate, it is quite
	 * difficult to represent overlapping line segments with floating point
	 * coordinates without using an absolute-precision math package. Because of
	 * this, poorly behaved outcome may result when computing the intersection
	 * of two [nearly] overlapping line segments. The only way around this would
	 * be to round intersection points to the nearest 32-bit floating point
	 * quantity. But then dynamic range is greatly compromised.
	 *
	 * @param returnVal A two element array specifying the point where the
	 * two line segments intersect. Element 0 specifies the X location and
	 * Element 1 specifies the Y location.  If this method returns false, 
	 * this array will not be modified.
	 * @param x1 The X location of beginning of the first line segment.
	 * @param y1 The Y location of beginning of the first line segment.
	 * @param x2 The X location of ending of the first line segment.
	 * @param y2 The Y location of ending of the first line segment.
	 * @param x3 The X location of beginning of the second line segment.
	 * @param y3 The Y location of beginning of the second line segment.
	 * @param x4 The X location of ending of the second line segment.
	 * @param y4 The Y location of ending of the second line segment.
	 * @return Whether or not the line segments defined by the input points intersect.
	 */
	public static boolean segmentIntersection(final float[] returnVal, 
	                                            float x1, float y1, 
	                                            float x2, float y2, 
	                                            float x3, float y3, 
	                                            float x4, float y4) {

		// Arrange the segment endpoints such that in segment 1, y1 >= y2
		// and such that in segment 2, y3 >= y4.
		boolean s1reverse = false;

		if (y2 > y1) {
			s1reverse = !s1reverse;

			float temp = x1;
			x1 = x2;
			x2 = temp;
			temp = y1;
			y1 = y2;
			y2 = temp;
		}

		if (y4 > y3) {
			float temp = x3;
			x3 = x4;
			x4 = temp;
			temp = y3;
			y3 = y4;
			y4 = temp;
		}

		// 
		// Note: While this algorithm for computing an intersection is
		// completely bulletproof, it's not a straighforward 'classic'
		// bruteforce method. This algorithm is well-suited for an
		// implementation using fixed-point arithmetic instead of floating-point
		// arithmetic because all computations are constrained to a certain
		// dynamic range relative to the input parameters.
		// 
		// We're going to reduce the problem in the following way:
		// 
		// 
		// (x1,y1) + \ \ \ (x3,y3) x1 x3 ---------+------+----------- yMax
		// ---------+------+----------- yMax \ | \ | \ | \ | \ | \ | \ | \ \ | \ |
		// =====\ \ | \| > \| + =====/ + (x,y) |\ / |\ | \ | \ | \ | \
		// ----------------+---+------- yMin ----------------+---+------ yMin |
		// (x2,y2) x4 x2 | | + If W := (x2-x4) / ((x2-x4) + (x3-x1)) , then
		// (x4,y4) x = x2 + W*(x1-x2) and y = yMin + W*(yMax-yMin)
		// 
		final float yMax = Math.min(y1, y3);
		final float yMin = Math.max(y2, y4);

		if (yMin > yMax) {
			return false;
		}

		if (y1 > yMax) {
			x1 = x1 + (((x2 - x1) * (yMax - y1)) / (y2 - y1));
			y1 = yMax;
		}

		if (y3 > yMax) {
			x3 = x3 + (((x4 - x3) * (yMax - y3)) / (y4 - y3));
			y3 = yMax;
		}

		if (y2 < yMin) {
			x2 = x1 + (((x2 - x1) * (yMin - y1)) / (y2 - y1));
			y2 = yMin;
		}

		if (y4 < yMin) {
			x4 = x3 + (((x4 - x3) * (yMin - y3)) / (y4 - y3));
			y4 = yMin;
		}

		// Handling for yMin == yMax. That is, in the reduced problem, both
		// segments are horizontal.
		if (yMin == yMax) {
			// Arrange the segment endpoints such that in segment 1, x1 <= x2
			// and such that in segment 2, x3 <= x4.
			if (x2 < x1) {
				s1reverse = !s1reverse;

				float temp = x1;
				x1 = x2;
				x2 = temp;
				temp = y1;
				y1 = y2;
				y2 = temp;
			}

			if (x4 < x3) {
				float temp = x3;
				x3 = x4;
				x4 = temp;
				temp = y3;
				y3 = y4;
				y4 = temp;
			}

			final float xMin = Math.max(x1, x3);
			final float xMax = Math.min(x2, x4);

			if (xMin > xMax) {
				return false;
			} else {
				if (s1reverse) {
					returnVal[0] = Math.max(xMin, xMax);
				} else {
					returnVal[0] = Math.min(xMin, xMax);
				}

				returnVal[1] = yMin; // == yMax

				return true;
			}
		}

		// It is now true that yMin < yMax because we've fully handled
		// the yMin == yMax case above.
		// Following if statement checks for a "twist" in the line segments.
		if (((x1 < x3) && (x2 < x4)) || ((x3 < x1) && (x4 < x2))) {
			return false;
		}

		// The segments are guaranteed to intersect.
		if ((x1 == x3) && (x2 == x4)) { // The segments overlap.

			if (s1reverse) {
				returnVal[0] = x2;
				returnVal[1] = y2;
			} else {
				returnVal[0] = x1;
				returnVal[1] = y1;
			}
		}

		// The segments are guaranteed to intersect in exactly one point.
		final float W = (x2 - x4) / ((x2 - x4) + (x3 - x1));
		returnVal[0] = x2 + (W * (x1 - x2));
		returnVal[1] = yMin + (W * (yMax - yMin));

		return true;
	}

	/**
	 * A method that computes the intersection of a line segment with 
	 * either a quad or cubic curve.
	 * @param x1 X locaiton of the beginning point of the line segement. 
	 * @param y1 Y locaiton of the beginning point of the line segement. 
	 * @param x2 X locaiton of the ending point of the line segement. 
	 * @param y2 Y locaiton of the ending point of the line segement. 
	 * @param curve While this is typed as a Shape it must be either a CubicCurve2D 
	 * or a QuadCurve2D.
	 */
	private boolean curveIntersection(final float[] returnVal,
			float x1, float y1, float x2, float y2, Shape curve) { 

		final float centerX = (x1 + x2)/2.0f;
		final float centerY = (y1 + y2)/2.0f;
	
		final PathIterator pi = curve.getPathIterator(at,0.05f);
		
		// where the path iterator places its coordinates as it iterates
		final float[] coords = new float[6];

		// the beginning point of the line we test at each step
		final float[] pt1 = new float[2];

		// the ending point of the line we test at each step
		final float[] pt2 = new float[2];

		// the location of the first SEG_MOVETO - necessary for SEG_CLOSE
		final float[] orig = new float[2];

		// the point identifying the intersection of the two line segments
		final float[] intersection = new float[2];

		// iterate over each segment
		while ( !pi.isDone() ) {
			final int segment = pi.currentSegment(coords);
			switch (segment) {
				case PathIterator.SEG_MOVETO:
					orig[0] = coords[0];
					orig[1] = coords[1];
					pt1[0] = coords[0];
					pt1[1] = coords[1];
					break;
						
				case PathIterator.SEG_LINETO: 
					pt2[0] = coords[0];
					pt2[1] = coords[1];

					if ( segmentIntersection(intersection,centerX,centerY,x1,y1,
					                         pt1[0],pt1[1],pt2[0],pt2[1] ) ) { 
						returnVal[0] = intersection[0];
						returnVal[1] = intersection[1];
						return true;
					} else {
						pt1[0] = pt2[0];
						pt1[1] = pt2[1];
					}
					break;

				case PathIterator.SEG_CLOSE: 
					pt2[0] = orig[0];
					pt2[1] = orig[1];

					if ( segmentIntersection(intersection,centerX,centerY,x1,y1,
					                         pt1[0],pt1[1],pt2[0],pt2[1] ) ) { 
						returnVal[0] = intersection[0];
						returnVal[1] = intersection[1];
						return true;
					} 
					break;

				// This shouldn't ever happen because the input curve should be
				// flattened into line segments (SEG_LINETO), but if it does, 
				// just treat the quad as if it were a line.
				case PathIterator.SEG_QUADTO: 
					pt2[0] = coords[2];
					pt2[1] = coords[3];

					if ( segmentIntersection(intersection,centerX,centerY,x1,y1,
					                         pt1[0],pt1[1],pt2[0],pt2[1] ) ) { 
						returnVal[0] = intersection[0];
						returnVal[1] = intersection[1];
						return true;
					} else {
						pt1[0] = pt2[0];
						pt1[1] = pt2[1];
					}
					break;

				// This shouldn't ever happen because the input curve should be
				// flattened into line segments (SEG_LINETO), but if it does, 
				// just treat the cubic as if it were a line.
				case PathIterator.SEG_CUBICTO: 
					pt2[0] = coords[4];
					pt2[1] = coords[5];

					if ( segmentIntersection(intersection,centerX,centerY,x1,y1,
					                         pt1[0],pt1[1],pt2[0],pt2[1] ) ) { 
						returnVal[0] = intersection[0];
						returnVal[1] = intersection[1];
						return true;
					} else {
						pt1[0] = pt2[0];
						pt1[1] = pt2[1];
					}
					break;

			}
			pi.next();
		}

		return false;
	}
}
