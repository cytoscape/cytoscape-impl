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
