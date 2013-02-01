package de.mpg.mpi_inf.bioinf.netanalyzer.data.io;

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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reader for &quot;.netstats&quot: version 2 files. It uses <code>BufferedReader</code> for
 * accessing the underlying stream.
 * 
 * @author Yassen Assenov
 */
class Netstats2Reader implements LineReader {

	/**
	 * Initializes a new instance of <code>Netstats2Reader</code>.
	 * 
	 * @param aFileReader Reader of the underlying file stream.
	 */
	public Netstats2Reader(FileReader aFileReader) {
		reader = new BufferedReader(aFileReader);
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
		return reader.readLine();
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
	 * Buffered reader of the underlying stream.
	 */
	private BufferedReader reader;
}
