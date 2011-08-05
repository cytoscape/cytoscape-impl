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

package de.mpg.mpi_inf.bioinf.netanalyzer.data.io;

import java.io.IOException;

/**
 * Reader for &quot;.netstats&quot: version 1 files. It is a wrapper around a
 * {@link Netstats2Reader} instance, providing conversion wherever necessary.
 * 
 * @author Yassen Assenov
 */
class Netstats1Reader implements LineReader {

	/**
	 * Initializes a new instance of <code>Netstats1Reader</code>.
	 * 
	 * @param aReader Successfully initialized Netstats version 2 reader.
	 * @throws NullPointerException If <code>aReader</code> is <code>null</code>.
	 */
	public Netstats1Reader(Netstats2Reader aReader) {
		if (aReader == null) {
			throw new NullPointerException();
		}
		reader = aReader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.data.io.LineReader#close()
	 */
	public void close() throws IOException {
		reader.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.data.io.LineReader#readLine()
	 */
	public String readLine() throws IOException {
		String line = reader.readLine();
		if (line != null) {
			if (line.startsWith("splDist IntHistogram")) {
				line = "splDist LongHistogram" + line.substring(20);
			}
		}
		return line;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.data.io.LineReader#ready()
	 */
	public boolean ready() throws IOException {
		return reader.ready();
	}

	/**
	 * Underlying Netstats version 2 reader.
	 */
	private Netstats2Reader reader;
}
