package de.mpg.mpi_inf.bioinf.netanalyzer.dec;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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

import java.awt.geom.Point2D;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.ArrayUtils;

/**
 * Utility class providing methods for fitting functions and working with numerical sequences.
 * 
 * @author Yassen Assenov
 */
public abstract class Fitter {

	/**
	 * Fits a power to a set of data points using the least squares method.
	 * 
	 * @param aPoints Set of data points to fit a power law to. All data points must have positive
	 *        coordinates.
	 * @return Coefficients of the fitted power law (<i>y</i> = <i>&alpha;x<sup>&beta;</sup></i>)
	 *         encapsulated in a <code>Point2D.Double</code> instance.
	 * 
	 * @throws NullPointerException If <code>aPoints</code> is <code>null</code>.
	 */
	public static Point2D.Double leastSquaresPowerLawFit(Point2D.Double[] aPoints) {
		final int n = aPoints.length;
		double[] lnX = new double[n];
		double[] lnY = new double[n];
		for (int i = 0; i < n; ++i) {
			lnX[i] = Math.log(aPoints[i].x);
			lnY[i] = Math.log(aPoints[i].y);
		}
		final Point2D.Double coefs = computeLeastSquares(lnX, lnY);
		coefs.x = Math.exp(coefs.x);
		return coefs;
	}

	/**
	 * Fits a line to a set of data points using the least squares method.
	 * 
	 * @param aPoints Set of data points to fit a line to.
	 * @return Coefficients of the fitted line (<i>y</i> = <i>&alpha;</i> + <i>x &beta;</i>)
	 *         encapsulated in a <code>Point2D.Double</code> instance.
	 */
	public static Point2D.Double leastSquaresLineFit(Point2D.Double[] aPoints) {
		final int count = aPoints.length;
		final double[] x = new double[count];
		final double[] y = new double[count];
		ArrayUtils.coordinateSplit(aPoints, x, y);
		return computeLeastSquares(x, y);
	}

	/**
	 * Computes the Pearson correlation coefficient between the two data series.
	 * 
	 * @param aSeries1 First series of values.
	 * @param aSeries2 Second series of values.
	 * @return Correlation between <code>aSeries1</code> and <code>aSeries2</code>.
	 * 
	 * @throws ArithmeticException If the correlation between the two series cannot be computed.
	 * @throws IllegalArgumentException If the length of <code>aSeries1</code> is different than
	 *         the length of <code>aSeries2</code>, or if the series are empty.
	 * @throws NullPointerException If <code>aSeries1</code> or <code>aSeries2</code> is
	 *         <code>null</code>.
	 */
	public static double computeCorr(double[] aSeries1, double[] aSeries2) {
		final int count = aSeries1.length;
		if (count == 0 || aSeries2.length != count) {
			throw new IllegalArgumentException();
		}
		final double mean1 = ArrayUtils.sum(aSeries1) / count;
		final double mean2 = ArrayUtils.sum(aSeries2) / count;
		double nom = 0;
		double denom1 = 0;
		double denom2 = 0;
		for (int i = 0; i < count; ++i) {
			final double diff1 = aSeries1[i] - mean1;
			final double diff2 = aSeries2[i] - mean2;
			nom += diff1 * diff2;
			denom1 += diff1 * diff1;
			denom2 += diff2 * diff2;
		}
		if (denom1 == 0 || denom2 == 0) {
			throw new ArithmeticException();
		}
		return nom / Math.sqrt(denom1 * denom2);
	}

	/**
	 * Computes the coefficient of determination for the given data series and prediction outcomes.
	 * 
	 * @param aValues True data values.
	 * @param aPredicted Predicted values.
	 * @return R-Squared coefficient for the given true and predicted data series.
	 * 
	 * @throws ArithmeticException If the R-Squared value cannot be computed.
	 * @throws IllegalArgumentException If the length of <code>aValues</code> is different than
	 *         the length of <code>aPredicted</code>, or if the series are empty.
	 * @throws NullPointerException If <code>aValues</code> or <code>aPredicted</code> is
	 *         <code>null</code>.
	 */
	public static double computeRSquared(double[] aValues, double[] aPredicted) {
		final int count = aValues.length;
		if (count == 0 || aPredicted.length != count) {
			throw new IllegalArgumentException();
		}
		final double mean = ArrayUtils.mean(aValues);
		double nom = 0;
		double denom = 0;
		for (int i = 0; i < count; ++i) {
			final double diff1 = aPredicted[i] - mean;
			final double diff2 = aValues[i] - mean;
			nom += diff1 * diff1;
			denom += diff2 * diff2;
		}
		if (denom == 0) {
			throw new ArithmeticException();
		}
		return nom / denom;
	}

	/**
	 * Fits a line to a set of data points using the least squares method.
	 * 
	 * @param aX Array storing the <i>x</i> values of the data points.
	 * @param aY Array storing the <i>y</i> values of the data points.
	 * @return Coefficients of the fitted line (<i>y</i> = <i>&alpha; + &beta; x</i>)
	 *         encapsulated in a <code>Point2D.Double</code> instance; <code>null</code> if a
	 *         line could not be fitted.
	 */
	private static Point2D.Double computeLeastSquares(double[] aX, double[] aY) {
		final double sumX = ArrayUtils.sum(aX);
		final double sumY = ArrayUtils.sum(aY);
		final double n = aX.length;

		final double b = (n * ArrayUtils.sumMult(aX, aY) - sumX * sumY)
				/ (n * ArrayUtils.sumSquares(aX) - sumX * sumX);
		final double a = (sumY - b * sumX) / n;
		if (Double.isNaN(a) || Double.isNaN(b)) {
			return null;
		}
		return new Point2D.Double(a, b);
	}
}
