package org.cytoscape.ding.internal.util;

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
    public static double invLinearInterp(final double x, final double min, final double max) {
        final double denom = max - min;
        return (denom < EPSILON && denom > -EPSILON ? 0 : (x - min) / denom);
    }
    
    /**
	 * Computes a linear interpolation between two values.
	 * @param f the interpolation fraction (typically between 0 and 1)
	 * @param min the minimum value (corresponds to f==0)
	 * @param max the maximum value (corresponds to f==1)
	 * @return the interpolated value
	 */
	public static double linearInterp(final double f, final double min, final double max) {
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
	
	/**
	 * Computes the intersection between two lines. The calculated point is approximate, 
	 * @param p1 Point 1 of Line 1
	 * @param p2 Point 2 of Line 1
	 * @param p3 Point 1 of Line 2
	 * @param p4 Point 2 of Line 2
	 * @return Point where the segments intersect, or null if they don't
	 */
	public static Point2D getIntersectionPoint(final Point2D p1, final Point2D p2, final Point2D p3, final Point2D p4) {
		return getIntersectionPoint(new Line2D.Double(p1, p2), new Line2D.Double(p3, p4));
	}
	
	public static Point2D getIntersectionPoint(final Line2D lineA, final Line2D lineB) {
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
	public static Point2D[] getIntersectionPoints(final Line2D line, final Rectangle2D rect) {
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
	
	public static double normalizeAngle(double angle) {
		double value = angle % 360;
		if (angle < 0) value = value + 360;
		
		return value;
	}
	
	public static int getQuadrant(double angle) {
		return (int)(normalizeAngle(angle) / 90) % 4 + 1;
	}
	
	public static Line2D rotate(final Line2D line, final double angle, final double anchorx, final double anchory) {
		final AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(angle), anchorx, anchory);
		final Point2D p1 = at.transform(line.getP1(), new Point2D.Double());
		final Point2D p2 = at.transform(line.getP2(), new Point2D.Double());
		
		return new Line2D.Double(p1, p2);
	}
}
