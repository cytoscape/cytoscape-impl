package de.mpg.mpi_inf.bioinf.netanalyzer.data.filter;

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

import de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.LongHistogram;

/**
 * Filter for complex parameters of type <code>LongHistogram</code>.
 * 
 * @author Yassen Assenov
 */
public class LongHistogramFilter implements ComplexParamFilter {

	/**
	 * Initializes a new instance of <code>LongHistogramFilter</code>.
	 */
	public LongHistogramFilter() {
		observationMin = Long.MIN_VALUE;
		observationMax = Long.MAX_VALUE;
	}

	/**
	 * Initializes a new instance of <code>LongHistogramFilter</code> based on the given range.
	 * 
	 * @param aObservationMin Minimal observed value to be considered.
	 * @param aObservationMax Maximal observer value to be considered.
	 * @see #getObservationMin()
	 * @see #getObservationMax()
	 */
	public LongHistogramFilter(long aObservationMin, long aObservationMax) {
		observationMin = aObservationMin;
		observationMax = aObservationMax;
	}

	/**
	 * Performs filtering on the given histogram.
	 * 
	 * @param aParam <code>LongHistogram</code> instance whose data is to be filtered.
	 * @return New instance of <code>LongHistogram</code> whose data is the result of applying
	 *         filtering criteria on <code>aParam</code>'s data.
	 * @throws UnsupportedOperationException If the complex parameter is not of type
	 *         {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.LongHistogram}.
	 */
	public ComplexParam filter(ComplexParam aParam) {
		if (!(aParam instanceof LongHistogram)) {
			throw new UnsupportedOperationException();
		}
		long[][] bins = ((LongHistogram) aParam).getBins();
		long[] dataRange = bins[0];
		int fromIndex = 0;
		while (dataRange[fromIndex] < observationMin) {
			fromIndex++;
		}
		int toIndex = fromIndex;
		while (toIndex < dataRange.length && dataRange[toIndex] <= observationMax) {
			toIndex++;
		}
		toIndex--;
		return new LongHistogram(bins, fromIndex, toIndex);
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
	public long getObservationMin() {
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
	public long getObservationMax() {
		return observationMax;
	}

	/**
	 * Sets the minimal observed value to be considered.
	 * <p>
	 * Note that if you set the minimal observation value to be &ge; the maximal observation value
	 * ({@link #setObservationMax(long)}), the filter will effectively drop any data from a
	 * passed histogram.
	 * </p>
	 * 
	 * @param aObservation Minimal observation value to be considered.
	 * @see #getObservationMin()
	 */
	public void setObservationMin(long aObservation) {
		observationMin = aObservation;
	}

	/**
	 * Sets the maximal observed value to be considered.
	 * <p>
	 * Note that if you set the maximal observation value to be &le; the minimal observation value
	 * ({@link #setObservationMin(long)}, the filter will effectively drop any data from a
	 * passed histogram.
	 * </p>
	 * 
	 * @param aObservation Maximal observation value to be considered.
	 * @see #getObservationMax()
	 */
	public void setObservationMax(long aObservation) {
		observationMax = aObservation;
	}

	/**
	 * Minimal observed value considered in filtering.
	 */
	private long observationMin;

	/**
	 * Maximal observed value considered in filtering.
	 */
	private long observationMax;
}
