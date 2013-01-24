package de.mpg.mpi_inf.bioinf.netanalyzer.data;

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

import java.io.FileWriter;
import java.io.IOException;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.LineReader;

/**
 * Complex parameter that stores histograms of long values only.
 * <p>
 * A long histogram shows the number of observations on long values. Formally, this type of
 * histogram is characterized by:<br/>
 * <ul>
 * <li>every bin is in the form <code>[i; j)</code> where <code>i</code> and <code>j</code>
 * are longs and <code>i &lt; j</code>.</li>
 * <li>the value of every bin is a long.</li>
 * </ul>
 * </p>
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public class LongHistogram
	implements ComplexParam {

	/**
	 * Initializes a new instance of <code>LongHistogram</code> by loading it from the given
	 * stream.
	 * 
	 * @param aArgs One-element array representing the argument passed to this type. This argument
	 *        must be the <code>String</code> representation of an integer and it specifies the
	 *        number of bins in this histogram.
	 * @param aReader Reader from a text stream. The reader must be open and positioned in the
	 *        stream such that the data for the histogram follows.
	 * 
	 * @throws IOException If I/O error occurs while reading from <code>aReader</code>.
	 * 
	 * @see #load(String[], LineReader)
	 * @see ComplexParam#load(String[], LineReader)
	 */
	public LongHistogram(String[] aArgs, LineReader aReader)
		throws IOException {
		load(aArgs, aReader);
	}

	/**
	 * Initializes a new instance of <code>LongHistogram</code> as a subset of the given histogram.
	 * 
	 * @param aBins Bins of the source histogram.
	 * @param aFromIndex Index of first bin to be included in the histogram.
	 * @param aToIndex Index of last bin to be included in the histogram.
	 * @throws ArrayIndexOutOfBoundsException If one of the following is <code>true</code>:
	 *         <ul>
	 *         <li><code>aBins.length</code> &lt; <code>2</code></li>
	 *         <li><code>aBins[0].length</code> &ne; <code>aBins[1].length</code></li>
	 *         <li><code>aFromIndex</code> &notin; [<code>0</code>;
	 *         <code>aBins[0].length</code>)</li>
	 *         <li><code>aToIndex</code> &notin; [<code>0</code>;
	 *         <code>aBins[0].length</code>)</li>
	 *         <li><code>aFromIndex</code> &gt; <code>aToIndex</code></li>
	 *         </ul>
	 */
	public LongHistogram(long[][] aBins, int aFromIndex, int aToIndex) {
		if (aBins[0].length != aBins[1].length || aFromIndex > aToIndex) {
			throw new ArrayIndexOutOfBoundsException();
		}
		final int binCount = aToIndex - aFromIndex + 1;
		bins = new long[2][binCount];
		for (int i = 0; i < binCount; ++i) {
			bins[0][i] = aBins[0][i + aFromIndex];
			bins[1][i] = aBins[1][i + aFromIndex];
		}
	}

	/**
	 * Initializes a new instance of <code>LongHistogram</code> based on the given observations.
	 * 
	 * @param aData Array of observations. The <code>i</code>-th element of this array contains
	 *        the number of observations for <code>[i; i + 1)</code>.
	 * @param aFromIndex Index of the array to be considered a starting index.
	 * @param aToIndex Index of the array to be considered last index; the observation at
	 *        <code>aToIndex</code> (the value of <code>aData[aToIndex]</code>) is also included
	 *        in the histogram.
	 * @throws ArrayIndexOutOfBoundsException If one of the following is <code>true</code>:
	 *         <ul>
	 *         <li><code>aFromIndex</code> &notin; [<code>0</code>; <code>aData.length</code>)</li>
	 *         <li><code>aToIndex</code> &notin; [<code>0</code>; <code>aData.length</code>)</li>
	 *         <li><code>aFromIndex</code> &gt; <code>aToIndex</code></li>
	 *         </ul>
	 */
	public LongHistogram(long[] aData, int aFromIndex, int aToIndex) {
		if (aFromIndex > aToIndex) {
			throw new ArrayIndexOutOfBoundsException();
		}
		final int binCount = aToIndex - aFromIndex + 1;
		bins = new long[2][binCount];
		for (int i = 0; i < binCount; ++i) {
			bins[0][i] = i + aFromIndex;
			bins[1][i] = aData[i + aFromIndex];
		}
	}

	/**
	 * Gets the number of bins in this histogram.
	 * 
	 * @return Number of bins in this histogram.
	 */
	public int getBinCount() {
		return bins[0].length;
	}

	/**
	 * Gets the range of observation values for this histogram.
	 * 
	 * @return Array of two elements - the minimum and the maximum value of observations. This is the
	 *         left end of the interval for the first bin and the right end of the interval for last
	 *         bin, as described in the description of this class.
	 */
	public long[] getObservedRange() {
		return new long[] { bins[0][0], bins[0][bins[0].length - 1] };
	}

	/**
	 * Gets the bins of this histogram.
	 * 
	 * @return Bins of this histogram in the form of a table of two rows.
	 */
	public long[][] getBins() {
		return bins;
	}

	/**
	 * Loads the data of the histogram from the given stream.
	 * 
	 * @param aArgs One-element array representing the argument passed to this type. This argument
	 *        must be the <code>String</code> representation of an integer and it specifies the
	 *        number of bins in this histogram.
	 * @param aReader Reader from a text stream. The reader must be open and positioned in the
	 *        stream such that the data for the histogram follows.
	 * @throws IOException If I/O error occurs.
	 * @throws NumberFormatException If the stream contains invalid data.
	 * @throws NullPointerException If at least one of the parameters is <code>null</code>.
	 * @throws ArrayIndexOutOfBoundsException If an empty array is specified for <code>aArgs</code>.
	 */
	public void load(String[] aArgs, LineReader aReader) throws IOException {
		final int binCount = Integer.parseInt(aArgs[0]);
		bins = new long[2][binCount];
		for (int i = 0; i < binCount; ++i) {
			String[] values = aReader.readLine().split(SEPREGEX);
			bins[0][i] = Integer.parseInt(values[0]);
			bins[1][i] = Integer.parseInt(values[1]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam#save(java.io.FileWriter)
	 */
	public void save(FileWriter aWriter, boolean aSaveArgs) throws IOException {
		final int binCount = bins[0].length;
		if (aSaveArgs) {
			aWriter.write(String.valueOf(binCount) + "\n");
		}
		for (int i = 0; i < binCount; ++i) {
			aWriter.write(String.valueOf(bins[0][i]) + SEP + String.valueOf(bins[1][i]) + "\n");
		}
	}

	/**
	 * Histogram values in the form of a table of 2 rows.
	 */
	private long[][] bins;
}
