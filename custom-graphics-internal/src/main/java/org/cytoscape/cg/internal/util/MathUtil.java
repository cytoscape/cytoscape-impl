package org.cytoscape.cg.internal.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public final class MathUtil {

	private static final double EPSILON = 1e-30d;

	private MathUtil() {
		// restrict instantiation
	}
	
	/**
	 * Computes an inverse linear interpolation, returning an interpolation
	 * fraction. Returns 0.5 if the min and max values are the same.
	 * @param x the interpolated value
	 * @param min the minimum value (corresponds to f==0)
	 * @param min the maximum value (corresponds to f==1)
	 * @return the inferred interpolation fraction
	 */
    public static double invLinearInterp(double x, double min, double max) {
        double denom = max - min;
        return (denom < EPSILON && denom > -EPSILON ? 0 : (x - min) / denom);
    }
    
    /**
	 * Computes a linear interpolation between two values.
	 * @param f the interpolation fraction (typically between 0 and 1)
	 * @param min the minimum value (corresponds to f==0)
	 * @param max the maximum value (corresponds to f==1)
	 * @return the interpolated value
	 */
	public static double linearInterp(double f, double min, double max) {
		return min + f * (max - min);
	}
	
	/**
	 * Computes a logarithmic interpolation between two values. Uses a
	 * zero-symmetric logarithm calculation (see <code>symLog</code>).
	 * @param f the interpolation fraction (typically between 0 and 1)
	 * @param min the minimum value (corresponds to f==0)
	 * @param max the maximum value (corresponds to f==1)
	 * @param b the base of the logarithm
	 * @return the interpolated value
	 */
	public static double logInterp(double f, double min, double max, double b) {
		min = symLog(min, b);
		max = symLog(max, b);
		f = min + f * (max - min);
		
		return f < 0 ? -Math.pow(b, -f) : Math.pow(b, f);
	}
	
    /**
	 * Computes an inverse logarithmic interpolation, returning an
	 * interpolation fraction. Uses a zero-symmetric logarithm.
	 * Returns 0.5 if the min and max values are the same.
	 * @param x the interpolated value
	 * @param min the minimum value (corresponds to f==0)
	 * @param min the maximum value (corresponds to f==1)
	 * @param b the base of the logarithm
	 * @return the inferred interpolation fraction
	 */
	public static double invLogInterp(double x, double min, double max, double b) {
        min = symLog(min, b);
        double denom = symLog(max, b) - min;
        return (denom < EPSILON && denom > -EPSILON ? 0 : (symLog(x, b) - min) / denom);
    }
    
	/**
	 * Computes a zero-symmetric logarithm. Computes the logarithm of the
	 * absolute value of the input, and determines the sign of the output
	 * according to the sign of the input value.
	 * @param x the number for which to compute the logarithm
	 * @param b the base of the logarithm
	 * @return the symmetric log value.
	 */
	public static double symLog(double x, double b) {
		return x == 0 ? 0 : x > 0 ? log(x, b) : -log(-x, b);
	}
	
	public static double log(double x, double b) {
		return Math.log(x) / Math.log(b);
	}
	
	public static int findNearestNumber(int[] numbers, int target) {
		int minDiff = Integer.MAX_VALUE;
		int nearest = 0;
		
		for (int n : numbers) {
			int diff = Math.abs(n - target);
			
			if (diff < minDiff) {
				minDiff = diff;
				nearest = n;
			}
		}
		
		return nearest;
	}
	
	public static float findNearestNumber(float[] numbers, float target) {
		float minDiff = Float.MAX_VALUE;
		float nearest = 0.0f;
		
		for (float n : numbers) {
			float diff = Math.abs(n - target);
			
			if (diff < minDiff) {
				minDiff = diff;
				nearest = n;
			}
		}
		
		return nearest;
	}
	
	/**
	 * Computes the intersection between two lines. The calculated point is approximate, 
	 * @param p1 Point 1 of Line 1
	 * @param p2 Point 2 of Line 1
	 * @param p3 Point 1 of Line 2
	 * @param p4 Point 2 of Line 2
	 * @return Point where the segments intersect, or null if they don't
	 */
	public static Point2D getIntersectionPoint(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
		return getIntersectionPoint(new Line2D.Double(p1, p2), new Line2D.Double(p3, p4));
	}
	
	public static Point2D getIntersectionPoint(Line2D lineA, Line2D lineB) {
        double a1x = lineA.getX1();
        double a1y = lineA.getY1();
        double a2x = lineA.getX2();
        double a2y = lineA.getY2();

        double b1x = lineB.getX1();
        double b1y = lineB.getY1();
        double b2x = lineB.getX2();
        double b2y = lineB.getY2();

        Point2D p = null;
        double d = (b2y - b1y) * (a2x - a1x) - (b2x - b1x) * (a2y - a1y);
        
		if (d != 0) {
			double ua = ((b2x - b1x) * (a1y - b1y) - (b2y - b1y) * (a1x - b1x)) / d;
			double ub = ((a2x - a1x) * (a1y - b1y) - (a2y - a1y) * (a1x - b1x)) / d;

			if (0 <= ua && ua <= 1 && 0 <= ub && ub <= 1) {
				double xi = a1x + ua * (a2x - a1x);
				double yi = a1y + ua * (a2y - a1y);
				p = new Point2D.Double(xi, yi);
			}
		}
		
        return p;
    }
	
	/**
	 * @param line
	 * @param rect
	 * @return Point2D[0] = Top line; Point2D[1] = Bottom line; Point2D[3] = Left line; Point2D[4] = Right line;
	 */
	public static Point2D[] getIntersectionPoints(Line2D line, Rectangle2D rect) {
        Point2D[] p = new Point2D[4];

        // Top line
        p[0] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rect.getX(),
                        rect.getY(),
                        rect.getX() + rect.getWidth(),
                        rect.getY()));
        // Bottom line
        p[1] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rect.getX(),
                        rect.getY() + rect.getHeight(),
                        rect.getX() + rect.getWidth(),
                        rect.getY() + rect.getHeight()));
        // Left side...
        p[2] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rect.getX(),
                        rect.getY(),
                        rect.getX(),
                        rect.getY() + rect.getHeight()));
        // Right side
        p[3] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rect.getX() + rect.getWidth(),
                        rect.getY(),
                        rect.getX() + rect.getWidth(),
                        rect.getY() + rect.getHeight()));

        return p;
    }
	
	public static double sqr(double x) {
        return x * x;
    }
	
	public static int getQuadrant(double angle) {
		return (int)(normalizeAngle(angle) / 90) % 4 + 1;
	}
	
	public static double normalizeAngle(double angle) {
		return angle + Math.ceil(-angle / 360) * 360;
	}
	
	public static Line2D getGradientAxis(Rectangle2D bounds, double angle) {
		Point2D start = null;
		Point2D end = null;
		
		double x1 = bounds.getMinX();
		double y1 = bounds.getMinY();
		double x2 = bounds.getMaxX();
		double y2 = bounds.getMaxY();
		double cx = bounds.getCenterX();
		double cy = bounds.getCenterY();
		
		if (angle == 0.0) {
			// Just because it's faster to calculate it like this with these angles
			start = new Point2D.Double(x1, cy);
			end = new Point2D.Double(x2, cy);
		} else if (angle == 90.0) {
			start = new Point2D.Double(cx, y2);
			end = new Point2D.Double(cx, y1);
		} else if (angle == 180.0) {
			start = new Point2D.Double(x2, cy);
			end = new Point2D.Double(x1, cy);
		} else if (angle == 270.0) {
			start = new Point2D.Double(cx, y1);
			end = new Point2D.Double(cx, y2);
		} else {
			// To understand what it does here, see:
			//   https://developer.mozilla.org/en-US/docs/Web/CSS/linear-gradient
			//   http://hugogiraudel.com/blog/css-gradients
			//   http://codepen.io/thebabydino/pen/qgoBL
			
			double ax;
			double ay;
			double d = Math.max(bounds.getWidth(), bounds.getHeight());
			
			// Gradient line when angle is 0 degree and the rotate it
			Line2D gl = new Line2D.Double(cx - d, cy, cx + d, cy);
			gl = MathUtil.rotate(gl, -angle, cx, cy);
			
			int q = MathUtil.getQuadrant(angle);
			
			// Creates two perpendicular lines (to the gradient line) and rotate them accordingly
			Line2D pl1 = new Line2D.Double(x1, cy - d, x1, cy + d);
			ax = x1;
			ay = q == 1 || q == 3 ? y2 : y1;
			pl1 = MathUtil.rotate(pl1, -angle, ax, ay);
			
			Line2D pl2 = new Line2D.Double(x2, cy - d, x2, cy + d);
			ax = x2;
			ay = q == 1 || q == 3 ? y1 : y2;
			pl2 = MathUtil.rotate(pl2, -angle, ax, ay);
			
			// Find the intersection points between the gradient line and the perpendicular lines
			start = MathUtil.getIntersectionPoint(gl, (q == 1 || q == 4 ? pl1 : pl2));
			end = MathUtil.getIntersectionPoint(gl, (q == 1 || q == 4 ? pl2 : pl1));
		}
		
		return new Line2D.Double(start, end);
	}
	
	public static Line2D rotate(Line2D line, double angle, double anchorx, double anchory) {
		AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(angle), anchorx, anchory);
		Point2D p1 = at.transform(line.getP1(), new Point2D.Double());
		Point2D p2 = at.transform(line.getP2(), new Point2D.Double());
		
		return new Line2D.Double(p1, p2);
	}
	
	/**
	 * Calculates the scaling factor for the maximum possible size that fits inside of the target bounding box
	 * while maintaining the aspect ratio given by the original box. Just multiply the original rectangle's
	 * width and height by the returned value in order to get the new size.
	 * 
	 * @param w1 the width of the rectangle that must be resized to fit the bounding box.
	 * @param h1 the height of the rectangle that must be resized to fit the bounding box.
	 * @param w2 the width of the bounding box.
	 * @param h2 the height of the bounding box.
	 * @return the scaling factor that allows the original rectangle to fit inside the bounding box
	 *         with no distortion or cropping.
	 */
	public static double scaleToFit(double w1, double h1, double w2, double h2) {
		var fw = 0.0; // scaling factor for the width
		var fh = 0.0; // scaling factor for the height
		
		if (w1 > 0)
			fw = w2 / w1;
		if (h1 > 0)
			fh = h2 / h1;
		
		return Math.min(fw, fh); // final scaling factor
    }
}
