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

import java.io.Closeable;
import java.io.IOException;

/**
 * Interface for reading text streams one line at a time.
 * 
 * @author Yassen Assenov
 */
public interface LineReader extends Closeable {

	/**
	 * Closes the stream. Once the stream is closed, further invocations of {@link #readLine()}
	 * cause <code>IOException</code>. Closing a previously-closed stream, however, has no effect.
	 * 
	 * @throws IOException If an I/O error occurs.
	 */
	public void close() throws IOException;

	/**
	 * Reads a line of text. A line is considered to be terminated by any one of a line feed ('\n'),
	 * a carriage return ('\r'), or a carriage return followed immediately by a line feed.
	 * 
	 * @return A <code>String</code> containing the contents of the line, not including any
	 * line-termination characters; or <code>null</code> if the end of the stream has been reached.
	 * @throws IOException If an I/O error occurs.
	 */
	public String readLine() throws IOException;

	/**
	 * Checks whether this stream is ready to be read.
	 * 
	 * @return <code>true</code> if the next invocation of {@link #readLine()} is guaranteed not to
	 * block for input, <code>false</code> otherwise. Note that returning <code>false</code> does
	 * not guarantee that the next read will block.
	 * @throws IOException If an I/O error occurs.
	 */
	public boolean ready() throws IOException;
}
