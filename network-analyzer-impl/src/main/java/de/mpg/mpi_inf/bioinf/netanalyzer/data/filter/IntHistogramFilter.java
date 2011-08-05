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

package de.mpg.mpi_inf.bioinf.netanalyzer.data.filter;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.IntHistogram;

/**
 * Filter for complex parameters of type <code>IntHistogram</code>.
 * 
 * @author Yassen Assenov
 */
public class IntHistogramFilter implements ComplexParamFilter {

	/**
	 * Initializes a new instance of <code>IntHistogramFilter</code>.
	 */
	public IntHistogramFilter() {
		observationMin = Integer.MIN_VALUE;
		observationMax = Integer.MAX_VALUE;
	}

	/**
	 * Initializes a new instance of <code>IntHistogramFilter</code> based on the given range.
	 * 
	 * @param aObservationMin Minimal observed value to be considered.
	 * @param aObservationMax Maximal observer value to be considered.
	 * @see #getObservationMin()
	 * @see #getObservationMax()
	 */
	public IntHistogramFilter(int aObservationMin, int aObservationMax) {
		observationMin = aObservationMin;
		observationMax = aObservationMax;
	}

	/**
	 * Performs filtering on the given histogram.
	 * 
	 * @param aParam <code>IntHistogram</code> instance whose data is to be filtered.
	 * @return New instance of <code>IntHistogram</code> whose data is the result of applying
	 *         filtering criteria on <code>aParam</code>'s data.
	 * @throws UnsupportedOperationException If the complex parameter is not of type
	 *         {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.IntHistogram}.
	 */
	public ComplexParam filter(ComplexParam aParam) {
		if (!(aParam instanceof IntHistogram)) {
			throw new UnsupportedOperationException();
		}
		int[][] bins = ((IntHistogram) aParam).getBins();
		int[] dataRange = bins[0];
		int fromIndex = 0;
		while (dataRange[fromIndex] < observationMin) {
			fromIndex++;
		}
		int toIndex = fromIndex;
		while (toIndex < dataRange.length && dataRange[toIndex] <= observationMax) {
			toIndex++;
		}
		toIndex--;
		return new IntHistogram(bins, fromIndex, toIndex);
	}

	/**
	 * Gets the minimal observed value to be considered.
	 * <p>
	 * When filtering of a histogram is performed, only bins defined over intervals
	 * &quot;right&quot; of the minimal observed value are retained; all other bins are dropped.
	 * Formally, a bin defined over the interval <code>[i, j)</code> is dropped &hArr;
	 * <code>i &lt; n</code>, where <code>n</code> is the value returned by this method.
	 * </p>
	 * 
	 * @return Minimal observed value to be considered when filtering.
	 */
	public int getObservationMin() {
		return observationMin;
	}

	/**
	 * Gets the maximal observed value to be considered.
	 * <p>
	 * When filtering of a histogram is performed, only bins defined over intervals
	 * &quot;left&quot; of the maximal observed value are retained; all other bins are dropped.
	 * Formally, a bin defined over the interval <code>[i, j)</code> is dropped &hArr;
	 * <code>j &gt; n</code>, where <code>n</code> is the value returned by this method.
	 * </p>
	 * 
	 * @return Maximal observed value to be considered when filtering.
	 */
	public int getObservationMax() {
		return observationMax;
	}

	/**
	 * Sets the minimal observed value to be considered.
	 * <p>
	 * Note that if you set the minimal observation value to be &ge; the maximal observation value
	 * ({@link #setObservationMax(int)}), the filter will effectively drop any data from a
	 * passed histogram.
	 * </p>
	 * 
	 * @param aObservation Minimal observation value to be considered.
	 * @see #getObservationMin()
	 */
	public void setObservationMin(int aObservation) {
		observationMin = aObservation;
	}

	/**
	 * Sets the maximal observed value to be considered.
	 * <p>
	 * Note that if you set the maximal observation value to be &le; the minimal observation value
	 * ({@link #setObservationMin(int)}), the filter will effectively drop any data from a
	 * passed histogram.
	 * </p>
	 * 
	 * @param aObservation Maximal observation value to be considered.
	 * @see #getObservationMax()
	 */
	public void setObservationMax(int aObservation) {
		observationMax = aObservation;
	}

	/**
	 * Minimal observed value considered in filtering.
	 */
	private int observationMin;

	/**
	 * Maximal observed value considered in filtering.
	 */
	private int observationMax;
}
