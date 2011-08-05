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

import de.mpg.mpi_inf.bioinf.netanalyzer.ui.charts.MyLogarithmicAxis;

/**
 * Histogram dataset which puts the observations in log bins (bins of exponentially growing size).
 * 
 * @author Yassen Assenov
 * @author Nadezhda Doncheva
 */
public class LogBinDistribution {

	/**
	 * Initializes a new instance of <code>LogBinDistribution</code>.
	 * 
	 */
	public LogBinDistribution() {
		bins = new long[2][INITIAL_SIZE];
		bins[0][0] = 0;
		for (int i = 1, power = 1; i < INITIAL_SIZE; ++i, power *= LOGBASE) {
			bins[0][i] = power;
		}
		maxObservation = 0;
	}

	/**
	 * Gets the maximum value for an observation added to the distribution.
	 * 
	 * @return Maximum added observation.
	 * @see #addObservation(long)
	 */
	public long getMaxObservation() {
		return maxObservation;
	}

	/**
	 * Creates an <code>LongHistogram</code> complex parameter instance that contains the data of this
	 * distribution.
	 * 
	 * @return Newly created instance of <code>LongHistogram</code> based on this distribution.
	 */
	public LongHistogram createHistogram() {
		return new LongHistogram(bins, 0, getBinIndex(maxObservation));
	}

	/**
	 * Creates an <code>Points2D</code> complex parameter instance that contains the data of this
	 * distribution.
	 * 
	 * @return Newly created instance of <code>Points2D</code> based on this distribution.
	 */
	public Points2D createPoints2D() {
		final int size = getBinIndex(maxObservation) + 1;
		final Point2D.Double[] points = new Point2D.Double[size];
		for (int i = 0; i < size; ++i) {
			points[i] = new Point2D.Double(bins[0][i], bins[1][i]);
		}
		return new Points2D(points);
	}

	/**
	 * Adds a new observation to this distribution.
	 * 
	 * @param aObservation Observed value to be added.
	 */
	public void addObservation(long aObservation) {
		final int binIndex = getBinIndex(aObservation);
		if (binIndex >= bins[0].length) {
			ensureCapacity(binIndex + 1);
		}
		if (maxObservation < aObservation) {
			maxObservation = aObservation;
		}
		bins[1][binIndex]++;
	}

	/**
	 * Enlarges the size of {@link #bins}, if necessary, to be at least the specified capacity.
	 * 
	 * @param aCapacity Desired minimum size of {@link #bins}.
	 */
	private void ensureCapacity(long aCapacity) {
		int currentCap = bins[0].length;
		int newCap = currentCap;
		while (newCap < aCapacity) {
			newCap += 2;
		}
		if (newCap != currentCap) {
			long[][] newDist = new long[2][newCap];
			System.arraycopy(bins[0], 0, newDist[0], 0, currentCap);
			System.arraycopy(bins[1], 0, newDist[1], 0, currentCap);
			bins = newDist;
			for (int i = currentCap; i < newCap; ++i) {
				bins[0][i] = bins[0][i - 1] * (int)LOGBASE;
			}
		}
	}

	/**
	 * Gets the bin to which the given value should be assigned.
	 * 
	 * @param aObservation Value to be assigned to a bin.
	 * @return Zero-based index of the bin to which <code>aObservation</code> belongs.
	 */
	private static int getBinIndex(long aObservation) {
		return aObservation != 0 ? (int) (Math.log(aObservation) / Math.log(LOGBASE)) + 1 : 0;
	}

	/**
	 * Initial size of {@link #bins}.
	 */
	private static final int INITIAL_SIZE = 8;

	/**
	 * Base of the logarithmic function applied in binning.
	 */
	private static final double LOGBASE = MyLogarithmicAxis.LOGBASE;

	/**
	 * Bins in an long distribution in the form of two arrays. The <code>i</code>-th element of the
	 * first array contains the left end of the <code>i</code>-th bin: <code>0</code> for
	 * <code>i = 0</code> and <code>{@link #LOGBASE}<sup>i - 1</sup></code> for <code>i &gt; 0</code>.
	 * The <code>i</code>-th element of the second array contains the number of observations (nodes) with
	 * values assigned to the <code>i</code>-th bin.
	 */
	private long[][] bins;

	/**
	 * Maximum observation of a node.
	 */
	private long maxObservation;
}
