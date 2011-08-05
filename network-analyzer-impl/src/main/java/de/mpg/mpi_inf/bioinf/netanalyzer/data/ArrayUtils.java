/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer.data;

import java.awt.geom.Point2D;

/**
 * Utility class providing helper methods for manipulation of arrays.
 * 
 * @author Yassen Assenov
 */
public abstract class ArrayUtils {

	/**
	 * Stores the coordinates of the given points in separate arrays.
	 * 
	 * @param aPoints Array of points from which coordinate values are to be read.
	 * @param aX Array to store the values of the <i>x</i> coordinates of the points. The length of
	 *        this array must be at least the length of <code>aPoints</code>.
	 * @param aY Array to store the values of the <i>y</i> coordinates of the points. The length of
	 *        this array must be at least the length of <code>aPoints</code>.
	 * 
	 * @throws ArrayIndexOutOfBoundsException If the length of <code>aX</code> or of
	 *         <code>aY</code> is smaller than the length of <code>aPoints</code>.
	 * @throws NullPointerException If any of the parameters is <code>null</code>.
	 */
	public static void coordinateSplit(Point2D.Double[] aPoints, double[] aX, double[] aY) {
		final int count = aPoints.length;
		if (aX.length < count || aY.length < count) {
			throw new ArrayIndexOutOfBoundsException(count);
		}
		for (int i = 0; i < count; ++i) {
			final Point2D.Double point = aPoints[i];
			aX[i] = point.x;
			aY[i] = point.y;
		}
	}

	/**
	 * Applies natural logarithm to the coordinate values of all numbers in the given array.
	 * 
	 * @param aPoints Array of points to which logarithm is to be applied.
	 * @param aModifyOriginal Flag indicating if the original array (<code>aPoints</code>)
	 *        should be modified. If this parameter is <code>true</code>, the coordinates of the
	 *        points in <code>aPoints</code> are modified to contain the logarithms of the
	 *        original coordinate values. If this parameter is <code>false</code>, the logarithms
	 *        of the point coordinates are stored in a newly created array.
	 * @return If <code>aModifyOriginal</code> is <code>false</code>, newly created array in
	 *         which the <code>i</code>-th element is a point having as coordinates the natural
	 *         logarithm of the coordinates of <code>aPoints[i]</code>. If
	 *         <code>aModifyOriginal</code> is <code>false</code>, the returned value is (the
	 *         modified) <code>aPoints</code>.
	 * 
	 * @throws NullPointerException If <code>aPoints</code> is <code>null</code>.
	 */
	public static Point2D.Double[] log(Point2D.Double[] aPoints, boolean aModifyOriginal) {
		final int pointCount = aPoints.length;
		final Point2D.Double[] result = aModifyOriginal ? aPoints : new Point2D.Double[pointCount];
		for (int i = 0; i < pointCount; ++i) {
			final Point2D.Double p = aPoints[i];
			if (aModifyOriginal) {
				p.x = Math.log(p.x);
				p.y = Math.log(p.y);
			} else {
				result[i] = new Point2D.Double(Math.log(p.x), Math.log(p.y));
			}
		}
		return result;
	}

	/**
	 * Applies natural logarithm to all numbers in the given array.
	 * 
	 * @param aPoints Array of numbers to which logarithm is to be applied.
	 * @param aModifyOriginal Flag indicating if the original array (<code>aPoints</code>)
	 *        should be modified. If this parameter is <code>true</code>, the values of
	 *        <code>aPoints</code> are modified to contain the logarithms of the original values.
	 *        If this parameter is <code>false</code>, the logarithms of the elements of
	 *        <code>aPoints</code> are stored in a newly created array.
	 * @return If <code>aModifyOriginal</code> is <code>false</code>, newly created array in
	 *         which the <code>i</code>-th element contains the natural logarithm of
	 *         <code>aPoints[i]</code>. If <code>aModifyOriginal</code> is <code>false</code>,
	 *         the returned value is (the modified) <code>aPoints</code>.
	 * 
	 * @throws NullPointerException If <code>aPoints</code> is <code>null</code>.
	 */
	public static double[] log(double[] aPoints, boolean aModifyOriginal) {
		final int pointCount = aPoints.length;
		final double[] result = aModifyOriginal ? aPoints : new double[pointCount];
		for (int i = 0; i < pointCount; ++i) {
			result[i] = Math.log(aPoints[i]);
		}
		return result;
	}

	/**
	 * Computes the sum of all values in the given array.
	 * 
	 * @param aArray Array of numbers.
	 * @return Sum of all values in <code>aArray</code>; <code>0</code> if <code>aArray</code>
	 *         is empty.
	 * 
	 * @throws NullPointerException If <code>aArray</code> is <code>null</code>.
	 */
	public static double sum(double[] aArray) {
		double result = 0;
		for (final double a : aArray) {
			result += a;
		}
		return result;
	}

	/**
	 * Computes the arithmetic mean of the values in the given array.
	 * 
	 * @param aArray Array of numbers.
	 * @return Mean of all values in <code>aArray</code>; <code>0</code> if <code>aArray</code>
	 *         is empty.
	 * 
	 * @throws NullPointerException If <code>aArray</code> is <code>null</code>.
	 */
	public static double mean(double[] aArray) {
		return sum(aArray) / aArray.length;
	}

	/**
	 * Computes the sum of the multiplications of the given arrays.
	 * 
	 * @param aArray1 First array of numbers.
	 * @param aArray2 Second array of numbers.
	 * 
	 * @return Sum of the multiplicated pairs of the elements of <code>aArray1</code> and
	 *         <code>aArray2</code> :
	 *         <code>aArray1[0] * aArray2[0] + aArray1[1] * aArray2[1] + ...</code>.
	 * 
	 */
	public static double sumMult(double[] aArray1, double[] aArray2) {
		double result = 0;
		for (int i = 0; i < aArray1.length; ++i) {
			result += aArray1[i] * aArray2[i];
		}
		return result;
	}

	/**
	 * Computes the sum of the squares all elements in the given array.
	 * 
	 * @param aArray Array of numbers.
	 * @return Sum of the squares of all elements of <code>aArray</code>.
	 */
	public static double sumSquares(double[] aArray) {
		double result = 0;
		for (final double a : aArray) {
			result += a * a;
		}
		return result;
	}

	/**
	 * Finds the minimum and maximum values for <i>x</i> and <i>y</i> coordinates in the given
	 * array of points.
	 * 
	 * @param aPoints Array of two-dimensional points to be traversed.
	 * @param aMin Point to store the minimum values for <i>x</i> and <i>y</i> coordinates. If
	 *        <code>aPoints</code> is an empty array, the value of this parameter remains
	 *        unmodified.
	 * @param aMax Point to store the maximum values for <i>x</i> and <i>y</i> coordinates. If
	 *        <code>aPoints</code> is an empty array, the value of this parameter remains
	 *        unmodified.
	 * 
	 * @throws NullPointerException If <code>aPoints</code> is <code>null</code>, or if
	 *         <code>aPoints</code> is non-empty and any of the other parameters is
	 *         <code>null</code>.
	 */
	public static void minMax(Point2D.Double[] aPoints, Point2D.Double aMin, Point2D.Double aMax) {
		if (aPoints.length > 0) {
			aMin.x = aMax.x = aPoints[0].x;
			aMin.y = aMax.y = aPoints[0].y;
			for (final Point2D.Double point : aPoints) {
				if (point.x < aMin.x) {
					aMin.x = point.x;
				} else if (point.x > aMax.x) {
					aMax.x = point.x;
				}
				if (point.y < aMin.y) {
					aMin.y = point.y;
				} else if (point.y > aMax.y) {
					aMax.y = point.y;
				}
			}
		}
	}
}
