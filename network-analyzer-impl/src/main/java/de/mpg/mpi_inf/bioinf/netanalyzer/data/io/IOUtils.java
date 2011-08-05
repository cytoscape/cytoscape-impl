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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility class providing helper methods for stream manipulation.
 * 
 * @author Yassen Assenov
 */
public abstract class IOUtils {

	/**
	 * Reads the contents of the given stream as a <code>String</code> and closes the stream.
	 * 
	 * @param aStream
	 *            Stream to be read.
	 * @return The text read from the stream.
	 * 
	 * @throws IOException
	 *             If an I/O error occurs while reading from or while closing the stream.
	 * @throws NullPointerException
	 *             If <code>aStream</code> is <code>null</code>.
	 */
	public static final String readFile(InputStream aStream) throws IOException {
		final StringBuilder text = new StringBuilder();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(aStream));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (text.length() != 0) {
					text.append("\n");
				}
				text.append(line);
			}
		} catch (IOException ex) {
			closeStream(reader);
			throw ex;
		}
		reader.close();
		return text.toString();
	}

	/**
	 * Silently closes the given stream.
	 * <p>
	 * This method does not throw an exception in any circumstances.
	 * </p>
	 * 
	 * @param aStream
	 *            Stream to be closed.
	 */
	public static void closeStream(Closeable aStream) {
		try {
			aStream.close();
		} catch (Exception ex) {
			// Unsuccessful attempt to close the stream; ignore
		}
	}
}
